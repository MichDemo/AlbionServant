package com.albionservant.integration.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

@Component
public class LocalEndpointGuard {

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "[::1]");

    private final LocalIntegrationProperties properties;
    private final Environment environment;

    public LocalEndpointGuard(LocalIntegrationProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    public void validate() {
        if (!properties.isEnabled() || !properties.isStrictLoopback()) {
            return;
        }

        requireLoopbackUri("NATS", properties.getNats().getUrl());

        String mongoUri = environment.getProperty(
                "spring.data.mongodb.uri",
                "mongodb://127.0.0.1:27017/albionservant"
        );
        requireLoopbackMongoUri(mongoUri);
    }

    private void requireLoopbackUri(String label, String value) {
        URI uri = URI.create(value);
        String host = uri.getHost();

        if (host == null || !isLoopback(host)) {
            throw new IllegalStateException(label + " endpoint must be loopback-only, got: " + value);
        }
    }

    private void requireLoopbackMongoUri(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        if (!normalized.startsWith("mongodb://") && !normalized.startsWith("mongodb+srv://")) {
            throw new IllegalStateException("Invalid MongoDB URI: " + value);
        }

        String authorityAndPath = normalized.substring(normalized.indexOf("//") + 2);
        String authority = authorityAndPath.split("/", 2)[0];
        String hostsPart = authority.contains("@")
                ? authority.substring(authority.lastIndexOf('@') + 1)
                : authority;

        for (String hostEntry : hostsPart.split(",")) {
            String host = hostEntry.trim();
            if (host.startsWith("[")) {
                int end = host.indexOf(']');
                host = end >= 0 ? host.substring(0, end + 1) : host;
            } else if (host.contains(":")) {
                host = host.substring(0, host.indexOf(':'));
            }

            if (!isLoopback(host)) {
                throw new IllegalStateException("MongoDB endpoint must be loopback-only, got: " + value);
            }
        }
    }

    private boolean isLoopback(String host) {
        return LOOPBACK_HOSTS.contains(host.toLowerCase(Locale.ROOT));
    }
}
