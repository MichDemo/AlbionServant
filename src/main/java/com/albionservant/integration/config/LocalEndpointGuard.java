package com.albionservant.integration.config;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

@Component
public class LocalEndpointGuard {

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "[::1]");
    private final LocalIntegrationProperties properties;

    public LocalEndpointGuard(LocalIntegrationProperties properties) {
        this.properties = properties;
    }

    public void validate() {
        if (!properties.isEnabled() || !properties.isStrictLoopback()) {
            return;
        }
        requireLoopbackUri("NATS", properties.getNats().getUrl());
    }

    private void requireLoopbackUri(String label, String value) {
        URI uri = URI.create(value);
        String host = uri.getHost();
        if (host == null || !LOOPBACK_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException(label + " endpoint must be loopback-only, got: " + value);
        }
    }
}
