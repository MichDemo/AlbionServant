package com.albionservant.integration.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LocalIntegrationProperties.class)
public class LocalIntegrationConfiguration {
}
