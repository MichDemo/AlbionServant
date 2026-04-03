package com.albionservant.gui;

import com.albionservant.AppConfig;
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
        setBackground(new Background(new BackgroundFill(AppConfig.BACKGROUND_TAB_BAR, null, null)));
        setPadding(new Insets(10, 20, 10, 20));
        setSpacing(8);

        buildHeaderLabel();
        buildTabButtons();
    }

    private void buildHeaderLabel() {
        Label title = new Label("Ekran Główny");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(AppConfig.TEXT_SECONDARY);
        title.setPadding(new Insets(0, 0, 4, 4));

        getChildren().add(title);
    }

    private void buildTabButtons() {
        HBox tabsContainer = new HBox(12);
        tabsContainer.setAlignment(Pos.CENTER);           // Changed to CENTER
        tabsContainer.setPadding(new Insets(0, 10, 0, 10));

        Button craftBtn   = createTabButton("CRAFT");
        Button refineBtn  = createTabButton("REFINE");
        Button specsBtn   = createTabButton("SPECS");
        Button optionsBtn = createTabButton("OPTIONS");

        // Make buttons grow equally and fill available space
        HBox.setHgrow(craftBtn,   Priority.ALWAYS);
        HBox.setHgrow(refineBtn,  Priority.ALWAYS);
        HBox.setHgrow(specsBtn,   Priority.ALWAYS);
        HBox.setHgrow(optionsBtn, Priority.ALWAYS);

        craftBtn.setOnAction(e -> setActiveButton(craftBtn));
        refineBtn.setOnAction(e -> setActiveButton(refineBtn));
        specsBtn.setOnAction(e -> setActiveButton(specsBtn));
        optionsBtn.setOnAction(e -> setActiveButton(optionsBtn));

        tabsContainer.getChildren().addAll(craftBtn, refineBtn, specsBtn, optionsBtn);

        getChildren().add(tabsContainer);
    }

    private Button createTabButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(48);
        btn.setMaxWidth(Double.MAX_VALUE);     // Important: allow growing
        btn.setFont(Font.font("System", FontWeight.BOLD, 16));
        btn.setTextFill(AppConfig.TAB_TEXT);

        btn.setStyle("-fx-background-color: " + toRgbString(AppConfig.TAB_INACTIVE) +
                "; -fx-background-radius: 8;" +
                "-fx-padding: 0 20;");

        return btn;
    }

    private void setActiveButton(Button newActive) {
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: " + toRgbString(AppConfig.TAB_INACTIVE) +
                    "; -fx-background-radius: 8;" +
                    "-fx-padding: 0 20;");
        }

        newActive.setStyle("-fx-background-color: " + toRgbString(AppConfig.TAB_ACTIVE) +
                "; -fx-background-radius: 8;" +
                "-fx-padding: 0 20;");

        activeButton = newActive;
    }

    private String toRgbString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }

    public void setOnCraftClicked(Runnable action) {
        HBox tabs = (HBox) getChildren().get(1);
        Button craftBtn = (Button) tabs.getChildren().get(0);
        craftBtn.setOnAction(e -> action.run());
    }

    public void setOnOtherTabClicked(Runnable action) {
        HBox tabs = (HBox) getChildren().get(1);
        for (int i = 1; i < tabs.getChildren().size(); i++) {
            Button btn = (Button) tabs.getChildren().get(i);
            btn.setOnAction(e -> action.run());
        }
    }
}