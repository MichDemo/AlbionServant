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
        setPadding(new Insets(12, 20, 12, 20));
        setSpacing(8);

        buildHeaderLabel();
        buildTabButtons();
    }

    private void buildHeaderLabel() {
        Label title = new Label("Ekran Główny");
        title.setFont(Font.font("System", FontWeight.BOLD, 15));
        title.setTextFill(AppConfig.TEXT_SECONDARY);
        title.setPadding(new Insets(0, 0, 4, 2));
        getChildren().add(title);
    }

    private void buildTabButtons() {
        HBox tabsContainer = new HBox(10);
        tabsContainer.setAlignment(Pos.CENTER);
        tabsContainer.setPadding(new Insets(0, 4, 0, 4));

        Button craftBtn   = createTabButton("CRAFT");
        Button refineBtn  = createTabButton("REFINE");
        Button specsBtn   = createTabButton("SPECS");
        Button optionsBtn = createTabButton("OPTIONS");

        HBox.setHgrow(craftBtn,   Priority.ALWAYS);
        HBox.setHgrow(refineBtn,  Priority.ALWAYS);
        HBox.setHgrow(specsBtn,   Priority.ALWAYS);
        HBox.setHgrow(optionsBtn, Priority.ALWAYS);

        craftBtn.setOnAction(e   -> setActiveButton(craftBtn));
        refineBtn.setOnAction(e  -> setActiveButton(refineBtn));
        specsBtn.setOnAction(e   -> setActiveButton(specsBtn));
        optionsBtn.setOnAction(e -> setActiveButton(optionsBtn));

        tabsContainer.getChildren().addAll(craftBtn, refineBtn, specsBtn, optionsBtn);
        getChildren().add(tabsContainer);
    }

    private Button createTabButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(44);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setFont(Font.font("System", FontWeight.BOLD, 14));
        btn.setTextFill(AppConfig.TAB_TEXT);
        applyInactiveStyle(btn);

        btn.setOnMouseEntered(e -> {
            if (btn != activeButton) applyHoverStyle(btn);
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeButton) applyInactiveStyle(btn);
        });

        return btn;
    }

    private void setActiveButton(Button newActive) {
        if (activeButton != null) applyInactiveStyle(activeButton);
        applyActiveStyle(newActive);
        activeButton = newActive;
    }

    private void applyInactiveStyle(Button btn) {
        btn.setStyle(
                "-fx-background-color: " + toHex(AppConfig.TAB_INACTIVE) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 20;"
        );
    }

    private void applyActiveStyle(Button btn) {
        btn.setStyle(
                "-fx-background-color: " + toHex(AppConfig.TAB_ACTIVE) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 20;"
        );
    }

    private void applyHoverStyle(Button btn) {
        btn.setStyle(
                "-fx-background-color: " + toHex(AppConfig.BRAND_RED_DARK) + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 20;" +
                        "-fx-cursor: hand;"
        );
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int)(color.getRed()   * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue()  * 255));
    }

    public void setOnCraftClicked(Runnable action) {
        HBox tabs = (HBox) getChildren().get(1);
        Button craftBtn = (Button) tabs.getChildren().get(0);
        craftBtn.setOnAction(e -> {
            setActiveButton(craftBtn);
            action.run();
        });
    }

    public void setOnOtherTabClicked(Runnable action) {
        HBox tabs = (HBox) getChildren().get(1);
        for (int i = 1; i < tabs.getChildren().size(); i++) {
            Button btn = (Button) tabs.getChildren().get(i);
            final Button b = btn;
            btn.setOnAction(e -> {
                setActiveButton(b);
                action.run();
            });
        }
    }
}