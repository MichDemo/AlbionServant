package com.albionservant.integration.local;

import com.albionservant.integration.config.LocalIntegrationProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class LocalProcessManager {

    private static final Logger log = LoggerFactory.getLogger(LocalProcessManager.class);

    private final LocalIntegrationProperties properties;
    private final Map<String, Process> ownedProcesses = new LinkedHashMap<>();
    private final Path projectRoot = Path.of("").toAbsolutePath().normalize();

    public LocalProcessManager(LocalIntegrationProperties properties) {
        this.properties = properties;
    }

    public void startNatsIfNeeded() throws IOException {
        LocalIntegrationProperties.Processes config = properties.getProcesses();
        if (!config.isManage() || !config.isStartNats() || isPortOpen(config.getNatsPort())) {
            return;
        }

        Path binary = requireBinary(config.getNatsBinary(), "NATS");
        Path natsConfig = resolve(config.getNatsConfig());
        if (!Files.isRegularFile(natsConfig)) {
            throw new IOException("NATS config does not exist: " + natsConfig);
        }

        startProcess("nats", List.of(binary.toString(), "-c", natsConfig.toString()));
        waitForPort("NATS", config.getNatsPort(), process("nats"));
    }

    public void startAodpIfNeeded() throws IOException {
        LocalIntegrationProperties.Processes config = properties.getProcesses();
        if (!config.isManage() || !config.isStartAodp()) {
            return;
        }

        Path binary = requireBinary(config.getAodpBinary(), "AODP client");
        if (properties.isStrictLoopback()
                && !binary.getFileName().toString().toLowerCase(Locale.ROOT).contains("local")) {
            throw new IOException(
                    "Strict local mode requires a locally patched AODP binary whose filename contains 'local'. "
                            + "The official client runs an updater that can contact GitHub."
            );
        }

        List<String> command = new ArrayList<>();
        command.add(binary.toString());
        command.add("-i");
        command.add(natsUrlWithCredentials());
        command.add("-p");
        command.add("noop");
        command.add("-minimize");

        if (config.getAodpListenDevices() != null && !config.getAodpListenDevices().isBlank()) {
            command.add("-l");
            command.add(config.getAodpListenDevices().trim());
        }

        startProcess("aodp", command);
    }

    private String natsUrlWithCredentials() {
        String url = properties.getNats().getUrl();
        String user = properties.getNats().getUser();
        String password = properties.getNats().getPassword();

        if (user == null || user.isBlank() || url.contains("@")) {
            return url;
        }

        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return url;
        }

        return url.substring(0, schemeEnd + 3)
                + user + ":" + (password == null ? "" : password) + "@"
                + url.substring(schemeEnd + 3);
    }

    private void startProcess(String name, List<String> command) throws IOException {
        Process current = ownedProcesses.get(name);
        if (current != null && current.isAlive()) {
            return;
        }

        Path logDir = resolve(properties.getProcesses().getLogDir());
        Files.createDirectories(logDir);
        Path logFile = logDir.resolve(name + ".log");

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(projectRoot.toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));

        log.info("Starting local process {}: {}", name, command);
        Process process = builder.start();
        ownedProcesses.put(name, process);
    }

    private void waitForPort(String label, int port, Process process) throws IOException {
        Duration timeout = Duration.ofSeconds(Math.max(2, properties.getProcesses().getStartupTimeoutSeconds()));
        long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (isPortOpen(port)) {
                return;
            }
            if (process != null && !process.isAlive()) {
                throw new IOException(label + " exited during startup with code " + process.exitValue());
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for " + label, e);
            }
        }
        throw new IOException(label + " did not open 127.0.0.1:" + port + " within " + timeout);
    }

    private boolean isPortOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 250);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private Path requireBinary(String configuredPath, String label) throws IOException {
        Path path = resolve(configuredPath);
        if (!Files.isRegularFile(path)) {
            throw new IOException(label + " binary does not exist: " + path);
        }
        return path;
    }

    private Path resolve(String configuredPath) {
        Path path = Path.of(configuredPath);
        return path.isAbsolute() ? path.normalize() : projectRoot.resolve(path).normalize();
    }

    private Process process(String name) {
        return ownedProcesses.get(name);
    }

    @PreDestroy
    public synchronized void stopOwnedProcesses() {
        List<Map.Entry<String, Process>> entries = new ArrayList<>(ownedProcesses.entrySet());
        Collections.reverse(entries);

        for (Map.Entry<String, Process> entry : entries) {
            Process process = entry.getValue();
            if (!process.isAlive()) {
                continue;
            }

            log.info("Stopping local process {}", entry.getKey());
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();

            try {
                if (!process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.descendants().forEach(ProcessHandle::destroyForcibly);
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }

        ownedProcesses.clear();
    }
}
