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
    private static final double TOP_BAR_HEIGHT = 96.0;
    private static final double TAB_BUTTON_HEIGHT = 26.0;


    private Button activeButton = null;

    public TopNavigationBar() {
        setMinHeight(TOP_BAR_HEIGHT);
        setPrefHeight(TOP_BAR_HEIGHT);
        setMaxHeight(TOP_BAR_HEIGHT);

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
        tabsContainer.setMinHeight(TAB_BUTTON_HEIGHT);
        tabsContainer.setPrefHeight(TAB_BUTTON_HEIGHT);
        tabsContainer.setMaxHeight(TAB_BUTTON_HEIGHT);
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
        btn.setMinHeight(TAB_BUTTON_HEIGHT);
        btn.setPrefHeight(TAB_BUTTON_HEIGHT);
        btn.setMaxHeight(TAB_BUTTON_HEIGHT);
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

    public void setOnRefineClicked(Runnable action) {
        HBox tabs = (HBox) getChildren().get(1);
        Button refineBtn = (Button) tabs.getChildren().get(1);
        refineBtn.setOnAction(e -> {
            setActiveButton(refineBtn);
            action.run();
        });
    }

    public void setOnSpecsClicked(Runnable action) {
        HBox tabs = (HBox) getChildren().get(1);
        Button specsBtn = (Button) tabs.getChildren().get(2);
        specsBtn.setOnAction(e -> {
            setActiveButton(specsBtn);
            action.run();
        });
    }

    public void setOnOtherTabClicked(Runnable action) {
        HBox tabs = (HBox) getChildren().get(1);
        // Skip 0=CRAFT, 1=REFINE, 2=SPECS — those have dedicated callbacks
        for (int i = 3; i < tabs.getChildren().size(); i++) {
            Button btn = (Button) tabs.getChildren().get(i);
            final Button b = btn;
            btn.setOnAction(e -> {
                setActiveButton(b);
                action.run();
            });
        }
    }


}