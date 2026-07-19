package com.albionservant.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "albion.local")
public class LocalIntegrationProperties {

    private boolean enabled = true;
    private boolean strictLoopback = true;
    private String serverName = "EUROPE";
    private int rawRetentionDays = 7;
    private final Nats nats = new Nats();
    private final Processes processes = new Processes();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isStrictLoopback() {
        return strictLoopback;
    }

    public void setStrictLoopback(boolean strictLoopback) {
        this.strictLoopback = strictLoopback;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getRawRetentionDays() {
        return rawRetentionDays;
    }

    public void setRawRetentionDays(int rawRetentionDays) {
        this.rawRetentionDays = rawRetentionDays;
    }

    public Nats getNats() {
        return nats;
    }

    public Processes getProcesses() {
        return processes;
    }

    public static class Nats {
        private String url = "nats://127.0.0.1:4222";
        private String user = "albionservant";
        private String password = "local-only-change-me";
        private String streamName = "AODP_RAW";
        private long maxBytes = 5L * 1024L * 1024L * 1024L;
        private int batchSize = 100;
        private int ackWaitSeconds = 60;
        private int maxDeliver = 10;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getStreamName() {
            return streamName;
        }

        public void setStreamName(String streamName) {
            this.streamName = streamName;
        }

        public long getMaxBytes() {
            return maxBytes;
        }

        public void setMaxBytes(long maxBytes) {
            this.maxBytes = maxBytes;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getAckWaitSeconds() {
            return ackWaitSeconds;
        }

        public void setAckWaitSeconds(int ackWaitSeconds) {
            this.ackWaitSeconds = ackWaitSeconds;
        }

        public int getMaxDeliver() {
            return maxDeliver;
        }

        public void setMaxDeliver(int maxDeliver) {
            this.maxDeliver = maxDeliver;
        }
    }

    public static class Processes {
        private boolean manage = false;
        private boolean startMongo = true;
        private boolean startNats = true;
        private boolean startAodp = false;
        private String mongoBinary = "runtime/mongodb/bin/mongod.exe";
        private String natsBinary = "runtime/nats/nats-server.exe";
        private String aodpBinary = "runtime/aodp/albiondata-client-local.exe";
        private String natsConfig = "runtime/nats/nats-server.conf";
        private String mongoDataDir = "data/mongodb";
        private String logDir = "data/logs";
        private int mongoPort = 27017;
        private int natsPort = 4222;
        private int startupTimeoutSeconds = 20;
        private String aodpListenDevices = "";

        public boolean isManage() {
            return manage;
        }

        public void setManage(boolean manage) {
            this.manage = manage;
        }

        public boolean isStartMongo() {
            return startMongo;
        }

        public void setStartMongo(boolean startMongo) {
            this.startMongo = startMongo;
        }

        public boolean isStartNats() {
            return startNats;
        }

        public void setStartNats(boolean startNats) {
            this.startNats = startNats;
        }

        public boolean isStartAodp() {
            return startAodp;
        }

        public void setStartAodp(boolean startAodp) {
            this.startAodp = startAodp;
        }

        public String getMongoBinary() {
            return mongoBinary;
        }

        public void setMongoBinary(String mongoBinary) {
            this.mongoBinary = mongoBinary;
        }

        public String getNatsBinary() {
            return natsBinary;
        }

        public void setNatsBinary(String natsBinary) {
            this.natsBinary = natsBinary;
        }

        public String getAodpBinary() {
            return aodpBinary;
        }

        public void setAodpBinary(String aodpBinary) {
            this.aodpBinary = aodpBinary;
        }

        public String getNatsConfig() {
            return natsConfig;
        }

        public void setNatsConfig(String natsConfig) {
            this.natsConfig = natsConfig;
        }

        public String getMongoDataDir() {
            return mongoDataDir;
        }

        public void setMongoDataDir(String mongoDataDir) {
            this.mongoDataDir = mongoDataDir;
        }

        public String getLogDir() {
            return logDir;
        }

        public void setLogDir(String logDir) {
            this.logDir = logDir;
        }

        public int getMongoPort() {
            return mongoPort;
        }

        public void setMongoPort(int mongoPort) {
            this.mongoPort = mongoPort;
        }

        public int getNatsPort() {
            return natsPort;
        }

        public void setNatsPort(int natsPort) {
            this.natsPort = natsPort;
        }

        public int getStartupTimeoutSeconds() {
            return startupTimeoutSeconds;
        }

        public void setStartupTimeoutSeconds(int startupTimeoutSeconds) {
            this.startupTimeoutSeconds = startupTimeoutSeconds;
        }

        public String getAodpListenDevices() {
            return aodpListenDevices;
        }

        public void setAodpListenDevices(String aodpListenDevices) {
            this.aodpListenDevices = aodpListenDevices;
        }
    }
}
