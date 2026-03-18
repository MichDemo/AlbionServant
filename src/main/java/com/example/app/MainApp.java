
package com.example.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class MainApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Boot the Spring context with headless mode disabled (required for JavaFX)
        context = new SpringApplicationBuilder(AppConfig.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) {
        GreetingService service = context.getBean(GreetingService.class);

        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(AppConfig.BACKGROUND_MAIN, null, null)));

        TopNavigationBar topBar = new TopNavigationBar();

        root.getChildren().add(topBar);

        Scene scene = new Scene(root, 1280, 960);
        stage.setTitle("AlbionServant App");
        stage.setScene(scene);

        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
