package com.albionservant.integration.local;

import com.albionservant.integration.config.LocalEndpointGuard;
import com.albionservant.integration.config.LocalIntegrationProperties;
import com.albionservant.integration.nats.NatsIngestionRuntime;
import com.albionservant.market.sqlite.SqliteSchemaInitializer;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class LocalIntegrationCoordinator {

    private static final Logger log = LoggerFactory.getLogger(LocalIntegrationCoordinator.class);

    private final LocalIntegrationProperties properties;
    private final LocalEndpointGuard endpointGuard;
    private final LocalProcessManager processManager;
    private final SqliteSchemaInitializer schemaInitializer;
    private final NatsIngestionRuntime natsRuntime;
    private final LocalIntegrationStatus status;
    private final ExecutorService starter = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "local-integration-starter");
        thread.setDaemon(true);
        return thread;
    });

    public LocalIntegrationCoordinator(
            LocalIntegrationProperties properties,
            LocalEndpointGuard endpointGuard,
            LocalProcessManager processManager,
            SqliteSchemaInitializer schemaInitializer,
            NatsIngestionRuntime natsRuntime,
            LocalIntegrationStatus status
    ) {
        this.properties = properties;
        this.endpointGuard = endpointGuard;
        this.processManager = processManager;
        this.schemaInitializer = schemaInitializer;
        this.natsRuntime = natsRuntime;
        this.status = status;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!properties.isEnabled()) {
            status.update(LocalIntegrationStatus.State.DISABLED, "Local market integration is disabled");
            return;
        }
        starter.submit(this::startInternal);
    }

    private void startInternal() {
        status.update(LocalIntegrationStatus.State.STARTING, "Starting SQLite and NATS");

        try {
            endpointGuard.validate();
            schemaInitializer.initialize();
            processManager.startNatsIfNeeded();
            natsRuntime.start();
            processManager.startAodpIfNeeded();

            status.update(LocalIntegrationStatus.State.RUNNING, "Local SQLite market ingestion is running");
            log.info("AlbionServant local SQLite market integration is running");
        } catch (Exception e) {
            status.update(LocalIntegrationStatus.State.FAILED, e.getMessage());
            log.error("Could not start local market integration", e);
        }
    }

    @PreDestroy
    public void stop() {
        starter.shutdownNow();
        status.update(LocalIntegrationStatus.State.STOPPED, "Local market integration stopped");
    }
}
