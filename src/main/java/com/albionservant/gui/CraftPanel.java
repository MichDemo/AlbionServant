package com.albionservant.gui;

import com.albionservant.data.CraftData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CraftPanel extends VBox {

    private final VBox contentArea = new VBox(15);
    private final HBox internalTopBar = new HBox(15);
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
            internalTopBar.setVisible(false);
            if (onDetailModeListener != null) onDetailModeListener.accept(true);
            String itemName = currentPath.get(currentPath.size() - 1);
            contentArea.getChildren().add(new CraftDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel));
            return;
        }

        internalTopBar.setVisible(true);
        if (onDetailModeListener != null) onDetailModeListener.accept(false);

        // Tree mode
        HBox levelsHBox = new HBox(20);
        levelsHBox.setAlignment(Pos.TOP_LEFT);

        int pathSize = currentPath.size();
        for (int level = 0; level <= pathSize; level++) {
            boolean isNextColumn = (level == pathSize);
            String parentKey = (level == 0) ? "ROOT" : currentPath.get(level - 1);
            List<String> options = CraftData.getChildren(parentKey);
            String selected = isNextColumn ? null : currentPath.get(level);

            VBox column = new VBox(8);
            column.setStyle("-fx-padding: 12; -fx-background-color: #f1f3f5; -fx-background-radius: 10; -fx-min-width: 170;");

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

    // ====================== FINAL CLEAN TABLE ======================
    private static class CraftDetailSubPanel extends VBox {

        private record Material(String name, String icon) {}
        private static final List<Material> MATERIALS = List.of(
                new Material("Metal Bars", "🛡️"),
                new Material("Planks", "📦")
        );

        public CraftDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack) {
            setPadding(new Insets(0));
            setSpacing(0);

            // FIXED RED HEADER
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

            // SCROLLABLE WHITE CONTENT
            VBox whiteContent = new VBox(30);
            whiteContent.setPadding(new Insets(30, 40, 40, 40));
            whiteContent.setStyle("-fx-background-color: #ffffff;");

            // TOP PART (unchanged)
            HBox topSection = new HBox(40);
            topSection.setAlignment(Pos.TOP_LEFT);

            VBox left = new VBox(12);
            left.setPrefWidth(320);

            TextField searchBar = new TextField();
            searchBar.setPromptText("Search tiers or materials...");
            searchBar.setStyle("-fx-font-size: 14px;");

            Label iconLabel = new Label("🛡️");
            iconLabel.setStyle("-fx-font-size: 110px; -fx-text-fill: #333;");
            iconLabel.setOnMouseClicked(e -> onBack.run());

            TextField quantity = new TextField("200");
            TextField stationFee = new TextField("999");

            ComboBox<String> demandType = new ComboBox<>();
            demandType.getItems().addAll("24h", "7d", "4w");
            demandType.setValue("24h");

            CheckBox craftingFocus = new CheckBox("Crafting Focus");

            left.getChildren().addAll(searchBar, iconLabel,
                    new Label("Quantity:"), quantity,
                    new Label("Station Fee:"), stationFee,
                    new Label("Demand Type:"), demandType,
                    craftingFocus);

            VBox center = new VBox(12);
            center.setPrefWidth(320);

            ComboBox<String> bonusCraft = new ComboBox<>();
            bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
            bonusCraft.setValue("Royal City");

            VBox hoSection = new VBox(8);
            hoSection.setVisible(false);
            hoSection.setManaged(false);

            ComboBox<String> hoQuality = new ComboBox<>();
            hoQuality.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Q5", "Q6");
            hoQuality.setValue("Q5");

            ComboBox<Integer> hoPower = new ComboBox<>();
            hoPower.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9);
            hoPower.setValue(5);

            hoSection.getChildren().addAll(new Label("Hideout Quality:"), hoQuality,
                    new Label("Hideout Power Level:"), hoPower);

            bonusCraft.setOnAction(e -> {
                boolean isHO = "HO".equals(bonusCraft.getValue());
                hoSection.setVisible(isHO);
                hoSection.setManaged(isHO);
            });

            center.getChildren().addAll(new Label("Bonus Craft:"), bonusCraft, hoSection);

            VBox right = new VBox(12);
            right.setPrefWidth(340);

            ComboBox<String> buy1 = createCityCombo(false);
            ComboBox<String> buy2 = createCityCombo(false);
            ComboBox<String> buy3 = createCityCombo(true);
            ComboBox<String> buy4 = createCityCombo(true);
            ComboBox<String> sellLocation = createSellCombo();

            right.getChildren().addAll(
                    new Label("Material-Buy1:"), buy1,
                    new Label("Material-Buy2:"), buy2,
                    new Label("Material-Buy3:"), buy3,
                    new Label("Material-Buy4:"), buy4,
                    new Label("Sell-Location:"), sellLocation);

            topSection.getChildren().addAll(left, center, right);

            // ====================== PERFECTLY ALIGNED TABLE ======================
            VBox tableBox = new VBox(4);
            tableBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

            // Row 1: Tiers + Material names (spanning 2 columns)
            HBox headerRow1 = new HBox(8);
            headerRow1.setAlignment(Pos.CENTER_LEFT);

            addHeaderCell(headerRow1, "Tiers", 85);

            for (Material mat : MATERIALS) {
                Label matHeader = new Label(mat.icon() + " " + mat.name());
                matHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 13px; -fx-pref-width: 170; -fx-alignment: center;");
                headerRow1.getChildren().add(matHeader);
            }

            String[] resultHeaders = {"Demand", "Costs", "Focus Costs", "Books", "Fame", "SPF", "Profit", "ROI"};
            for (String h : resultHeaders) {
                addHeaderCell(headerRow1, h, 85);
            }
            tableBox.getChildren().add(headerRow1);

            // Row 2: API | Manual under each material
            HBox headerRow2 = new HBox(8);
            headerRow2.setAlignment(Pos.CENTER_LEFT);

            // Empty space under Tiers
            Region empty = new Region();
            empty.setPrefWidth(85);
            headerRow2.getChildren().add(empty);

            for (Material ignored : MATERIALS) {
                addHeaderCell(headerRow2, "API", 85);
                addHeaderCell(headerRow2, "Manual", 85);
            }
            tableBox.getChildren().add(headerRow2);

            // Data rows
            String[] tiers = {"4.0","4.1","4.2","4.3","4.4","5.0","5.1","5.2","5.3","5.4","6.0","6.1","6.2","6.3","6.4","7.0","7.1","7.2","7.3","7.4","8.0","8.1","8.2","8.3","8.4"};
            for (String tier : tiers) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);

                addCell(row, tier);

                for (Material ignored : MATERIALS) {
                    TextField apiField = new TextField("120");
                    apiField.setEditable(false);
                    apiField.setStyle("-fx-font-size: 14px; -fx-pref-width: 85; -fx-alignment: center; -fx-background-color: #f0f0f0;");

                    TextField manualField = new TextField("");
                    manualField.setStyle("-fx-font-size: 14px; -fx-pref-width: 85; -fx-alignment: center;");

                    manualField.textProperty().addListener((obs, old, newVal) -> {
                        if (newVal != null && !newVal.trim().isEmpty()) {
                            apiField.setText(newVal);
                        }
                    });

                    row.getChildren().addAll(apiField, manualField);
                }

                addCell(row, "240");
                addCell(row, "620");
                addCell(row, "310");
                addCell(row, "8");
                addCell(row, "1250");
                addCell(row, "12");
                addCell(row, "850");
                addCell(row, "28%");

                tableBox.getChildren().add(row);
            }

            // BOTTOM
            VBox bottom = new VBox(10);
            bottom.setStyle("-fx-background-color: #f1f3f5; -fx-padding: 20; -fx-background-radius: 8;");
            Label bottomTitle = new Label("Calculation results / summary will appear here");
            bottomTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");
            bottom.getChildren().add(bottomTitle);

            whiteContent.getChildren().addAll(topSection, tableBox, bottom);
            getChildren().addAll(redHeader, whiteContent);
        }

        private void addHeaderCell(HBox row, String text, double width) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 13px; -fx-pref-width: " + width + "; -fx-alignment: center;");
            row.getChildren().add(l);
        }

        private void addCell(HBox row, String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size: 14px; -fx-pref-width: 85; -fx-alignment: center;");
            row.getChildren().add(l);
        }

        private ComboBox<String> createCityCombo(boolean withMedian) {
            ComboBox<String> cb = new ComboBox<>();
            cb.getItems().addAll("Bridgewatch", "Martlock", "Thetford", "Fort Sterling", "Lymhurst", "Caerleon", "Brecilien");
            if (withMedian) cb.getItems().add("Median");
            cb.setValue("Martlock");
            return cb;
        }

        private ComboBox<String> createSellCombo() {
            ComboBox<String> cb = new ComboBox<>();
            cb.getItems().addAll("Bridgewatch", "Martlock", "Thetford", "Fort Sterling", "Lymhurst", "Caerleon", "Brecilien", "Blackmarket");
            cb.setValue("Blackmarket");
            return cb;
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