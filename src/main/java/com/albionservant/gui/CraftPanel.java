package com.albionservant.gui;

import com.albionservant.data.ArtifactData;
import com.albionservant.data.CraftData;
import com.albionservant.data.CraftMaterialData;
import com.albionservant.data.FoodRecipeData;
import com.albionservant.data.FoodRecipeData.Ingredient;
import com.albionservant.data.PotionRecipeData;
import com.albionservant.data.ItemRenderData;
import com.albionservant.AppConfig;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
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
        internalTopBar.setPadding(new Insets(14, 20, 14, 20));

        Label breadcrumbLabel = new Label();
        breadcrumbLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        HBox.setHgrow(breadcrumbLabel, Priority.ALWAYS);

        Button backButton = new Button("← Back");
        backButton.setStyle(AppConfig.BTN_SECONDARY);
        backButton.setOnMouseEntered(e -> backButton.setStyle(AppConfig.BTN_SECONDARY_HOVER));
        backButton.setOnMouseExited(e  -> backButton.setStyle(AppConfig.BTN_SECONDARY));
        backButton.setOnAction(e -> goBackOneLevel());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        internalTopBar.getChildren().addAll(breadcrumbLabel, spacer, backButton);

        contentArea.setStyle("-fx-background-color: #f1f5f9;");
        contentArea.setPadding(new Insets(0));

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
            boolean isFoodItem   = currentPath.contains("Food");
            boolean isPotionItem = currentPath.contains("Potion");
            if (isFoodItem && FoodRecipeData.hasRecipe(itemName)) {
                contentArea.getChildren().add(
                        new FoodDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel));
            } else if (isPotionItem && PotionRecipeData.hasRecipe(itemName)) {
                contentArea.getChildren().add(
                        new PotionDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel));
            } else {
                contentArea.getChildren().add(
                        new CraftDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel));
            }
            return;
        }

        internalTopBar.setVisible(true);
        internalTopBar.setManaged(true);
        if (onDetailModeListener != null) onDetailModeListener.accept(false);

        // ── ROOT STATE: show 3 big centered buttons, no tree columns ──
        if (currentPath.isEmpty()) {
            List<String> rootOptions = CraftData.getChildren("ROOT");

            VBox centerBox = new VBox(24);
            centerBox.setAlignment(Pos.CENTER);
            centerBox.setPadding(new Insets(60, 60, 60, 60));
            centerBox.setStyle("-fx-background-color: #f1f5f9;");
            VBox.setVgrow(centerBox, Priority.ALWAYS);

            Label prompt = new Label("Choose a category to get started");
            prompt.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

            HBox buttonsRow = new HBox(24);
            buttonsRow.setAlignment(Pos.CENTER);

            for (String option : rootOptions) {
                Button btn = new Button(option);
                btn.setPrefWidth(200);
                btn.setPrefHeight(64);
                btn.setStyle(AppConfig.BTN_ROOT);
                btn.setOnMouseEntered(e -> btn.setStyle(AppConfig.BTN_ROOT_HOVER));
                btn.setOnMouseExited(e  -> btn.setStyle(AppConfig.BTN_ROOT));
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
        HBox levelsHBox = new HBox(16);
        levelsHBox.setAlignment(Pos.TOP_LEFT);
        levelsHBox.setPadding(new Insets(24, 28, 24, 28));

        int pathSize = currentPath.size();
        for (int level = 0; level <= pathSize; level++) {
            boolean isNextColumn = (level == pathSize);
            String parentKey = (level == 0) ? "ROOT" : currentPath.get(level - 1);
            List<String> options = CraftData.getChildren(parentKey);
            String selected = isNextColumn ? null : currentPath.get(level);

            VBox column = new VBox(6);
            column.setStyle(
                    "-fx-padding: 14 12 14 12;" +
                            "-fx-background-color: " + (isNextColumn ? "#ffffff" : "#f1f5f9") + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-min-width: 228;" +
                            "-fx-border-color: #e2e8f0;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-width: 1;"
            );

            String headerText = isNextColumn
                    ? (currentPath.isEmpty() ? "Choose category" : currentPath.get(currentPath.size() - 1))
                    : (level == 0 ? "Main Categories" : currentPath.get(level - 1));
            Label columnHeader = new Label(headerText);
            columnHeader.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: " + (isNextColumn ? "#ef4444" : "#94a3b8") + ";" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 0 0 6 0;"
            );
            column.getChildren().add(columnHeader);

            for (String option : options) {
                boolean isSelected = !isNextColumn && option.equals(selected);
                Button btn = createButton(option, isSelected, !isNextColumn);
                btn.setPrefWidth(202);
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
                Label emptyMsg = new Label("No items available");
                emptyMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 16 0 8 0;");
                column.getChildren().add(emptyMsg);
            }
            levelsHBox.getChildren().add(column);
        }
        contentArea.getChildren().add(levelsHBox);
    }

    // ====================== DETAIL SUB-PANEL ======================
    private static class CraftDetailSubPanel extends VBox {

        private record Material(String name) {}

        // Resolved dynamically per item from CraftingMaterialData
        private final List<Material> MATERIALS;

        public CraftDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack) {
            // Resolve materials for this specific item
            CraftMaterialData.Materials mats = CraftMaterialData.getMaterials(itemName);
            List<Material> resolvedMaterials = new java.util.ArrayList<>();
            resolvedMaterials.add(new Material(mats.material1()));
            resolvedMaterials.add(new Material(mats.material2())); // "N/A" if not applicable
            this.MATERIALS = resolvedMaterials;

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
            backBtn.setStyle(AppConfig.BTN_SECONDARY);
            backBtn.setOnMouseEntered(e -> backBtn.setStyle(AppConfig.BTN_SECONDARY_HOVER));
            backBtn.setOnMouseExited(e  -> backBtn.setStyle(AppConfig.BTN_SECONDARY));
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

            // ── LEFT COLUMN: search bar + item icon (click = go back) ──
            VBox left = new VBox(12);
            left.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(left, Priority.ALWAYS);

            TextField searchBar = new TextField();
            searchBar.setPromptText("Search tiers or materials...");
            searchBar.setStyle("-fx-font-size: 14px;");
            searchBar.setMaxWidth(Double.MAX_VALUE);

            // Item icon — loaded async from render.albiononline.com (T8 image)
            StackPane iconWrapper = buildItemIcon(itemName, onBack);

            left.getChildren().addAll(searchBar, iconWrapper);

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

            RequirementsCalculatorPanel reqPanel = RequirementsCalculatorPanel.forGear(itemName);
            reqPanel.setPadding(new Insets(20, 40, 20, 40));

            whiteContent.getChildren().addAll(topSection, buildTable(itemName), bottom, reqPanel);

            ScrollPane contentScroll = new ScrollPane(whiteContent);
            contentScroll.setFitToWidth(true);
            contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setStyle("-fx-background-color: white;");
            VBox.setVgrow(contentScroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, contentScroll);
        }

        private VBox buildTable(String itemName) {
            ArtifactData.ArtifactType artifactType = ArtifactData.getArtifactType(itemName);
            boolean hasArtifact = artifactType != null;
            boolean isAvalonEnergy = hasArtifact && artifactType == ArtifactData.ArtifactType.AVALON_ENERGY;

            // All 25 tier rows
            String[] allTiers = {
                    "4.0","4.1","4.2","4.3","4.4",
                    "5.0","5.1","5.2","5.3","5.4",
                    "6.0","6.1","6.2","6.3","6.4",
                    "7.0","7.1","7.2","7.3","7.4",
                    "8.0","8.1","8.2","8.3","8.4"
            };

            // Column layout:
            // col 0       : Tiers
            // col 1,2     : Material 1 → API | Manual
            // col 3,4     : Material 2 → API | Manual
            // col 5,6     : Material 3 → API | Manual  (only if hasArtifact; spans 5 rows per major tier)
            // col 7..14   : Demand, Costs, Focus Costs, Books, Fame, SPF, Profit, ROI
            int mat3Cols   = hasArtifact ? 2 : 0;
            int totalCols  = 1 + (MATERIALS.size() * 2) + mat3Cols + 8;
            double colPct  = 100.0 / totalCols;

            // Column index where result headers start
            int resultStartCol = 1 + (MATERIALS.size() * 2) + mat3Cols;
            // Column index where mat3 starts
            int mat3StartCol   = 1 + (MATERIALS.size() * 2);

            String[] resultHeaders = {"Demand", "Costs", "Focus Costs", "Books", "Fame", "SPF", "Profit", "ROI"};

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

            // ── Header row 0: Tiers | Mat1 (span2) | Mat2 (span2) | Mat3 (span2, if artifact) | result cols ──
            grid.add(makeHeaderLabel("Tiers"), 0, 0);
            int matCol = 1;
            for (Material mat : MATERIALS) {
                boolean isNA = CraftMaterialData.NA.equals(mat.name());
                Label matLbl = isNA ? makeNAHeaderLabel("N/A") : makeHeaderLabel(mat.name());
                GridPane.setColumnSpan(matLbl, 2);
                grid.add(matLbl, matCol, 0);
                matCol += 2;
            }
            if (hasArtifact) {
                String mat3Label = isAvalonEnergy ? "⚡ Avalonian Energy" : "🔮 " + artifactType.displayName;
                String mat3Color = isAvalonEnergy ? "#0ea5e9" : "#ef4444";
                Label mat3Hdr = new Label(mat3Label);
                mat3Hdr.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mat3Color + "; -fx-font-size: 13px;");
                mat3Hdr.setMaxWidth(Double.MAX_VALUE);
                mat3Hdr.setAlignment(Pos.CENTER);
                GridPane.setColumnSpan(mat3Hdr, 2);
                grid.add(mat3Hdr, mat3StartCol, 0);
            }
            for (int i = 0; i < resultHeaders.length; i++) {
                grid.add(makeHeaderLabel(resultHeaders[i]), resultStartCol + i, 0);
            }

            // ── Header row 1: empty | API|Manual per mat | API|Manual for mat3 | empty result ──
            grid.add(new Label(""), 0, 1);
            matCol = 1;
            for (Material mat : MATERIALS) {
                boolean isNA = CraftMaterialData.NA.equals(mat.name());
                grid.add(isNA ? makeNAHeaderLabel("—") : makeSubHeaderLabel("API"),    matCol,     1);
                grid.add(isNA ? makeNAHeaderLabel("—") : makeSubHeaderLabel("Manual"), matCol + 1, 1);
                matCol += 2;
            }
            if (hasArtifact) {
                grid.add(makeSubHeaderLabel("API"),    mat3StartCol,     1);
                grid.add(makeSubHeaderLabel("Manual"), mat3StartCol + 1, 1);
            }
            for (int i = 0; i < resultHeaders.length; i++) {
                grid.add(new Label(""), resultStartCol + i, 1);
            }

            // ── Data rows ──
            // Material3 has one value per major tier, spanning all 5 sub-tier rows vertically.
            // It is placed only on the .0 row of each group and given rowSpan=5.
            String[] resultValues = {"240", "620", "310", "8", "1250", "12", "850", "28%"};

            // Colours for mat3 fields
            String mat3FieldBg = isAvalonEnergy ? "#e0f2fe" : "#ffe4e4";

            int gridRow = 2;
            for (int t = 0; t < allTiers.length; t++) {
                String tier = allTiers[t];
                boolean isMajorTierRow = tier.endsWith(".0"); // first row of each 5-row group

                grid.add(makeDataLabel(tier), 0, gridRow);

                // Material 1 & 2 — every row
                matCol = 1;
                for (Material mat : MATERIALS) {
                    boolean isNA = CraftMaterialData.NA.equals(mat.name());

                    TextField apiField = new TextField(isNA ? "—" : "120");
                    apiField.setEditable(false);
                    apiField.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: "
                            + (isNA ? "#e8e8e8; -fx-text-fill: #aaaaaa;" : "#f0f0f0;"));
                    apiField.setMaxWidth(Double.MAX_VALUE);
                    apiField.setDisable(isNA);

                    TextField manualField = new TextField("");
                    manualField.setStyle("-fx-font-size: 12px; -fx-alignment: center;"
                            + (isNA ? " -fx-background-color: #e8e8e8;" : ""));
                    manualField.setMaxWidth(Double.MAX_VALUE);
                    manualField.setDisable(isNA);
                    if (!isNA) {
                        manualField.textProperty().addListener((obs, old, newVal) -> {
                            if (newVal != null && !newVal.trim().isEmpty()) apiField.setText(newVal);
                        });
                    }
                    grid.add(apiField,    matCol,     gridRow);
                    grid.add(manualField, matCol + 1, gridRow);
                    matCol += 2;
                }

                // Material 3 — only on the .0 row, spanning 5 rows vertically.
                // Each field is wrapped in a StackPane that fills the span height,
                // so the TextField stays at its natural height and centres visually.
                if (hasArtifact && isMajorTierRow) {
                    TextField mat3Api = new TextField("0");
                    mat3Api.setEditable(false);
                    mat3Api.setStyle(
                            "-fx-font-size: 12px; -fx-alignment: center;" +
                                    "-fx-background-color: " + mat3FieldBg + ";"
                    );
                    mat3Api.setMaxWidth(Double.MAX_VALUE);

                    TextField mat3Manual = new TextField("");
                    mat3Manual.setStyle(
                            "-fx-font-size: 12px; -fx-alignment: center;" +
                                    "-fx-background-color: " + mat3FieldBg + ";"
                    );
                    mat3Manual.setMaxWidth(Double.MAX_VALUE);
                    mat3Manual.textProperty().addListener((obs, old, newVal) -> {
                        if (newVal != null && !newVal.trim().isEmpty()) mat3Api.setText(newVal);
                    });

                    // Wrap in StackPanes so the span fills naturally and the field is centred
                    StackPane apiPane = new StackPane(mat3Api);
                    apiPane.setAlignment(Pos.CENTER);
                    apiPane.setMaxWidth(Double.MAX_VALUE);
                    apiPane.setMaxHeight(Double.MAX_VALUE);
                    apiPane.setStyle("-fx-background-color: " + mat3FieldBg + ";");

                    StackPane manualPane = new StackPane(mat3Manual);
                    manualPane.setAlignment(Pos.CENTER);
                    manualPane.setMaxWidth(Double.MAX_VALUE);
                    manualPane.setMaxHeight(Double.MAX_VALUE);
                    manualPane.setStyle("-fx-background-color: " + mat3FieldBg + ";");

                    GridPane.setRowSpan(apiPane,    5);
                    GridPane.setRowSpan(manualPane, 5);
                    GridPane.setFillHeight(apiPane,    true);
                    GridPane.setFillHeight(manualPane, true);
                    GridPane.setValignment(apiPane,    javafx.geometry.VPos.CENTER);
                    GridPane.setValignment(manualPane, javafx.geometry.VPos.CENTER);

                    grid.add(apiPane,    mat3StartCol,     gridRow);
                    grid.add(manualPane, mat3StartCol + 1, gridRow);
                }

                // Result value columns — every row
                for (int i = 0; i < resultValues.length; i++) {
                    grid.add(makeDataLabel(resultValues[i]), resultStartCol + i, gridRow);
                }
                gridRow++;
            }

            VBox wrapper = new VBox(4);
            wrapper.setMaxWidth(Double.MAX_VALUE);
            wrapper.setFillWidth(true);
            wrapper.getChildren().add(grid);
            return wrapper;
        }

        /**
         * Builds an item icon loaded asynchronously from the Albion render API (T8 image).
         * While loading, shows a spinner. On error, falls back to a placeholder label.
         * Clicking the icon triggers the onBack action.
         */
        private StackPane buildItemIcon(String itemName, Runnable onBack) {
            int SIZE = 256;

            StackPane wrapper = new StackPane();
            wrapper.setMaxWidth(SIZE);
            wrapper.setMinWidth(SIZE);
            wrapper.setPrefWidth(SIZE);
            wrapper.setMaxHeight(SIZE);
            wrapper.setMinHeight(SIZE);
            wrapper.setPrefHeight(SIZE);
            wrapper.setStyle(
                    "-fx-background-color: #f0f0f0;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #dddddd;" +
                            "-fx-border-radius: 8;" +
                            "-fx-cursor: hand;"
            );
            wrapper.setOnMouseClicked(e -> onBack.run());

            // Loading spinner shown while the image is fetching
            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setMaxWidth(48);
            spinner.setMaxHeight(48);

            // Tooltip for back navigation hint
            Tooltip tip = new Tooltip("Click to go back");
            Tooltip.install(wrapper, tip);

            String url = ItemRenderData.getT8ImageUrl(itemName);
            if (url == null) {
                Label fallback = new Label("?");
                fallback.setStyle("-fx-font-size: 48px; -fx-text-fill: #aaaaaa;");
                wrapper.getChildren().add(fallback);
                return wrapper;
            }

            wrapper.getChildren().add(spinner);

            // Load image in background — JavaFX Image supports backgroundLoading
            Image image = new Image(url, SIZE, SIZE, true, true, true);

            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    javafx.application.Platform.runLater(() -> {
                        wrapper.getChildren().clear();
                        if (image.isError()) {
                            // Fallback: show item name initials if render fails
                            Label fallback = new Label(itemName.substring(0, Math.min(2, itemName.length())).toUpperCase());
                            fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                            wrapper.getChildren().add(fallback);
                        } else {
                            ImageView iv = new ImageView(image);
                            iv.setFitWidth(SIZE);
                            iv.setFitHeight(SIZE);
                            iv.setPreserveRatio(true);
                            iv.setSmooth(true);
                            wrapper.getChildren().add(iv);
                        }
                    });
                }
            });

            return wrapper;
        }

        private Label makeNAHeaderLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #bbbbbb; -fx-font-size: 13px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
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
        btn.setPrefHeight(52);
        btn.setMinWidth(195);
        btn.setMaxWidth(Double.MAX_VALUE);

        String base;
        if (isSelected) {
            base = AppConfig.BTN_SELECTED;
        } else if (isSidePanel) {
            base = AppConfig.BTN_TRAVERSED;
        } else {
            base = AppConfig.BTN_ACTIVE;
        }
        btn.setStyle(base);

        if (!isSelected) {
            final String baseStyle  = base;
            final String hoverStyle = isSidePanel ? AppConfig.BTN_ACTIVE_HOVER : AppConfig.BTN_ACTIVE_HOVER;
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e  -> btn.setStyle(baseStyle));
        }

        return btn;
    }

    // =========================================================================
    //  FOOD DETAIL SUB-PANEL
    //  No tiers — columns are ingredients, rows are cities with buy price inputs.
    //  Fish sauce columns inline. Demand per city.
    // =========================================================================

    private static class FoodDetailSubPanel extends VBox {

        private static final List<String> CITIES      = FoodRecipeData.CITIES;
        private static final List<String> FISH_SAUCES = FoodRecipeData.FISH_SAUCES;

        public FoodDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack) {
            setPadding(new Insets(0));
            setSpacing(0);
            setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(this, Priority.ALWAYS);

            // ── Red header ───────────────────────────────────────────────────
            HBox redHeader = new HBox(15);
            redHeader.setPadding(new Insets(14, 40, 14, 40));
            redHeader.setStyle("-fx-background-color: #ef4444;");
            redHeader.setAlignment(Pos.CENTER_LEFT);

            Label breadcrumb = new Label(breadcrumbText);
            breadcrumb.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
            Region hSpacer = new Region();
            HBox.setHgrow(hSpacer, Priority.ALWAYS);

            FoodRecipeData.Recipe recipe = FoodRecipeData.getRecipe(itemName);
            Label tierLbl = recipe != null
                    ? new Label("T" + recipe.tier() + "  •  Batch: 10 items")
                    : new Label("");
            tierLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");

            Button backBtn = new Button("← Back");
            backBtn.setStyle(AppConfig.BTN_PRIMARY);
            backBtn.setOnMouseEntered(e -> backBtn.setStyle(AppConfig.BTN_PRIMARY_HOVER));
            backBtn.setOnMouseExited(e  -> backBtn.setStyle(AppConfig.BTN_PRIMARY));
            backBtn.setOnAction(e -> onBack.run());

            redHeader.getChildren().addAll(breadcrumb, tierLbl, hSpacer, backBtn);

            // ── Config bar ───────────────────────────────────────────────────
            HBox configBar = new HBox(28);
            configBar.setPadding(new Insets(14, 40, 14, 40));
            configBar.setAlignment(Pos.CENTER_LEFT);
            configBar.setStyle("-fx-background-color: #f8fafc;");

            configBar.getChildren().addAll(
                    labeledField("Quantity", "10"),
                    labeledField("Station Fee", "0"),
                    labeledCombo("Sell At", CITIES),
                    labeledCombo("Bonus Craft", List.of("Royal City", "Royal Island", "HO"))
            );

            // ── Scrollable content ───────────────────────────────────────────
            VBox content = new VBox(0);
            content.setFillWidth(true);
            content.setStyle("-fx-background-color: #ffffff;");

            if (recipe != null) {
                RequirementsCalculatorPanel reqPanel =
                        RequirementsCalculatorPanel.forFood(recipe);
                content.getChildren().addAll(buildFoodGrid(recipe), reqPanel);
            }

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setStyle("-fx-background-color: white;");
            VBox.setVgrow(scroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, configBar, scroll);
        }

        // ── Price grid ───────────────────────────────────────────────────────
        // Columns: City | ing1..N | sauce1..3 | Demand | Profit | SPF | Gain | Cost | Focus Cost
        // Row 0:   headers (ingredient names × qty, sauce names)
        // Row 1:   "API Price" — best market price per ingredient (read-only)
        // Rows 2+: one row per city — manual buy-price inputs + demand input

        private VBox buildFoodGrid(FoodRecipeData.Recipe recipe) {
            List<Ingredient> ings       = recipe.ingredients();
            int ingCols    = ings.size();
            int sauceCols  = FISH_SAUCES.size();
            String[] resultHeaders = {"Demand", "Profit", "SPF", "Gain", "Cost", "Focus Cost"};
            int resultCols = resultHeaders.length;
            int totalCols  = 1 + ingCols + sauceCols + resultCols;
            double colPct  = 100.0 / totalCols;

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setVgap(0);
            grid.setHgap(0);
            grid.setStyle("-fx-background-color: #ffffff; -fx-padding: 20 40 20 40;");

            for (int i = 0; i < totalCols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(colPct);
                cc.setHalignment(HPos.CENTER);
                cc.setFillWidth(true);
                grid.getColumnConstraints().add(cc);
            }

            // ── Row 0: column headers ─────────────────────────────────────
            grid.add(hdrCell("City"), 0, 0);
            int col = 1;
            for (Ingredient ing : ings) {
                Label l = new Label(ing.name() + " ×" + ing.quantity());
                l.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 12px;"
                        + "-fx-padding: 7 4 7 4;");
                l.setWrapText(true);
                l.setMaxWidth(Double.MAX_VALUE);
                l.setAlignment(Pos.CENTER);
                grid.add(l, col++, 0);
            }
            for (String sauce : FISH_SAUCES) {
                Label l = new Label(sauce);
                l.setStyle("-fx-font-weight: bold; -fx-text-fill: #0ea5e9; -fx-font-size: 11px;"
                        + "-fx-padding: 7 4 7 4;");
                l.setWrapText(true);
                l.setMaxWidth(Double.MAX_VALUE);
                l.setAlignment(Pos.CENTER);
                grid.add(l, col++, 0);
            }
            for (String rh : resultHeaders) {
                grid.add(hdrCell(rh), col++, 0);
            }

            // ── Row 1: API price row ──────────────────────────────────────
            Label apiRowLbl = new Label("API Price");
            apiRowLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;"
                    + "-fx-font-size: 12px; -fx-padding: 6 8 6 8;");
            apiRowLbl.setMaxWidth(Double.MAX_VALUE);
            grid.add(apiRowLbl, 0, 1);
            col = 1;
            for (int i = 0; i < ingCols + sauceCols; i++) {
                grid.add(apiField(), col++, 1);
            }
            for (int i = 0; i < resultCols; i++) {
                grid.add(calcCell("—", "#ffffff"), col++, 1);
            }

            // ── Rows 2+: one per city ─────────────────────────────────────
            for (int c = 0; c < CITIES.size(); c++) {
                int gridRow = c + 2;
                String bg = (c % 2 == 0) ? "#f8fafc" : "#ffffff";

                Label cityLbl = new Label(CITIES.get(c));
                cityLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"
                        + "-fx-text-fill: #1e293b; -fx-padding: 6 8 6 8;"
                        + "-fx-background-color: " + bg + ";");
                cityLbl.setMaxWidth(Double.MAX_VALUE);
                grid.add(cityLbl, 0, gridRow);

                col = 1;
                // Ingredient buy-price inputs (one per ingredient + sauce)
                for (int i = 0; i < ingCols + sauceCols; i++) {
                    grid.add(manualField(bg), col++, gridRow);
                }
                // Demand — editable per city
                grid.add(manualField(bg), col++, gridRow);
                // Remaining result cols — calculated
                for (int i = 1; i < resultCols; i++) {
                    grid.add(calcCell("—", bg), col++, gridRow);
                }
            }

            VBox wrapper = new VBox(grid);
            wrapper.setMaxWidth(Double.MAX_VALUE);
            wrapper.setFillWidth(true);
            return wrapper;
        }

        // ── Cell helpers ─────────────────────────────────────────────────────

        private Label hdrCell(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;"
                    + "-fx-font-size: 12px; -fx-padding: 7 4 7 4;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private Label calcCell(String text, String bg) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;"
                    + "-fx-padding: 4 4 4 4; -fx-background-color: " + bg + ";");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private TextField apiField() {
            TextField tf = new TextField("0");
            tf.setEditable(false);
            tf.setStyle("-fx-font-size: 12px; -fx-alignment: center;"
                    + "-fx-background-color: #f0f0f0;");
            tf.setMaxWidth(Double.MAX_VALUE);
            return tf;
        }

        private TextField manualField(String bg) {
            TextField tf = new TextField("");
            tf.setStyle("-fx-font-size: 12px; -fx-alignment: center;"
                    + "-fx-background-color: " + bg + ";");
            tf.setMaxWidth(Double.MAX_VALUE);
            return tf;
        }

        private VBox labeledField(String label, String def) {
            Label l = new Label(label);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            TextField tf = new TextField(def);
            tf.setPrefWidth(90);
            return new VBox(3, l, tf);
        }

        private VBox labeledCombo(String label, List<String> opts) {
            Label l = new Label(label);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            ComboBox<String> cb = new ComboBox<>();
            cb.getItems().addAll(opts);
            cb.setValue(opts.get(0));
            cb.setPrefWidth(150);
            return new VBox(3, l, cb);
        }
    }

    // =========================================================================
    //  POTION DETAIL SUB-PANEL
    // =========================================================================

    private static class PotionDetailSubPanel extends VBox {

        private static final List<String> CITIES = PotionRecipeData.CITIES;

        public PotionDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack) {
            setPadding(new Insets(0));
            setSpacing(0);
            setFillWidth(true);

            PotionRecipeData.PotionRecipe recipe = PotionRecipeData.getRecipe(itemName);

            // ── Red header ────────────────────────────────────────────────────
            HBox redHeader = new HBox(15);
            redHeader.setPadding(new Insets(15, 40, 15, 40));
            redHeader.setStyle("-fx-background-color: #ef4444;");
            redHeader.setAlignment(Pos.CENTER_LEFT);

            Label breadcrumb = new Label(breadcrumbText);
            breadcrumb.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button backBtn = new Button("← Back");
            backBtn.setStyle(AppConfig.BTN_PRIMARY);
            backBtn.setOnMouseEntered(e -> backBtn.setStyle(AppConfig.BTN_PRIMARY_HOVER));
            backBtn.setOnMouseExited(e  -> backBtn.setStyle(AppConfig.BTN_PRIMARY));
            backBtn.setOnAction(e -> onBack.run());

            redHeader.getChildren().addAll(breadcrumb, spacer, backBtn);

            // ── Scrollable white content ──────────────────────────────────────
            VBox whiteContent = new VBox(0);
            whiteContent.setFillWidth(true);
            whiteContent.setStyle("-fx-background-color: #ffffff;");

            if (recipe == null) {
                whiteContent.getChildren().add(new Label("Recipe not found: " + itemName));
            } else {
                RequirementsCalculatorPanel reqPanel =
                        RequirementsCalculatorPanel.forPotion(recipe);
                whiteContent.getChildren().addAll(
                        buildConfigBar(recipe),
                        new Separator(),
                        buildPriceGrid(recipe),
                        new Separator(),
                        buildArcaneExtractSection(recipe),
                        new Separator(),
                        buildSummarySection(),
                        reqPanel
                );
            }

            ScrollPane scroll = new ScrollPane(whiteContent);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: white;");
            VBox.setVgrow(scroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, scroll);
        }

        // ── Config bar ────────────────────────────────────────────────────────

        private HBox buildConfigBar(PotionRecipeData.PotionRecipe recipe) {
            HBox bar = new HBox(30);
            bar.setPadding(new Insets(18, 28, 18, 28));
            bar.setAlignment(Pos.CENTER_LEFT);
            bar.setStyle("-fx-background-color: #f8fafc;");

            VBox nameBox = new VBox(2);
            Label nameLabel = new Label(recipe.name());
            nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            Label batchLabel = new Label("Tier " + recipe.tier() + "   •   Batch: 5 potions");
            batchLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
            nameBox.getChildren().addAll(nameLabel, batchLabel);
            HBox.setHgrow(nameBox, Priority.ALWAYS);

            bar.getChildren().addAll(
                    nameBox,
                    labeledField("Quantity", "5"),
                    labeledField("Station Fee", "0"),
                    labeledCombo("Sell At", CITIES),
                    labeledCombo("Bonus Craft", List.of("Royal City", "Royal Island", "Royal City + Bonus", "HO"))
            );
            return bar;
        }

        // ── Price grid ────────────────────────────────────────────────────────
        //
        // Columns:
        //   col 0      : City
        //   col 1..N   : one col per herb/ingredient (API price at top, manual per city)
        //   col N+1    : tracking ingredient (only for 7 new potions, else greyed N/A)
        //   last cols  : Demand | Profit | SPF | Cost | Focus Cost

        private VBox buildPriceGrid(PotionRecipeData.PotionRecipe recipe) {
            List<PotionRecipeData.Ingredient> ingredients = recipe.ingredients();
            boolean hasTracking = recipe.hasTrackingIngredient();
            int ingCols    = ingredients.size();
            int trackCols  = 1; // always present, greyed out if not applicable
            int resultCols = 5; // Demand, Profit, SPF, Cost, Focus Cost
            int totalCols  = 1 + ingCols + trackCols + resultCols;
            double colPct  = 100.0 / totalCols;

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setVgap(0);
            grid.setHgap(0);
            grid.setStyle("-fx-background-color: #ffffff;");

            for (int i = 0; i < totalCols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(colPct);
                cc.setHalignment(HPos.CENTER);
                cc.setFillWidth(true);
                grid.getColumnConstraints().add(cc);
            }

            String[] resultHeaders = {"Demand", "Profit", "SPF", "Cost", "Focus Cost"};
            int trackCol    = 1 + ingCols;
            int resultStart = trackCol + 1;

            // ── Row 0: column headers ─────────────────────────────────────────
            grid.add(hdrCell(""), 0, 0);
            int col = 1;
            for (PotionRecipeData.Ingredient ing : ingredients) {
                Label lbl = new Label(ing.name() + " ×" + ing.quantity());
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #7c3aed; -fx-font-size: 12px;");
                lbl.setMaxWidth(Double.MAX_VALUE);
                lbl.setAlignment(Pos.CENTER);
                lbl.setPadding(new Insets(6, 4, 6, 4));
                lbl.setWrapText(true);
                grid.add(lbl, col++, 0);
            }
            // Tracking ingredient header
            if (hasTracking) {
                Label trackHdr = new Label("🦴 " + recipe.resolvedTrackingIngredient());
                trackHdr.setStyle("-fx-font-weight: bold; -fx-text-fill: #b45309; -fx-font-size: 12px;");
                trackHdr.setMaxWidth(Double.MAX_VALUE);
                trackHdr.setAlignment(Pos.CENTER);
                trackHdr.setPadding(new Insets(6, 4, 6, 4));
                trackHdr.setWrapText(true);
                grid.add(trackHdr, trackCol, 0);
            } else {
                grid.add(naCell("N/A"), trackCol, 0);
            }
            for (String h : resultHeaders) grid.add(hdrCell(h), resultStart + Arrays.asList(resultHeaders).indexOf(h), 0);

            // ── Row 1: "API Price" row ────────────────────────────────────────
            grid.add(boldCell("API Price", "#334155"), 0, 1);
            col = 1;
            for (int i = 0; i < ingCols; i++) grid.add(apiField(), col++, 1);
            grid.add(hasTracking ? apiField() : naField(), trackCol, 1);
            for (int i = 0; i < resultCols; i++) grid.add(calcCell("—", "#ffffff"), resultStart + i, 1);

            // ── Rows 2–8: city rows ───────────────────────────────────────────
            int gridRow = 2;
            for (int c = 0; c < CITIES.size(); c++) {
                String rowBg = (c % 2 == 0) ? "#f8fafc" : "#ffffff";
                Label cityLbl = new Label(CITIES.get(c));
                cityLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"
                        + "-fx-text-fill: #1e293b; -fx-padding: 6 8 6 8;"
                        + "-fx-background-color: " + rowBg + ";");
                cityLbl.setMaxWidth(Double.MAX_VALUE);
                grid.add(cityLbl, 0, gridRow);

                col = 1;
                for (int i = 0; i < ingCols; i++) grid.add(manualField(rowBg), col++, gridRow);
                grid.add(hasTracking ? manualField(rowBg) : naField(), trackCol, gridRow);
                // Demand editable, rest calculated
                grid.add(manualField(rowBg), resultStart, gridRow);
                for (int i = 1; i < resultCols; i++) grid.add(calcCell("—", rowBg), resultStart + i, gridRow);
                gridRow++;
            }

            VBox wrapper = new VBox(grid);
            wrapper.setFillWidth(true);
            wrapper.setMaxWidth(Double.MAX_VALUE);
            wrapper.setPadding(new Insets(20, 28, 20, 28));
            return wrapper;
        }

        // ── Arcane Extract enchanting section ─────────────────────────────────
        // Similar to Fish Sauce for food — shows sell prices per city per enchant level

        private VBox buildArcaneExtractSection(PotionRecipeData.PotionRecipe recipe) {
            VBox box = new VBox(8);
            box.setPadding(new Insets(16, 28, 0, 28));
            box.setStyle("-fx-background-color: #ffffff;");

            Label title = new Label("✨ Arcane Extract Enchanting  —  optional, shown per city");
            title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #7c3aed;");
            box.getChildren().add(title);

            Label note = new Label("Extract tier must match potion tier (T" + recipe.tier()
                    + " potion → T" + recipe.tier() + " extract)");
            note.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            box.getChildren().add(note);

            // Grid: city | sell price .1 | sell price .2 | sell price .3 | Demand | Profit | ROI | Focus Cost
            int totalCols = 1 + 3 + 4; // city + 3 enchant sell prices + results
            double colPct = 100.0 / totalCols;

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setVgap(0);
            grid.setHgap(0);

            for (int i = 0; i < totalCols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(colPct);
                cc.setHalignment(HPos.CENTER);
                cc.setFillWidth(true);
                grid.getColumnConstraints().add(cc);
            }

            // Header row
            grid.add(hdrCell("City"), 0, 0);
            String[] enchantLabels = {"Sell Price .1 (Basic)", "Sell Price .2 (Refined)", "Sell Price .3 (Pure)"};
            for (int i = 0; i < enchantLabels.length; i++) {
                Label lbl = new Label(enchantLabels[i]);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #7c3aed; -fx-font-size: 11px;");
                lbl.setMaxWidth(Double.MAX_VALUE);
                lbl.setAlignment(Pos.CENTER);
                lbl.setPadding(new Insets(6, 4, 6, 4));
                lbl.setWrapText(true);
                grid.add(lbl, i + 1, 0);
            }
            String[] resultHdrs = {"Demand", "Profit", "ROI", "Focus Cost"};
            for (int i = 0; i < resultHdrs.length; i++) {
                grid.add(hdrCell(resultHdrs[i]), 4 + i, 0);
            }

            // City rows
            for (int c = 0; c < CITIES.size(); c++) {
                String rowBg = (c % 2 == 0) ? "#faf5ff" : "#ede9fe";
                Label cityLbl = new Label(CITIES.get(c));
                cityLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"
                        + "-fx-text-fill: #1e293b; -fx-padding: 6 8 6 8;"
                        + "-fx-background-color: " + rowBg + ";");
                cityLbl.setMaxWidth(Double.MAX_VALUE);
                grid.add(cityLbl, 0, c + 1);
                for (int i = 0; i < 3; i++) grid.add(manualField(rowBg), i + 1, c + 1);
                for (int i = 0; i < resultHdrs.length; i++) grid.add(calcCell("—", rowBg), 4 + i, c + 1);
            }

            box.getChildren().add(grid);
            return box;
        }

        // ── Summary section ───────────────────────────────────────────────────

        private VBox buildSummarySection() {
            VBox box = new VBox(10);
            box.setPadding(new Insets(20, 28, 30, 28));
            box.setStyle("-fx-background-color: #f8fafc;");
            Label lbl = new Label("Calculation results / summary will appear here once prices are entered");
            lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
            box.getChildren().add(lbl);
            return box;
        }

        // ── Cell factory helpers ──────────────────────────────────────────────

        private Label naCell(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #bbbbbb; -fx-padding: 6 4 6 4;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private TextField naField() {
            TextField tf = new TextField("—");
            tf.setEditable(false);
            tf.setDisable(true);
            tf.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: #e8e8e8; -fx-text-fill: #aaaaaa;");
            tf.setMaxWidth(Double.MAX_VALUE);
            return tf;
        }

        private Label hdrCell(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #7c3aed; -fx-font-size: 12px; -fx-padding: 6 4 6 4;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private Label boldCell(String text, String color) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-size: 12px; -fx-padding: 6 8 6 8;");
            l.setMaxWidth(Double.MAX_VALUE);
            return l;
        }

        private Label calcCell(String text, String bgColor) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-padding: 4 4 4 4; -fx-background-color: " + bgColor + ";");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private TextField apiField() {
            TextField tf = new TextField("0");
            tf.setEditable(false);
            tf.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: #f0f0f0;");
            tf.setMaxWidth(Double.MAX_VALUE);
            return tf;
        }

        private TextField manualField(String bgColor) {
            TextField tf = new TextField("");
            tf.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: " + bgColor + ";");
            tf.setMaxWidth(Double.MAX_VALUE);
            return tf;
        }

        private VBox labeledField(String label, String defaultVal) {
            Label lbl = new Label(label);
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            TextField tf = new TextField(defaultVal);
            tf.setStyle("-fx-font-size: 13px;");
            tf.setPrefWidth(90);
            return new VBox(3, lbl, tf);
        }

        private VBox labeledCombo(String label, List<String> opts) {
            Label lbl = new Label(label);
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            ComboBox<String> cb = new ComboBox<>();
            cb.getItems().addAll(opts);
            cb.setValue(opts.get(0));
            cb.setPrefWidth(150);
            return new VBox(3, lbl, cb);
        }
    }
}