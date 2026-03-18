
package com.example.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

        Label label = new Label(service.getGreeting());
        Button button = new Button("Process");
        button.setOnAction(e -> label.setText(service.process("World")));

        VBox root = new VBox(12, label, button);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 480, 240);
        stage.setTitle("JavaFX + Spring + MongoDB (template)");
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
