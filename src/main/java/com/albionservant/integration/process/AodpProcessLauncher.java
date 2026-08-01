package com.albionservant.integration.process;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class AodpProcessLauncher {

    private static final Logger log =
            LoggerFactory.getLogger(AodpProcessLauncher.class);

    private static final String PROCESS_NAME =
            "albiondata-client-local.exe";

    private final boolean integrationEnabled;
    private final boolean processManagementEnabled;
    private final boolean startAodp;

    private final String aodpBinary;
    private final String natsUrl;
    private final String natsUser;
    private final String natsPassword;
    private final String privateIngest;
    private final String listenDevices;

    private final ExecutorService startupExecutor =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(
                        runnable,
                        "aodp-process-launcher"
                );

                thread.setDaemon(true);
                return thread;
            });

    private volatile Process managedProcess;

    public AodpProcessLauncher(
            @Value("${albion.local.enabled:true}")
            boolean integrationEnabled,

            @Value("${albion.local.processes.manage:true}")
            boolean processManagementEnabled,

            @Value("${albion.local.processes.start-aodp:true}")
            boolean startAodp,

            @Value("${albion.local.processes.aodp-binary:runtime/aodp/albiondata-client-local.exe}")
            String aodpBinary,

            @Value("${albion.local.nats.url:nats://127.0.0.1:4222}")
            String natsUrl,

            @Value("${albion.local.nats.user:albionservant}")
            String natsUser,

            @Value("${albion.local.nats.username:}")
            String legacyNatsUsername,

            @Value("${albion.local.nats.password:local-only-change-me}")
            String natsPassword,

            @Value("${albion.local.processes.aodp-private-ingest:noop}")
            String privateIngest,

            @Value("${albion.local.processes.aodp-listen-devices:}")
            String listenDevices
    ) {
        this.integrationEnabled = integrationEnabled;
        this.processManagementEnabled = processManagementEnabled;
        this.startAodp = startAodp;
        this.aodpBinary = aodpBinary;
        this.natsUrl = natsUrl;

        /*
         * Obsługuje obie spotykane nazwy konfiguracji:
         * albion.local.nats.user
         * albion.local.nats.username
         */
        this.natsUser = !isBlank(natsUser)
                ? natsUser
                : legacyNatsUsername;

        this.natsPassword = natsPassword;
        this.privateIngest = privateIngest;
        this.listenDevices = listenDevices;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info(
                "AODP launcher configuration: enabled={}, manage={}, startAodp={}, binary={}",
                integrationEnabled,
                processManagementEnabled,
                startAodp,
                aodpBinary
        );

        if (!integrationEnabled) {
            log.info("AODP launcher disabled: albion.local.enabled=false");
            return;
        }

        if (!processManagementEnabled) {
            log.info(
                    "AODP launcher disabled: albion.local.processes.manage=false"
            );
            return;
        }

        if (!startAodp) {
            log.info(
                    "AODP launcher disabled: albion.local.processes.start-aodp=false"
            );
            return;
        }

        startupExecutor.submit(this::startSafely);
    }

    private void startSafely() {
        try {
            startAodpProcess();
        } catch (Exception exception) {
            log.error(
                    "Unable to start local AODP process.",
                    exception
            );
        }
    }

    private synchronized void startAodpProcess()
            throws IOException, InterruptedException {

        if (!isWindows()) {
            log.warn(
                    "Automatic AODP startup is currently configured for Windows only."
            );
            return;
        }

        Optional<ProcessHandle> existing =
                findExistingAodpProcess();

        if (existing.isPresent()) {
            log.info(
                    "AODP process is already running. PID={}",
                    existing.get().pid()
            );
            return;
        }

        Path projectRoot = resolveProjectRoot();
        Path executable = resolveBinary(projectRoot);

        log.info("Resolved project root: {}", projectRoot);
        log.info("Resolved AODP executable: {}", executable);

        if (!Files.isRegularFile(executable)) {
            throw new IOException(
                    "AODP executable does not exist: "
                            + executable.toAbsolutePath()
            );
        }

        if (!Files.isReadable(executable)) {
            throw new IOException(
                    "AODP executable is not readable: "
                            + executable.toAbsolutePath()
            );
        }

        NatsEndpoint endpoint = parseNatsEndpoint(natsUrl);

        log.info(
                "Waiting for local NATS at {}:{}...",
                endpoint.host(),
                endpoint.port()
        );

        boolean natsAvailable = waitForPort(
                endpoint.host(),
                endpoint.port(),
                Duration.ofSeconds(30)
        );

        if (!natsAvailable) {
            throw new IOException(
                    "NATS did not become available at "
                            + endpoint.host()
                            + ":"
                            + endpoint.port()
                            + " within 30 seconds."
            );
        }

        Path logDirectory = resolveLogDirectory();
        Files.createDirectories(logDirectory);

        Path stdoutLog =
                logDirectory.resolve("aodp-stdout.log");

        Path stderrLog =
                logDirectory.resolve("aodp-stderr.log");

        List<String> command = buildCommand(
                executable,
                endpoint
        );

        log.info(
                "Starting local AODP process with command: {}",
                sanitizeCommand(command)
        );

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        processBuilder.directory(
                executable.getParent().toFile()
        );

        processBuilder.redirectOutput(
                ProcessBuilder.Redirect.appendTo(
                        stdoutLog.toFile()
                )
        );

        processBuilder.redirectError(
                ProcessBuilder.Redirect.appendTo(
                        stderrLog.toFile()
                )
        );

        processBuilder.environment().put(
                "ALBIONSERVANT_LOCAL_ONLY",
                "true"
        );

        managedProcess = processBuilder.start();

        boolean exitedImmediately =
                managedProcess.waitFor(
                        2,
                        TimeUnit.SECONDS
                );

        if (exitedImmediately) {
            int exitCode = managedProcess.exitValue();
            managedProcess = null;

            throw new IOException(
                    "AODP process exited immediately with code "
                            + exitCode
                            + ". Check logs: "
                            + stdoutLog
                            + " and "
                            + stderrLog
            );
        }

        log.info(
                "Local AODP process started successfully. PID={}",
                managedProcess.pid()
        );

        log.info("AODP stdout log: {}", stdoutLog);
        log.info("AODP stderr log: {}", stderrLog);
    }

    private List<String> buildCommand(
            Path executable,
            NatsEndpoint endpoint
    ) {
        List<String> command = new ArrayList<>();

        command.add(executable.toAbsolutePath().toString());

        /*
         * Przekazujemy parametry również wtedy, gdy lokalny build
         * ma już endpoint zaszyty. Dzięki temu zachowanie procesu
         * jest jednoznaczne i widoczne w konfiguracji.
         */
        command.add("-i");
        command.add(buildAuthenticatedNatsUrl(endpoint));

        if (!isBlank(privateIngest)
                && !"noop".equalsIgnoreCase(privateIngest.trim())) {

            command.add("-p");
            command.add(privateIngest.trim());
        }

        command.add("-minimize");

        if (!isBlank(listenDevices)) {
            /*
             * AODP przyjmuje urządzenia jako wartość flagi
             * listen-devices. Nie rozbijamy tej wartości,
             * ponieważ może zawierać listę oddzieloną przecinkami.
             */
            command.add("-listen-devices");
            command.add(listenDevices.trim());
        }

        return command;
    }

    private String buildAuthenticatedNatsUrl(
            NatsEndpoint endpoint
    ) {
        if (isBlank(natsUser)) {
            return "nats://"
                    + endpoint.host()
                    + ":"
                    + endpoint.port();
        }

        return "nats://"
                + natsUser
                + ":"
                + natsPassword
                + "@"
                + endpoint.host()
                + ":"
                + endpoint.port();
    }

    private Path resolveProjectRoot() {
        Path workingDirectory =
                Paths.get(
                        System.getProperty("user.dir")
                ).toAbsolutePath().normalize();

        if (Files.exists(workingDirectory.resolve("pom.xml"))) {
            return workingDirectory;
        }

        Path current = workingDirectory;

        while (current != null) {
            if (Files.exists(current.resolve("pom.xml"))) {
                return current;
            }

            current = current.getParent();
        }

        return workingDirectory;
    }

    private Path resolveBinary(Path projectRoot) {
        Path configured =
                Paths.get(aodpBinary);

        if (configured.isAbsolute()) {
            return configured.normalize();
        }

        return projectRoot
                .resolve(configured)
                .normalize();
    }

    private Path resolveLogDirectory() {
        String localAppData =
                System.getenv("LOCALAPPDATA");

        if (!isBlank(localAppData)) {
            return Paths.get(
                    localAppData,
                    "AlbionServant",
                    "logs"
            );
        }

        return Paths.get(
                System.getProperty("user.home"),
                ".albionservant",
                "logs"
        );
    }

    private Optional<ProcessHandle> findExistingAodpProcess() {
        return ProcessHandle.allProcesses()
                .filter(ProcessHandle::isAlive)
                .filter(handle -> {
                    ProcessHandle.Info info =
                            handle.info();

                    String command =
                            info.command().orElse("");

                    String commandLine =
                            info.commandLine().orElse("");

                    String combined =
                            command + " " + commandLine;

                    return combined
                            .toLowerCase(Locale.ROOT)
                            .contains(
                                    PROCESS_NAME.toLowerCase(
                                            Locale.ROOT
                                    )
                            );
                })
                .findFirst();
    }

    private boolean waitForPort(
            String host,
            int port,
            Duration timeout
    ) throws InterruptedException {
        long deadline =
                System.nanoTime()
                        + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (isPortOpen(host, port)) {
                return true;
            }

            Thread.sleep(250);
        }

        return false;
    }

    private boolean isPortOpen(
            String host,
            int port
    ) {
        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(host, port),
                    500
            );

            return true;

        } catch (IOException ignored) {
            return false;
        }
    }

    private NatsEndpoint parseNatsEndpoint(
            String configuredUrl
    ) {
        String value = configuredUrl;

        if (isBlank(value)) {
            return new NatsEndpoint(
                    "127.0.0.1",
                    4222
            );
        }

        value = value.trim();

        value = value.replaceFirst(
                "^nats://",
                ""
        );

        int atIndex = value.lastIndexOf('@');

        if (atIndex >= 0) {
            value = value.substring(atIndex + 1);
        }

        int slashIndex = value.indexOf('/');

        if (slashIndex >= 0) {
            value = value.substring(0, slashIndex);
        }

        String host = value;
        int port = 4222;

        int colonIndex = value.lastIndexOf(':');

        if (colonIndex > 0) {
            host = value.substring(0, colonIndex);

            String rawPort =
                    value.substring(colonIndex + 1);

            try {
                port = Integer.parseInt(rawPort);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "Invalid NATS port in URL: "
                                + configuredUrl,
                        exception
                );
            }
        }

        if (!isLoopbackHost(host)) {
            throw new IllegalArgumentException(
                    "Local-only mode refuses non-loopback "
                            + "NATS host: "
                            + host
            );
        }

        return new NatsEndpoint(host, port);
    }

    private boolean isLoopbackHost(String host) {
        if (host == null) {
            return false;
        }

        String normalized =
                host.trim().toLowerCase(Locale.ROOT);

        return normalized.equals("127.0.0.1")
                || normalized.equals("localhost")
                || normalized.equals("::1")
                || normalized.equals("[::1]");
    }

    private String sanitizeCommand(
            List<String> command
    ) {
        List<String> sanitized =
                new ArrayList<>(command);

        for (int i = 0; i < sanitized.size(); i++) {
            String value = sanitized.get(i);

            if (value.startsWith("nats://")
                    && value.contains("@")) {

                int schemeEnd =
                        value.indexOf("://") + 3;

                int atIndex =
                        value.lastIndexOf('@');

                sanitized.set(
                        i,
                        value.substring(0, schemeEnd)
                                + "***:***"
                                + value.substring(atIndex)
                );
            }
        }

        return String.join(" ", sanitized);
    }

    private boolean isWindows() {
        return System.getProperty("os.name")
                .toLowerCase(Locale.ROOT)
                .contains("win");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @PreDestroy
    public synchronized void stop() {
        startupExecutor.shutdownNow();

        Process process = managedProcess;

        if (process == null || !process.isAlive()) {
            return;
        }

        log.info(
                "Stopping managed AODP process. PID={}",
                process.pid()
        );

        process.destroy();

        try {
            boolean stopped =
                    process.waitFor(
                            5,
                            TimeUnit.SECONDS
                    );

            if (!stopped && process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(
                        3,
                        TimeUnit.SECONDS
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            if (process.isAlive()) {
                process.destroyForcibly();
            }
        } finally {
            managedProcess = null;
        }
    }

    private record NatsEndpoint(
            String host,
            int port
    ) {
    }
}