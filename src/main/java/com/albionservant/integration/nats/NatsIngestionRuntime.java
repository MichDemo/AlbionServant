package com.albionservant.integration.nats;

import com.albionservant.integration.aodp.AodpMessageRouter;
import com.albionservant.integration.aodp.AodpSubjects;
import com.albionservant.integration.aodp.PermanentMessageException;
import com.albionservant.integration.config.LocalIntegrationProperties;
import io.nats.client.Connection;
import io.nats.client.JetStreamManagement;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class NatsIngestionRuntime {

    private static final Logger log =
            LoggerFactory.getLogger(NatsIngestionRuntime.class);

    private final LocalIntegrationProperties properties;
    private final AodpMessageRouter router;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ExecutorService workers;
    private Connection connection;

    public NatsIngestionRuntime(
            LocalIntegrationProperties properties,
            AodpMessageRouter router
    ) {
        this.properties = properties;
        this.router = router;
    }

    public synchronized void start()
            throws IOException, InterruptedException, JetStreamApiException {

        if (!properties.isEnabled() || running.get()) {
            return;
        }

        connection = Nats.connect(buildOptions());

        ensureStream(connection);

        running.set(true);

        workers = Executors.newFixedThreadPool(
                AodpSubjects.CONSUMED_SUBJECTS.size(),
                new NamedDaemonThreadFactory("aodp-nats-consumer-")
        );

        for (String subject : AodpSubjects.CONSUMED_SUBJECTS) {
            workers.submit(() -> consumeLoop(subject));
        }

        log.info(
                "Local AODP NATS ingestion started for subjects {}",
                AodpSubjects.CONSUMED_SUBJECTS
        );
    }

    private Options buildOptions() {
        Options.Builder builder = new Options.Builder()
                .server(properties.getNats().getUrl())
                .connectionName("albionservant-local-ingestor")
                .connectionTimeout(Duration.ofSeconds(5))
                .reconnectWait(Duration.ofSeconds(2))
                .maxReconnects(-1);

        if (!isBlank(properties.getNats().getUser())) {
            builder.userInfo(
                    properties.getNats().getUser(),
                    properties.getNats().getPassword()
            );
        }

        return builder.build();
    }

    private void ensureStream(Connection natsConnection)
            throws IOException, JetStreamApiException {

        JetStreamManagement management =
                natsConnection.jetStreamManagement();

        String streamName = properties.getNats().getStreamName();

        try {
            management.getStreamInfo(streamName);

            log.info("Using existing JetStream stream {}", streamName);
            return;

        } catch (JetStreamApiException exception) {
            if (exception.getErrorCode() != 404) {
                throw exception;
            }
        }

        StreamConfiguration configuration =
                StreamConfiguration.builder()
                        .name(streamName)
                        .subjects(
                                AodpSubjects.STREAM_SUBJECTS.toArray(String[]::new)
                        )
                        .storageType(StorageType.File)
                        .retentionPolicy(RetentionPolicy.Limits)
                        .maxAge(
                                Duration.ofDays(
                                        Math.max(
                                                1,
                                                properties.getRawRetentionDays()
                                        )
                                )
                        )
                        .maxBytes(properties.getNats().getMaxBytes())
                        .replicas(1)
                        .build();

        management.addStream(configuration);

        log.info("Created JetStream stream {}", streamName);
    }

    private void consumeLoop(String subject) {
        String durableName = durableName(subject);

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                JetStreamSubscription subscription =
                        createSubscription(subject, durableName);

                while (
                        running.get()
                                && !Thread.currentThread().isInterrupted()
                ) {
                    List<Message> messages = subscription.fetch(
                            Math.max(
                                    1,
                                    properties.getNats().getBatchSize()
                            ),
                            Duration.ofSeconds(1)
                    );

                    for (Message message : messages) {
                        if (
                                !running.get()
                                        || Thread.currentThread().isInterrupted()
                        ) {
                            return;
                        }

                        processMessage(subject, message);
                    }
                }

            } catch (Exception exception) {
                /*
                 * JNATS fetch() nie deklaruje InterruptedException.
                 * Przerwanie wątku obsługujemy więc przez sprawdzenie
                 * flagi interrupted oraz running.
                 */
                if (
                        !running.get()
                                || Thread.currentThread().isInterrupted()
                ) {
                    return;
                }

                log.error(
                        "NATS consumer for {} failed; "
                                + "recreating subscription in 2 seconds",
                        subject,
                        exception
                );

                sleepQuietly(Duration.ofSeconds(2));
            }
        }
    }

    private JetStreamSubscription createSubscription(
            String subject,
            String durableName
    ) throws IOException, JetStreamApiException {

        ConsumerConfiguration consumerConfiguration =
                ConsumerConfiguration.builder()
                        .durable(durableName)
                        .filterSubject(subject)
                        .deliverPolicy(DeliverPolicy.All)
                        .ackPolicy(AckPolicy.Explicit)
                        .ackWait(
                                Duration.ofSeconds(
                                        Math.max(
                                                10,
                                                properties
                                                        .getNats()
                                                        .getAckWaitSeconds()
                                        )
                                )
                        )
                        .maxDeliver(
                                Math.max(
                                        1,
                                        properties
                                                .getNats()
                                                .getMaxDeliver()
                                )
                        )
                        .build();

        PullSubscribeOptions subscribeOptions =
                PullSubscribeOptions.builder()
                        .stream(properties.getNats().getStreamName())
                        .configuration(consumerConfiguration)
                        .build();

        return connection
                .jetStream()
                .subscribe(subject, subscribeOptions);
    }

    private void processMessage(
            String subject,
            Message message
    ) {
        try {
            router.route(subject, message.getData());

            /*
             * ACK wykonujemy dopiero po poprawnym przetworzeniu
             * i zapisaniu danych.
             */
            message.ack();

        } catch (PermanentMessageException exception) {
            log.warn(
                    "Rejecting malformed AODP message on {}: {}",
                    subject,
                    exception.getMessage()
            );

            /*
             * TERM oznacza, że wiadomości nie należy ponownie dostarczać.
             */
            message.term();

        } catch (RuntimeException exception) {
            log.error(
                    "Temporary failure while processing "
                            + "AODP message on {}",
                    subject,
                    exception
            );

            /*
             * NAK powoduje ponowne dostarczenie wiadomości.
             */
            message.nak();
        }
    }

    private String durableName(String subject) {
        return "albionservant-"
                + subject.replace('.', '-');
    }

    private void sleepQuietly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @PreDestroy
    public synchronized void stop() {
        running.set(false);

        if (workers != null) {
            workers.shutdownNow();
            workers = null;
        }

        if (connection != null) {
            try {
                connection.close();

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

            } finally {
                connection = null;
            }
        }

        log.info("Local AODP NATS ingestion stopped");
    }

    private static class NamedDaemonThreadFactory
            implements ThreadFactory {

        private final String prefix;
        private int counter;

        private NamedDaemonThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread thread = new Thread(
                    runnable,
                    prefix + (++counter)
            );

            thread.setDaemon(true);

            return thread;
        }
    }
}