package com.albionservant.gui;

import com.albionservant.data.ArtifactData;
import com.albionservant.data.CraftData;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
        VBox.setVgrow(scroll, Priority.ALWAYS);

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
            internalTopBar.setManaged(false);
            if (onDetailModeListener != null) onDetailModeListener.accept(true);
            String itemName = currentPath.get(currentPath.size() - 1);
            contentArea.getChildren().add(new CraftDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel));
            return;
        }

        internalTopBar.setVisible(true);
        internalTopBar.setManaged(true);
        if (onDetailModeListener != null) onDetailModeListener.accept(false);

        // ── ROOT STATE: show 3 big centered buttons, no tree columns ──
        if (currentPath.isEmpty()) {
            List<String> rootOptions = CraftData.getChildren("ROOT");

            VBox centerBox = new VBox(20);
            centerBox.setAlignment(Pos.CENTER);
            centerBox.setPadding(new Insets(60, 40, 60, 40));
            centerBox.setStyle("-fx-background-color: #f8f9fa;");
            VBox.setVgrow(centerBox, Priority.ALWAYS);

            Label prompt = new Label("Choose a category to get started");
            prompt.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555555;");

            HBox buttonsRow = new HBox(24);
            buttonsRow.setAlignment(Pos.CENTER);

            for (String option : rootOptions) {
                Button btn = new Button(option);
                btn.setPrefWidth(200);
                btn.setPrefHeight(70);
                btn.setStyle("""
                    -fx-background-color: #ef4444;
                    -fx-text-fill: #ffffff;
                    -fx-font-weight: bold;
                    -fx-font-size: 18px;
                    -fx-background-radius: 10;
                    """);
                btn.setOnMouseEntered(e -> btn.setStyle("""
                    -fx-background-color: #dc2626;
                    -fx-text-fill: #ffffff;
                    -fx-font-weight: bold;
                    -fx-font-size: 18px;
                    -fx-background-radius: 10;
                    -fx-cursor: hand;
                    """));
                btn.setOnMouseExited(e -> btn.setStyle("""
                    -fx-background-color: #ef4444;
                    -fx-text-fill: #ffffff;
                    -fx-font-weight: bold;
                    -fx-font-size: 18px;
                    -fx-background-radius: 10;
                    """));
                final String finalOption = option;
                btn.setOnAction(e -> {
                    currentPath.add(finalOption);
                    refreshUI();
                });
                buttonsRow.getChildren().add(btn);
            }

            centerBox.getChildren().addAll(prompt, buttonsRow);
            contentArea.getChildren().add(centerBox);
            return;
        }

        // ── TREE STATE: multi-column drill-down ──
        HBox levelsHBox = new HBox(20);
        levelsHBox.setAlignment(Pos.TOP_LEFT);
        levelsHBox.setPadding(new Insets(15));

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

    // ====================== DETAIL SUB-PANEL ======================
    private static class CraftDetailSubPanel extends VBox {

        private record Material(String name, String icon) {}
        private static final List<Material> MATERIALS = List.of(
                new Material("Metal Bars", "🛡️"),
                new Material("Planks", "📦")
        );

        public CraftDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack) {
            setPadding(new Insets(0));
            setSpacing(0);

            // ── FIXED RED HEADER ──
            HBox redHeader = new HBox(15);
            redHeader.setPadding(new Insets(15, 40, 15, 40));
            redHeader.setStyle("-fx-background-color: #ef4444;");
            redHeader.setAlignment(Pos.CENTER_LEFT);

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

            // ── SCROLLABLE WHITE CONTENT ──
            VBox whiteContent = new VBox(30);
            whiteContent.setPadding(new Insets(30, 40, 40, 40));
            whiteContent.setStyle("-fx-background-color: #ffffff;");
            whiteContent.setFillWidth(true);
            whiteContent.setMaxWidth(Double.MAX_VALUE);

            // ── TOP SECTION: 3 equal columns, stretches with window ──
            HBox topSection = new HBox(30);
            topSection.setAlignment(Pos.TOP_LEFT);
            topSection.setFillHeight(false);
            topSection.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(topSection, Priority.ALWAYS);

            // ── LEFT COLUMN: search bar + item icons (click = go back) ──
            VBox left = new VBox(12);
            left.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(left, Priority.ALWAYS);

            TextField searchBar = new TextField();
            searchBar.setPromptText("Search tiers or materials...");
            searchBar.setStyle("-fx-font-size: 14px;");
            searchBar.setMaxWidth(Double.MAX_VALUE);

            HBox iconRow = new HBox(16);
            iconRow.setAlignment(Pos.CENTER_LEFT);

            Label icon1 = new Label("🛡️");
            icon1.setStyle("-fx-font-size: 72px; -fx-cursor: hand;");
            icon1.setOnMouseClicked(e -> onBack.run());

            Label icon2 = new Label("📦");
            icon2.setStyle("-fx-font-size: 72px; -fx-cursor: hand;");
            icon2.setOnMouseClicked(e -> onBack.run());

            iconRow.getChildren().addAll(icon1, icon2);

            left.getChildren().addAll(searchBar, iconRow);

            // ── CENTER COLUMN: quantity, station fee, demand type, crafting focus, bonus craft, HO ──
            VBox center = new VBox(8);
            center.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(center, Priority.ALWAYS);

            Label quantityLbl = new Label("Quantity:");
            quantityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            TextField quantity = new TextField("200");
            quantity.setStyle("-fx-font-size: 14px;");
            quantity.setMaxWidth(Double.MAX_VALUE);

            Label stationFeeLbl = new Label("Station Fee:");
            stationFeeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            TextField stationFee = new TextField("999");
            stationFee.setStyle("-fx-font-size: 14px;");
            stationFee.setMaxWidth(Double.MAX_VALUE);

            Label demandTypeLbl = new Label("Demand Type:");
            demandTypeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> demandType = new ComboBox<>();
            demandType.getItems().addAll("24h", "7d", "4w");
            demandType.setValue("24h");
            demandType.setMaxWidth(Double.MAX_VALUE);

            CheckBox craftingFocus = new CheckBox("Crafting Focus");
            craftingFocus.setStyle("-fx-font-size: 13px;");

            Label bonusCraftLbl = new Label("Bonus Craft:");
            bonusCraftLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> bonusCraft = new ComboBox<>();
            bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
            bonusCraft.setValue("Royal City");
            bonusCraft.setMaxWidth(Double.MAX_VALUE);

            VBox hoSection = new VBox(6);
            hoSection.setVisible(false);
            hoSection.setManaged(false);

            Label hoQualityLbl = new Label("Hideout Quality:");
            hoQualityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> hoQuality = new ComboBox<>();
            hoQuality.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Q5", "Q6");
            hoQuality.setValue("Q5");
            hoQuality.setMaxWidth(Double.MAX_VALUE);

            Label hoPowerLbl = new Label("Hideout Power Level:");
            hoPowerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<Integer> hoPower = new ComboBox<>();
            hoPower.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9);
            hoPower.setValue(5);
            hoPower.setMaxWidth(Double.MAX_VALUE);

            hoSection.getChildren().addAll(hoQualityLbl, hoQuality, hoPowerLbl, hoPower);

            bonusCraft.setOnAction(e -> {
                boolean isHO = "HO".equals(bonusCraft.getValue());
                hoSection.setVisible(isHO);
                hoSection.setManaged(isHO);
            });

            center.getChildren().addAll(
                    quantityLbl, quantity,
                    stationFeeLbl, stationFee,
                    demandTypeLbl, demandType,
                    craftingFocus,
                    bonusCraftLbl, bonusCraft,
                    hoSection
            );

            // ── RIGHT COLUMN: material buy locations + sell location ──
            VBox right = new VBox(8);
            right.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(right, Priority.ALWAYS);

            Label buy1Lbl = new Label("Material-Buy1:");
            buy1Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy1 = createCityCombo(false);
            buy1.setMaxWidth(Double.MAX_VALUE);

            Label buy2Lbl = new Label("Material-Buy2:");
            buy2Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy2 = createCityCombo(false);
            buy2.setMaxWidth(Double.MAX_VALUE);

            Label buy3Lbl = new Label("Material-Buy3:");
            buy3Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy3 = createCityCombo(true);
            buy3.setMaxWidth(Double.MAX_VALUE);

            Label buy4Lbl = new Label("Material-Buy4:");
            buy4Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy4 = createCityCombo(true);
            buy4.setMaxWidth(Double.MAX_VALUE);

            Label sellLbl = new Label("Sell-Location:");
            sellLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> sellLocation = createSellCombo();
            sellLocation.setMaxWidth(Double.MAX_VALUE);

            right.getChildren().addAll(
                    buy1Lbl, buy1,
                    buy2Lbl, buy2,
                    buy3Lbl, buy3,
                    buy4Lbl, buy4,
                    sellLbl, sellLocation
            );

            topSection.getChildren().addAll(left, center, right);
            topSection.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(topSection, Priority.NEVER);

            // ── BOTTOM SUMMARY ──
            VBox bottom = new VBox(10);
            bottom.setStyle("-fx-background-color: #f1f3f5; -fx-padding: 20; -fx-background-radius: 8;");
            Label bottomTitle = new Label("Calculation results / summary will appear here");
            bottomTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");
            bottom.getChildren().add(bottomTitle);

            whiteContent.getChildren().addAll(topSection, buildTable(itemName), bottom);

            ScrollPane contentScroll = new ScrollPane(whiteContent);
            contentScroll.setFitToWidth(true);
            contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setStyle("-fx-background-color: white;");
            VBox.setVgrow(contentScroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, contentScroll);
        }

        private VBox buildTable(String itemName) {
            // Detect artifact type — drives whether material3 (and optionally material4) rows appear
            ArtifactData.ArtifactType artifactType = ArtifactData.getArtifactType(itemName);
            boolean hasArtifact = artifactType != null;

            // Standard tiers (all 25 rows including enchanted sub-tiers)
            String[] allTiers = {
                    "4.0","4.1","4.2","4.3","4.4",
                    "5.0","5.1","5.2","5.3","5.4",
                    "6.0","6.1","6.2","6.3","6.4",
                    "7.0","7.1","7.2","7.3","7.4",
                    "8.0","8.1","8.2","8.3","8.4"
            };

            // Artifact material tiers — MAJOR only, no enchanted sub-tiers
            String[] artifactTiers = {"4", "5", "6", "7", "8"};

            // Column layout:
            // col 0       : Tiers
            // col 1,2     : Material 1 → API | Manual
            // col 3,4     : Material 2 → API | Manual
            // col 5..12   : Demand, Costs, Focus Costs, Books, Fame, SPF, Profit, ROI
            int totalCols = 1 + (MATERIALS.size() * 2) + 8; // 13
            double colPct = 100.0 / totalCols;

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");
            grid.setVgap(4);
            grid.setHgap(0);

            for (int i = 0; i < totalCols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(colPct);
                cc.setHalignment(HPos.CENTER);
                cc.setFillWidth(true);
                grid.getColumnConstraints().add(cc);
            }

            String[] resultHeaders = {"Demand", "Costs", "Focus Costs", "Books", "Fame", "SPF", "Profit", "ROI"};

            // ── Header row 0: Tiers | MaterialName (colspan 2) | result headers ──
            grid.add(makeHeaderLabel("Tiers"), 0, 0);
            int matCol = 1;
            for (Material mat : MATERIALS) {
                Label matLbl = makeHeaderLabel(mat.icon() + " " + mat.name());
                GridPane.setColumnSpan(matLbl, 2);
                grid.add(matLbl, matCol, 0);
                matCol += 2;
            }
            for (int i = 0; i < resultHeaders.length; i++) {
                grid.add(makeHeaderLabel(resultHeaders[i]), matCol + i, 0);
            }

            // ── Header row 1: empty | API | Manual per material | empty result cols ──
            grid.add(new Label(""), 0, 1);
            matCol = 1;
            for (Material ignored : MATERIALS) {
                grid.add(makeSubHeaderLabel("API"),    matCol,     1);
                grid.add(makeSubHeaderLabel("Manual"), matCol + 1, 1);
                matCol += 2;
            }
            for (int i = 0; i < resultHeaders.length; i++) {
                grid.add(new Label(""), matCol + i, 1);
            }

            // ── Standard material rows (all tiers incl. enchanted sub-tiers) ──
            String[] resultValues = {"240", "620", "310", "8", "1250", "12", "850", "28%"};
            int gridRow = 2;
            for (String tier : allTiers) {
                grid.add(makeDataLabel(tier), 0, gridRow);
                matCol = 1;
                for (Material ignored : MATERIALS) {
                    TextField apiField = new TextField("120");
                    apiField.setEditable(false);
                    apiField.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: #f0f0f0;");
                    apiField.setMaxWidth(Double.MAX_VALUE);

                    TextField manualField = new TextField("");
                    manualField.setStyle("-fx-font-size: 12px; -fx-alignment: center;");
                    manualField.setMaxWidth(Double.MAX_VALUE);
                    manualField.textProperty().addListener((obs, old, newVal) -> {
                        if (newVal != null && !newVal.trim().isEmpty()) apiField.setText(newVal);
                    });
                    grid.add(apiField,    matCol,     gridRow);
                    grid.add(manualField, matCol + 1, gridRow);
                    matCol += 2;
                }
                for (int i = 0; i < resultValues.length; i++) {
                    grid.add(makeDataLabel(resultValues[i]), matCol + i, gridRow);
                }
                gridRow++;
            }

            VBox wrapper = new VBox(4);
            wrapper.setMaxWidth(Double.MAX_VALUE);
            wrapper.setFillWidth(true);
            wrapper.getChildren().add(grid);

            // ── Artifact material rows — only shown for artifact items ──
            if (hasArtifact) {
                // Separator label
                Label artifactSectionLabel = new Label("Artifact Material  —  " + artifactType.displayName
                        + "  (major tiers only, no enchanted sub-tiers)");
                artifactSectionLabel.setStyle(
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #ef4444;" +
                                "-fx-padding: 12 0 4 0;");

                // Material 3 — Artifact item (e.g. "Rune Artifact")
                GridPane artifactGrid = buildArtifactGrid(artifactTiers, artifactType.displayName, totalCols, colPct);

                wrapper.getChildren().addAll(artifactSectionLabel, artifactGrid);
            }

            return wrapper;
        }

        /** Builds the artifact-tier-only grid for material3 (no .1/.2/.3 rows). */
        private GridPane buildArtifactGrid(String[] tiers, String materialLabel,
                                           int totalCols, double colPct) {
            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setStyle("-fx-background-color: #fff3f3; -fx-padding: 10; -fx-background-radius: 8;" +
                    "-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-border-width: 1;");
            grid.setVgap(4);
            grid.setHgap(0);

            for (int i = 0; i < totalCols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(colPct);
                cc.setHalignment(HPos.CENTER);
                cc.setFillWidth(true);
                grid.getColumnConstraints().add(cc);
            }

            // Header: "Tier" | artifact label spanning mat columns | blanks for result cols
            Label tierHeader = makeHeaderLabel("Tier");
            grid.add(tierHeader, 0, 0);

            Label matHeader = makeHeaderLabel("🔮 " + materialLabel);
            GridPane.setColumnSpan(matHeader, MATERIALS.size() * 2);
            grid.add(matHeader, 1, 0);

            // sub-header: API | Manual
            grid.add(new Label(""), 0, 1);
            grid.add(makeSubHeaderLabel("API"),    1, 1);
            grid.add(makeSubHeaderLabel("Manual"), 2, 1);

            // Data rows — MAJOR tiers only
            int row = 2;
            for (String tier : tiers) {
                grid.add(makeDataLabel("T" + tier), 0, row);

                TextField apiField = new TextField("0");
                apiField.setEditable(false);
                apiField.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: #ffe4e4;");
                apiField.setMaxWidth(Double.MAX_VALUE);

                TextField manualField = new TextField("");
                manualField.setStyle("-fx-font-size: 12px; -fx-alignment: center;");
                manualField.setMaxWidth(Double.MAX_VALUE);
                manualField.textProperty().addListener((obs, old, newVal) -> {
                    if (newVal != null && !newVal.trim().isEmpty()) apiField.setText(newVal);
                });

                grid.add(apiField,    1, row);
                grid.add(manualField, 2, row);

                // Fill remaining result columns with blank labels for alignment
                int matColEnd = 1 + MATERIALS.size() * 2;
                for (int c = matColEnd; c < totalCols; c++) {
                    grid.add(new Label(""), c, row);
                }
                row++;
            }
            return grid;
        }

        private Label makeHeaderLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 13px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private Label makeSubHeaderLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #888888; -fx-font-size: 12px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private Label makeDataLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size: 13px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
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