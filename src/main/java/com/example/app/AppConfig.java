
package com.example.app;

import javafx.scene.paint.Color;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppConfig {

    public static final Color BACKGROUND_MAIN = Color.rgb(10, 20, 40);
    public static final Color TEXT_PRIMARY = Color.rgb(220, 230, 255);
// Add these to your existing AppConfig class

    /** Tab bar background */
    public static final Color BACKGROUND_TAB_BAR = Color.rgb(45, 55, 75);

    /** Inactive tab color (the brownish/maroon you have) */
    public static final Color TAB_INACTIVE = Color.rgb(120, 70, 70);

    /** Active tab color */
    public static final Color TAB_ACTIVE = Color.rgb(200, 80, 80);

    /** Tab text color */
    public static final Color TAB_TEXT = Color.rgb(235, 235, 240);
    public static final String APP_TITLE = "AlbionServant";
    public static final String VERSION = "0.1.0";
}
