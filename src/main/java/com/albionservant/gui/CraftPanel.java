package com.albionservant.gui;

import com.albionservant.data.CraftData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CraftPanel extends VBox {

    private final VBox contentArea = new VBox(15);
    private final HBox internalTopBar = new HBox(15);   // renamed for clarity
    private List<String> currentPath = new ArrayList<>();
    private Consumer<Boolean> onDetailModeListener;

    public CraftPanel() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #ef4444;");

        internalTopBar.setAlignment(Pos.CENTER_LEFT);
        internalTopBar.setPadding(new Insets(0, 0, 15, 0));

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

        internalTopBar.getChildren().addAll(breadcrumbLabel, spacer, backButton);


        contentArea.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 0;");

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent;");

        getChildren().addAll(internalTopBar, scroll);

        currentPath = new ArrayList<>();
        refreshUI();
    }

    public void setOnDetailModeListener(Consumer<Boolean> listener) {
        this.onDetailModeListener = listener;
    }

    private void refreshUI() {
        contentArea.getChildren().clear();

        String breadcrumbText = currentPath.isEmpty()
                ? "Main Categories"
                : String.join("  ►  ", currentPath);
        ((Label) internalTopBar.getChildren().get(0)).setText(breadcrumbText);

        boolean isLeaf = !currentPath.isEmpty() &&
                CraftData.getChildren(currentPath.get(currentPath.size() - 1)).isEmpty();

        if (isLeaf) {
            internalTopBar.setVisible(false);                                      // hide internal bar
            if (onDetailModeListener != null) onDetailModeListener.accept(true);   // hide main top nav
            String itemName = currentPath.get(currentPath.size() - 1);
            contentArea.getChildren().add(new CraftDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel));
            return;
        }

        internalTopBar.setVisible(true);
        if (onDetailModeListener != null) onDetailModeListener.accept(false);

        // === TREE MODE ===
        HBox levelsHBox = new HBox(20);
        levelsHBox.setAlignment(Pos.TOP_LEFT);

        int pathSize = currentPath.size();

        for (int level = 0; level <= pathSize; level++) {
            boolean isNextColumn = (level == pathSize);
            String parentKey = (level == 0) ? "ROOT" : currentPath.get(level - 1);
            List<String> options = CraftData.getChildren(parentKey);
            String selected = isNextColumn ? null : currentPath.get(level);

            VBox column = new VBox(8);
            column.setStyle("""
                -fx-padding: 12;
                -fx-background-color: #f1f3f5;
                -fx-background-radius: 10;
                -fx-min-width: 170;
                """);

            String headerText = isNextColumn
                    ? (currentPath.isEmpty() ? "Choose your starting category" : currentPath.get(currentPath.size() - 1) + " → next")
                    : (level == 0 ? "Main Categories" : currentPath.get(level - 1));
            Label columnHeader = new Label(headerText);
            columnHeader.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; -fx-font-weight: bold;");
            column.getChildren().add(columnHeader);

            for (String option : options) {
                boolean isSelected = !isNextColumn && option.equals(selected);
                Button btn = createButton(option, isSelected, !isNextColumn);

                btn.setPrefWidth(155);
                btn.setMaxWidth(Double.MAX_VALUE);

                final int finalLevel = level;
                final String finalOption = option;

                if (isNextColumn) {
                    btn.setOnAction(e -> {
                        currentPath.add(finalOption);
                        refreshUI();
                    });
                } else {
                    btn.setOnAction(e -> {
                        if (!isSelected) changeSelectionAtLevel(finalLevel, finalOption);
                    });
                }
                column.getChildren().add(btn);
            }

            if (isNextColumn && options.isEmpty()) {
                Label emptyMsg = new Label("No further sub-categories");
                emptyMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-padding: 20 0 10 0;");
                column.getChildren().add(emptyMsg);
            }

            levelsHBox.getChildren().add(column);
        }
        contentArea.getChildren().add(levelsHBox);
    }

    private static class CraftDetailSubPanel extends VBox {
        public CraftDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack) {
            setPadding(new Insets(0));
            setSpacing(0);

            HBox redHeader = new HBox(15);
            redHeader.setPadding(new Insets(15, 40, 15, 40));
            redHeader.setStyle("-fx-background-color: #ef4444;");

            Label breadcrumb = new Label(breadcrumbText);
            breadcrumb.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button backBtn = new Button("← Back");
            backBtn.setStyle("""
                -fx-background-color: #4ade80;
                -fx-text-fill: #111;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                -fx-padding: 8 24;
                """);
            backBtn.setOnAction(e -> onBack.run());

            redHeader.getChildren().addAll(breadcrumb, spacer, backBtn);

            VBox whiteContent = new VBox(25);
            whiteContent.setPadding(new Insets(25, 40, 40, 40));   // minimal top padding
            whiteContent.setStyle("-fx-background-color: #ffffff;");


            HBox titleRow = new HBox(20);
            titleRow.setAlignment(Pos.CENTER_LEFT);

            Label itemLabel = new Label(itemName);
            itemLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #111;");

            Label bonusLabel = new Label("Bonus Crafting City : Martlock");
            bonusLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666666;");

            Region titleSpacer = new Region();
            HBox.setHgrow(titleSpacer, Priority.ALWAYS);

            Button smallArrow = new Button("↩");
            smallArrow.setStyle("-fx-font-size: 28px; -fx-background-color: transparent; -fx-text-fill: #ef4444;");
            smallArrow.setOnAction(e -> onBack.run());

            titleRow.getChildren().addAll(itemLabel, bonusLabel, titleSpacer, smallArrow);


            HBox iconStats = new HBox(40);
            iconStats.setAlignment(Pos.TOP_LEFT);

            HBox icons = new HBox(15);
            Label shield = new Label("🛡️");
            shield.setStyle("-fx-font-size: 120px; -fx-text-fill: #333;");
            Label square = new Label("⬛");
            square.setStyle("-fx-font-size: 120px; -fx-text-fill: #222;");
            Label blackSquare = new Label("⬛");
            blackSquare.setStyle("-fx-font-size: 120px; -fx-text-fill: #111;");
            icons.getChildren().addAll(shield, square, blackSquare);

            VBox stats = new VBox(12);
            stats.getChildren().addAll(
                    createStatRow("Quantity:", "200"),
                    createStatRow("Station Fee:", "999"),
                    createStatRow("Crafting Focus:", "Y/N"),
                    createStatRow("Bonus Craft:", "HO"),
                    createStatRow("HO Zone:", "2 HO Quality : 5"),
                    createStatRow("Demand AVG:", "24h")
            );

            iconStats.getChildren().addAll(icons, stats);

            VBox matsBox = new VBox(12);
            Label matsTitle = new Label("Required mats");
            matsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111;");

            HBox matsRow = new HBox(15);
            matsRow.setAlignment(Pos.CENTER_LEFT);

            Label metal = new Label("Metal Bars : XYZ");
            metal.setStyle("-fx-font-size: 16px; -fx-text-fill: #111;");

            Label buyFrom = new Label("Buy Mats From :");
            ComboBox<String> buyCombo = new ComboBox<>();
            buyCombo.getItems().addAll("Thetford", "Martlock", "Bridgewatch", "Lymhurst");
            buyCombo.setValue("Thetford");

            Label sellIn = new Label("Sell Items In :");
            ComboBox<String> sellCombo = new ComboBox<>();
            sellCombo.getItems().addAll("Thetford", "Martlock", "Bridgewatch", "Black Market");
            sellCombo.setValue("Thetford");

            matsRow.getChildren().addAll(metal, buyFrom, buyCombo, sellIn, sellCombo);
            matsBox.getChildren().addAll(matsTitle, matsRow);

            VBox tableBox = new VBox(4);
            tableBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

            HBox headerRow = new HBox(12);
            String[] headers = {"Tiers", "API", "Manual", "Demand", "Costs", "Focus Costs", "Books", "Fame", "SPF", "Profit", "ROI"};
            for (String h : headers) {
                Label l = new Label(h);
                l.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-pref-width: 78;");
                headerRow.getChildren().add(l);
            }
            tableBox.getChildren().add(headerRow);

            String[][] data = {
                    {"4.0", "100", "120", "50", "300", "200", "5", "1000", "10", "500", "25%"},
                    {"4.1", "105", "125", "55", "310", "210", "6", "1100", "11", "520", "26%"},
                    {"4.2", "110", "130", "60", "320", "220", "7", "1200", "12", "540", "27%"},
                    {"4.3", "115", "135", "65", "330", "230", "8", "1300", "13", "560", "28%"},
                    {"5.0", "150", "180", "80", "400", "300", "10", "1500", "15", "800", "40%"}
            };
            for (String[] rowData : data) {
                HBox row = new HBox(12);
                for (String val : rowData) {
                    Label cell = new Label(val);
                    cell.setStyle("-fx-font-size: 14px; -fx-pref-width: 78;");
                    row.getChildren().add(cell);
                }
                tableBox.getChildren().add(row);
            }

            whiteContent.getChildren().addAll(titleRow, iconStats, matsBox, tableBox);
            getChildren().addAll(redHeader, whiteContent);
        }

        private HBox createStatRow(String label, String value) {
            HBox row = new HBox(15);
            Label l = new Label(label);
            l.setStyle("-fx-font-size: 15px; -fx-text-fill: #555; -fx-pref-width: 160;");
            Label v = new Label(value);
            v.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111;");
            row.getChildren().addAll(l, v);
            return row;
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
            btn.setStyle("-fx-background-color: #555555; -fx-text-fill: #aaaaaa; -fx-font-size: 14px; -fx-opacity: 0.85;");
        } else {
            btn.setStyle("-fx-background-color: #86efac; -fx-text-fill: #111; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
        return btn;
    }
}