package com.albionservant.gui;

import com.albionservant.data.CraftData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CraftPanel extends VBox {

    private final VBox navigationVBox = new VBox(15);
    private final VBox mainContentVBox = new VBox(15);
    private final HBox topBar = new HBox(15);
    private List<String> currentPath = new ArrayList<>();

    public CraftPanel() {
        // Main red panel
        setMaxWidth(1150);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(25));
        setStyle("""
            -fx-background-color: #ef4444;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 4);
            """);

        // === TOP BAR (breadcrumb + Back button) ===
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 15, 0));

        // Breadcrumb will be updated in refreshUI
        Label breadcrumbLabel = new Label();
        breadcrumbLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        HBox.setHgrow(breadcrumbLabel, Priority.ALWAYS);

        Button backButton = new Button("← Back");
        backButton.setStyle("""
            -fx-background-color: #4ade80;
            -fx-text-fill: #111;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-padding: 8 20;
            """);
        backButton.setOnAction(e -> goBackOneLevel());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(breadcrumbLabel, spacer, backButton);

        // Inner layout: left nav + right content
        HBox innerLayout = new HBox(25);
        innerLayout.setAlignment(Pos.TOP_LEFT);

        // LEFT NAVIGATION - side-by-side columns
        navigationVBox.setStyle("""
            -fx-background-color: #f8f9fa;
            -fx-padding: 20;
            -fx-background-radius: 12;
            -fx-border-color: #e5e7eb;
            -fx-border-width: 2;
            -fx-border-radius: 12;
            """);
        ScrollPane navScroll = new ScrollPane(navigationVBox);
        navScroll.setFitToWidth(true);
        navScroll.setPrefWidth(650);
        navScroll.setMaxHeight(750);

        // RIGHT MAIN CONTENT
        mainContentVBox.setPrefWidth(460);

        innerLayout.getChildren().addAll(navScroll, mainContentVBox);

        getChildren().addAll(topBar, innerLayout);

        // Start with Gear selected
        currentPath = new ArrayList<>(List.of("Gear"));
        refreshUI();
    }

    private void refreshUI() {
        navigationVBox.getChildren().clear();
        mainContentVBox.getChildren().clear();

        // Update top breadcrumb
        String breadcrumbText = currentPath.isEmpty() ? "Main Categories" : String.join("  ►  ", currentPath);
        ((Label) topBar.getChildren().get(0)).setText(breadcrumbText);

        // Show/hide Back button
        topBar.getChildren().get(2).setVisible(currentPath.size() > 1);

        // === LEFT SIDE: SIDE-BY-SIDE COLUMNS ===
        HBox levelsHBox = new HBox(15);
        levelsHBox.setAlignment(Pos.TOP_LEFT);

        for (int level = 0; level < currentPath.size(); level++) {
            String selected = currentPath.get(level);
            String parentKey = (level == 0) ? "ROOT" : currentPath.get(level - 1);
            List<String> siblings = CraftData.getChildren(parentKey);

            VBox column = new VBox(8);
            for (String option : siblings) {
                boolean isSelected = option.equals(selected);
                Button btn = createButton(option, isSelected, true);

                btn.setPrefWidth(155);
                btn.setMaxWidth(Double.MAX_VALUE);

                final int finalLevel = level;
                final String finalOption = option;
                btn.setOnAction(e -> {
                    if (!isSelected) {
                        changeSelectionAtLevel(finalLevel, finalOption);
                    }
                });
                column.getChildren().add(btn);
            }
            levelsHBox.getChildren().add(column);
        }
        navigationVBox.getChildren().add(levelsHBox);

        // === RIGHT SIDE ===
        List<String> currentOptions = currentPath.isEmpty()
                ? CraftData.getChildren("ROOT")
                : CraftData.getChildren(currentPath.get(currentPath.size() - 1));

        if (currentOptions.isEmpty()) {
            // === EMPTY PANEL AT FINAL SUB-SUB LEVEL ===
            VBox emptyPanel = new VBox(30);
            emptyPanel.setAlignment(Pos.CENTER);
            emptyPanel.setStyle("""
                -fx-background-color: rgba(255,255,255,0.12);
                -fx-padding: 60;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: rgba(255,255,255,0.2);
                """);

            Label msg = new Label("No items yet");
            msg.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            Label subMsg = new Label("Crafting recipes / items will appear here.");
            subMsg.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff;");

            Button returnBtn = new Button("← Return to choice tree");
            returnBtn.setStyle("""
                -fx-background-color: #4ade80;
                -fx-text-fill: #111;
                -fx-font-weight: bold;
                -fx-font-size: 16px;
                -fx-padding: 12 32;
                """);
            returnBtn.setOnAction(e -> goBackOneLevel());

            emptyPanel.getChildren().addAll(msg, subMsg, returnBtn);
            mainContentVBox.getChildren().add(emptyPanel);

        } else {
            // Normal level with choices
            Label choose = new Label("Choose one:");
            choose.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff;");
            mainContentVBox.getChildren().add(choose);

            FlowPane optionsPane = new FlowPane(15, 15);
            optionsPane.setPrefWrapLength(440);

            for (String option : currentOptions) {
                Button btn = createButton(option, false, false);
                btn.setOnAction(e -> {
                    currentPath.add(option);
                    refreshUI();
                });
                optionsPane.getChildren().add(btn);
            }
            mainContentVBox.getChildren().add(optionsPane);
        }
    }

    private void goBackOneLevel() {
        if (!currentPath.isEmpty()) {
            currentPath.remove(currentPath.size() - 1);
            refreshUI();
        }
    }

    private void changeSelectionAtLevel(int level, String newSelection) {
        List<String> newPath = new ArrayList<>(currentPath.subList(0, level));
        newPath.add(newSelection);
        currentPath = newPath;
        refreshUI();
    }

    private Button createButton(String text, boolean isSelected, boolean isSidePanel) {
        Button btn = new Button(text);
        btn.setPrefHeight(58);
        btn.setMinWidth(145);

        if (isSelected) {
            btn.setStyle("-fx-background-color: #4ade80; -fx-text-fill: #111; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else if (isSidePanel) {
            btn.setStyle("-fx-background-color: #555555; -fx-text-fill: #cccccc; -fx-font-size: 14px;");
        } else {
            btn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: #111; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
        return btn;
    }
}