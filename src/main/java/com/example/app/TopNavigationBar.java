package com.example.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TopNavigationBar extends VBox {

    private Button activeButton = null;

    public TopNavigationBar() {
        // Main container
        setBackground(new Background(new BackgroundFill(AppConfig.BACKGROUND_TAB_BAR, null, null)));
        setPadding(new Insets(8, 20, 8, 20));
        setSpacing(6);

        buildTopLabel();
        buildTabButtons();
    }

    private void buildTopLabel() {
        Label screenTitle = new Label("Ekran Główny");
        screenTitle.setFont(Font.font("System", FontWeight.BOLD, 15));
        screenTitle.setTextFill(AppConfig.TEXT_PRIMARY);
        screenTitle.setPadding(new Insets(0, 0, 4, 8));

        getChildren().add(screenTitle);
    }

    private void buildTabButtons() {
        HBox tabContainer = new HBox(10);
        tabContainer.setAlignment(Pos.CENTER_LEFT);

        Button craftBtn   = createTabButton("CRAFT");
        Button refineBtn  = createTabButton("REFINE");
        Button specsBtn   = createTabButton("SPECS");
        Button optionsBtn = createTabButton("OPTIONS");

        // Click handlers
        craftBtn.setOnAction(e -> setActive(craftBtn));
        refineBtn.setOnAction(e -> setActive(refineBtn));
        specsBtn.setOnAction(e -> setActive(specsBtn));
        optionsBtn.setOnAction(e -> setActive(optionsBtn));

        tabContainer.getChildren().addAll(craftBtn, refineBtn, specsBtn, optionsBtn);

        getChildren().add(tabContainer);

        // Activate first tab by default
        setActive(craftBtn);
    }

    private Button createTabButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(140, 46);
        btn.setFont(Font.font("System", FontWeight.BOLD, 16));
        btn.setTextFill(AppConfig.TAB_TEXT);

        // Default inactive style
        btn.setStyle("-fx-background-color: " + toRgb(AppConfig.TAB_INACTIVE) +
                "; -fx-background-radius: 8;");

        return btn;
    }

    private void setActive(Button btn) {
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: " + toRgb(AppConfig.TAB_INACTIVE) +
                    "; -fx-background-radius: 8;");
        }

        btn.setStyle("-fx-background-color: " + toRgb(AppConfig.TAB_ACTIVE) +
                "; -fx-background-radius: 8;");

        activeButton = btn;
    }

    private String toRgb(Color c) {
        return String.format("rgb(%d,%d,%d)",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }
}