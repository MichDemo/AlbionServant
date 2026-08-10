package com.albionservant;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application and Spring Boot configuration entry point.
 *
 * Visual configuration intentionally lives in JavaFX CSS.  Keep this class
 * available for future runtime, profile, service and application metadata
 * configuration without coupling it to the presentation layer.
 */
@SpringBootApplication
public class AppConfig {

    public static final String APP_TITLE = "AlbionServant";
    public static final String VERSION = "0.1.0";

    /** Single application stylesheet loaded centrally by MainApp. */
    public static final String MAIN_STYLESHEET = "/css/albion-minimal-gray.css";
}
