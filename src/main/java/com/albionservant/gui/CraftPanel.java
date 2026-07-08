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
    private static final double FORM_CONTROL_WIDTH = 470.0;

    private static void lockComboBoxWidth(ComboBox<?> comboBox) {
        comboBox.setMinWidth(FORM_CONTROL_WIDTH);
        comboBox.setPrefWidth(FORM_CONTROL_WIDTH);
        comboBox.setMaxWidth(FORM_CONTROL_WIDTH);
    }


    private final VBox contentArea = new VBox(15);
    private final HBox internalTopBar = new HBox(15);
    private List<String> currentPath = new ArrayList<>();
    private Consumer<Boolean> onDetailModeListener;
    private SpecsPanel specsPanel;

    public void setSpecsPanel(SpecsPanel sp) { this.specsPanel = sp; }

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
                        new FoodDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel,
                                specsPanel,
                                currentPath.size() >= 2 ? currentPath.get(currentPath.size() - 2) : ""));
            } else if (isPotionItem && PotionRecipeData.hasRecipe(itemName)) {
                contentArea.getChildren().add(
                        new PotionDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel,
                                specsPanel,
                                currentPath.size() >= 2 ? currentPath.get(currentPath.size() - 2) : ""));
            } else {
                contentArea.getChildren().add(
                        new CraftDetailSubPanel(itemName, breadcrumbText, this::goBackOneLevel, specsPanel));
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

        private final List<Material> MATERIALS;
        private final SpecsPanel     specsPanel;
        private final String         itemName;

        public CraftDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack,
                                   SpecsPanel specsPanel) {
            CraftMaterialData.Materials mats = CraftMaterialData.getMaterials(itemName);
            List<Material> resolvedMaterials = new java.util.ArrayList<>();
            resolvedMaterials.add(new Material(mats.material1()));
            resolvedMaterials.add(new Material(mats.material2()));
            this.MATERIALS   = resolvedMaterials;
            this.specsPanel  = specsPanel;
            this.itemName    = itemName;

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

            RequirementsCalculatorPanel reqPanel = RequirementsCalculatorPanel.forGear(itemName);

            left.getChildren().addAll(searchBar, iconWrapper, reqPanel);

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
            lockComboBoxWidth(demandType);

            CheckBox craftingFocus = new CheckBox("Crafting Focus");
            craftingFocus.setStyle("-fx-font-size: 13px;");

            Label bonusCraftLbl = new Label("Bonus Craft:");
            bonusCraftLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> bonusCraft = new ComboBox<>();
            bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
            bonusCraft.setValue("Royal City");
            lockComboBoxWidth(bonusCraft);

            VBox hoSection = new VBox(6);
            hoSection.setVisible(false);
            hoSection.setManaged(false);

            Label hoQualityLbl = new Label("Hideout Quality:");
            hoQualityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> hoQuality = new ComboBox<>();
            hoQuality.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Q5", "Q6");
            hoQuality.setValue("Q5");
            lockComboBoxWidth(hoQuality);

            Label hoPowerLbl = new Label("Hideout Power Level:");
            hoPowerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<Integer> hoPower = new ComboBox<>();
            hoPower.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9);
            hoPower.setValue(5);
            lockComboBoxWidth(hoPower);

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
                    reqPanel.getRrrLabel(),
                    hoSection
            );

            // ── RIGHT COLUMN: material buy locations + sell location ──
            VBox right = new VBox(8);
            right.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(right, Priority.ALWAYS);

            Label buy1Lbl = new Label("Material-Buy1:");
            buy1Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy1 = createCityCombo(false);
            lockComboBoxWidth(buy1);

            Label buy2Lbl = new Label("Material-Buy2:");
            buy2Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy2 = createCityCombo(false);
            lockComboBoxWidth(buy2);

            Label buy3Lbl = new Label("Material-Buy3:");
            buy3Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy3 = createCityCombo(true);
            lockComboBoxWidth(buy3);

            Label buy4Lbl = new Label("Material-Buy4:");
            buy4Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> buy4 = createCityCombo(true);
            lockComboBoxWidth(buy4);

            Label sellLbl = new Label("Sell-Location:");
            sellLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> sellLocation = createSellCombo();
            lockComboBoxWidth(sellLocation);

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

            whiteContent.getChildren().addAll(topSection, buildTable(itemName, reqPanel, quantity), bottom);

            reqPanel.bindControls(quantity, bonusCraft, craftingFocus, hoQuality, hoPower);

            com.albionservant.data.CraftQuantityData.Quantities gearQty =
                    com.albionservant.data.CraftQuantityData.get(itemName);
            int gearTotalMats = gearQty.mat1() + gearQty.mat2()
                    + (com.albionservant.data.ArtifactData.getArtifactType(itemName) != null ? 1 : 0);
            reqPanel.setFocusContext(4, gearTotalMats,
                    () -> specsPanel != null ? lookupGearSpec(specsPanel, itemName) : 0);

            ScrollPane contentScroll = new ScrollPane(whiteContent);
            contentScroll.setFitToWidth(true);
            contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setStyle("-fx-background-color: white;");
            VBox.setVgrow(contentScroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, contentScroll);
        }

        private VBox buildTable(String itemName, RequirementsCalculatorPanel reqPanel,
                                TextField quantityField) {
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
                Label hdr = makeHeaderLabel(resultHeaders[i]);
                if (i == 2) {
                    // Focus Costs header only — row values are calculated separately
                    VBox focusHdrBox = new VBox(1, hdr);
                    focusHdrBox.setAlignment(Pos.CENTER);
                    focusHdrBox.setMaxWidth(Double.MAX_VALUE);
                    grid.add(focusHdrBox, resultStartCol + i, 0);
                } else {
                    grid.add(hdr, resultStartCol + i, 0);
                }
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
            // Pre-compute total materials for focus cost lookup
            com.albionservant.data.CraftQuantityData.Quantities qty =
                    com.albionservant.data.CraftQuantityData.get(itemName);
            int totalMats = qty.mat1() + qty.mat2()
                    + (com.albionservant.data.ArtifactData.getArtifactType(itemName) != null ? 1 : 0);
            int specLevel = specsPanel != null ? lookupGearSpec(specsPanel, itemName) : 0;

            // Colours for mat3 fields
            String mat3FieldBg = isAvalonEnergy ? "#e0f2fe" : "#ffe4e4";

            int gridRow = 2;
            for (int t = 0; t < allTiers.length; t++) {
                String tier = allTiers[t];
                boolean isMajorTierRow = tier.endsWith(".0");

                // Tier base colour — subtle, one per major tier group
                String tierBg = switch (tier.charAt(0)) {
                    case '4' -> "#f0fdf4";   // T4 — light green
                    case '5' -> "#f0f9ff";   // T5 — light blue
                    case '6' -> "#fdf2f8";   // T6 — light pink
                    case '7' -> "#f5f3ff";   // T7 — light purple
                    case '8' -> "#fff1f2";   // T8 — light red
                    default  -> "#ffffff";
                };
                // Enchant overlay: .0=none, .1=green, .2=blue, .3=purple, .4=yellow
                String enchantSuffix = tier.contains(".") ? tier.substring(tier.indexOf('.')) : ".0";
                String rowBg = switch (enchantSuffix) {
                    case ".0" -> tierBg;
                    case ".1" -> blendHex(tierBg, "#bbf7d0");
                    case ".2" -> blendHex(tierBg, "#bfdbfe");
                    case ".3" -> blendHex(tierBg, "#e9d5ff");
                    case ".4" -> blendHex(tierBg, "#fef08a");
                    default   -> tierBg;
                };

                Label tierLbl = makeDataLabel(tier);
                tierLbl.setStyle("-fx-font-size: 13px; -fx-background-color: " + rowBg + ";");
                grid.add(tierLbl, 0, gridRow);

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

                // Result value columns — Focus Costs × quantity, updates live
                int tierNum    = Character.getNumericValue(tier.charAt(0));
                int enchantNum = tier.contains(".") ? Integer.parseInt(tier.substring(tier.indexOf('.') + 1)) : 0;

                String[] resultValues = {
                        "—", "—", "", "—", "—", "—", "—", "—"
                };

                for (int i = 0; i < resultValues.length; i++) {
                    if (i == 2) {
                        Label focusCell = new Label("—");
                        focusCell.setStyle("-fx-font-size: 13px; -fx-text-fill: #818cf8;"
                                + "-fx-font-weight: bold;");
                        focusCell.setMaxWidth(Double.MAX_VALUE);
                        focusCell.setAlignment(Pos.CENTER);

                        final int   fTierNum    = tierNum;
                        final int   fEnchantNum = enchantNum;
                        final int   fTotalMats  = totalMats;

                        // Helper: compute focus for current spec + qty
                        Runnable updateFocusCell = () -> {
                            int    liveSpec = specsPanel != null
                                    ? lookupGearSpec(specsPanel, itemName) : 0;
                            long   livePerItem = com.albionservant.data.FocusCostCalculator
                                    .compute(fTierNum, fEnchantNum, fTotalMats, liveSpec);
                            try {
                                long qty1 = Math.max(1, Long.parseLong(
                                        quantityField.getText().trim()));
                                focusCell.setText(String.format("%,d", livePerItem * qty1));
                            } catch (NumberFormatException ignored) {
                                focusCell.setText(String.format("%,d", livePerItem));
                            }
                        };

                        // Initial value
                        updateFocusCell.run();

                        // Update on quantity change
                        quantityField.textProperty().addListener(
                                (obs, ov, nv) -> updateFocusCell.run());
                        grid.add(focusCell, resultStartCol + i, gridRow);
                    } else {
                        grid.add(makeDataLabel(resultValues[i]), resultStartCol + i, gridRow);
                    }
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

        /** Blends two hex colours — averages RGB components for subtle row tinting */
        private String blendHex(String hex1, String hex2) {
            int r1 = Integer.parseInt(hex1.substring(1,3), 16);
            int g1 = Integer.parseInt(hex1.substring(3,5), 16);
            int b1 = Integer.parseInt(hex1.substring(5,7), 16);
            int r2 = Integer.parseInt(hex2.substring(1,3), 16);
            int g2 = Integer.parseInt(hex2.substring(3,5), 16);
            int b2 = Integer.parseInt(hex2.substring(5,7), 16);
            return String.format("#%02x%02x%02x", (r1+r2)/2, (g1+g2)/2, (b1+b2)/2);
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

    /**
     * Looks up the player's spec level for a gear item from SpecsPanel.
     * Walks the Warrior/Hunter/Mage/Toolmaker tree to find the right group.
     */
    private static int lookupGearSpec(SpecsPanel sp, String itemName) {
        String[][] sectionGroups = {
                {"Warrior", "Swords"}, {"Warrior", "Axes"}, {"Warrior", "Maces"},
                {"Warrior", "Hammers"}, {"Warrior", "War Gloves"}, {"Warrior", "Crossbows"},
                {"Warrior", "Shields"}, {"Warrior", "Plate Helmet"}, {"Warrior", "Plate Armor"},
                {"Warrior", "Plate Boots"},
                {"Hunter", "Bows"}, {"Hunter", "Daggers"}, {"Hunter", "Spears"},
                {"Hunter", "Quarterstaves"}, {"Hunter", "Shapeshifter"}, {"Hunter", "Nature Staves"},
                {"Hunter", "Torches"}, {"Hunter", "Leather Hood"}, {"Hunter", "Leather Jacket"},
                {"Hunter", "Leather Shoes"},
                {"Mage", "Fire Staves"}, {"Mage", "Arcane Staves"}, {"Mage", "Frost Staves"},
                {"Mage", "Holy Staves"}, {"Mage", "Cursed Staves"}, {"Mage", "Tomes"},
                {"Mage", "Cloth Cowl"}, {"Mage", "Cloth Robe"}, {"Mage", "Cloth Sandals"},
                {"Toolmaker", "Harvester"}, {"Toolmaker", "Lumberjack"}, {"Toolmaker", "Miner"},
                {"Toolmaker", "Quarrier"}, {"Toolmaker", "Skinner"}, {"Toolmaker", "Fisherman"},
                {"Toolmaker", "Siege Equipment"}, {"Toolmaker", "Bags"}, {"Toolmaker", "Capes"}
        };
        for (String[] sg : sectionGroups) {
            int v = sp.getSpecLevel(sg[0], sg[1], itemName);
            if (v > 0) return v;
        }
        return 0;
    }

    /**
     * Looks up the player's spec level for a food item from SpecsPanel.
     * Food specs live under Cooking → Food.
     */
    static int lookupFoodSpec(SpecsPanel sp, String categoryName) {
        return sp.getSpecLevel("Cooking", "Food", categoryName);
    }

    static int lookupPotionSpec(SpecsPanel sp, String categoryName) {
        return sp.getSpecLevel("Cooking", "Potions", categoryName);
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
    private static final List<String> CITIES = FoodRecipeData.CITIES;
    private static final List<String> FISH_SAUCES = FoodRecipeData.FISH_SAUCES;
    private static final double FOOD_CONTROL_WIDTH = 360.0;

    private record PricePair(TextField api, TextField manual) {}
    private record ResultRow(
            int enchant,
            TextField demand,
            Label profit,
            Label spf,
            Label gain,
            Label cost,
            Label focus
    ) {}

    public FoodDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack,
                              SpecsPanel specsPanel, String category) {
        setPadding(new Insets(0));
        setSpacing(0);
        setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);

        FoodRecipeData.Recipe recipe = FoodRecipeData.getRecipe(itemName);

        HBox redHeader = new HBox(15);
        redHeader.setPadding(new Insets(14, 40, 14, 40));
        redHeader.setStyle("-fx-background-color: #ef4444;");
        redHeader.setAlignment(Pos.CENTER_LEFT);

        Label breadcrumb = new Label(breadcrumbText);
        breadcrumb.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        Label tierLbl = recipe != null
                ? new Label("T" + recipe.tier() + " | Batch: " + recipe.batchSize() + " item" + (recipe.batchSize() == 1 ? "" : "s"))
                : new Label("");

        tierLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button backBtn = new Button("<- Back");
        backBtn.setStyle(AppConfig.BTN_PRIMARY);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(AppConfig.BTN_PRIMARY_HOVER));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(AppConfig.BTN_PRIMARY));
        backBtn.setOnAction(e -> onBack.run());

        redHeader.getChildren().addAll(breadcrumb, tierLbl, hSpacer, backBtn);

        if (recipe == null) {
            Label missing = new Label("Missing food recipe: " + itemName);
            missing.setStyle("-fx-font-size: 16px; -fx-text-fill: #ef4444; -fx-padding: 24;");
            getChildren().addAll(redHeader, missing);
            return;
        }

        VBox whiteContent = new VBox(22);
        whiteContent.setPadding(new Insets(20, 40, 40, 40));
        whiteContent.setStyle("-fx-background-color: #ffffff;");
        whiteContent.setMaxWidth(Double.MAX_VALUE);
        whiteContent.setFillWidth(true);

        HBox configSection = new HBox(30);
        configSection.setAlignment(Pos.TOP_LEFT);
        configSection.setFillHeight(false);
        configSection.setMaxWidth(Double.MAX_VALUE);

        VBox cfgLeft = new VBox(12);
        cfgLeft.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cfgLeft, Priority.ALWAYS);

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search ingredients...");
        searchBar.setStyle("-fx-font-size: 14px;");
        searchBar.setMaxWidth(Double.MAX_VALUE);

        StackPane iconPane = buildFoodIcon(itemName, onBack);

        RequirementsCalculatorPanel reqPanel = RequirementsCalculatorPanel.forFood(recipe);
        cfgLeft.getChildren().addAll(searchBar, iconPane, reqPanel);

        VBox cfgCenter = new VBox(8);
        cfgCenter.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cfgCenter, Priority.ALWAYS);

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
        lockFoodCombo(demandType);

        CheckBox craftingFocus = new CheckBox("Crafting Focus");
        craftingFocus.setStyle("-fx-font-size: 13px;");

        Label bonusCraftLbl = new Label("Bonus Craft:");
        bonusCraftLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        ComboBox<String> bonusCraft = new ComboBox<>();
        bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
        bonusCraft.setValue("Royal City");
        lockFoodCombo(bonusCraft);

        VBox hoSection = new VBox(6);
        hoSection.setVisible(false);
        hoSection.setManaged(false);

        Label hoQualityLbl = new Label("Hideout Quality:");
        hoQualityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        ComboBox<String> hoQuality = new ComboBox<>();
        hoQuality.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Q5", "Q6");
        hoQuality.setValue("Q5");
        lockFoodCombo(hoQuality);

        Label hoPowerLbl = new Label("Hideout Power Level:");
        hoPowerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        ComboBox<Integer> hoPower = new ComboBox<>();
        hoPower.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9);
        hoPower.setValue(5);
        lockFoodCombo(hoPower);

        hoSection.getChildren().addAll(hoQualityLbl, hoQuality, hoPowerLbl, hoPower);

        bonusCraft.setOnAction(e -> {
            boolean isHO = "HO".equals(bonusCraft.getValue());
            hoSection.setVisible(isHO);
            hoSection.setManaged(isHO);
        });

        cfgCenter.getChildren().addAll(
                quantityLbl, quantity,
                stationFeeLbl, stationFee,
                demandTypeLbl, demandType,
                craftingFocus,
                bonusCraftLbl, bonusCraft,
                reqPanel.getRrrLabel(),
                hoSection
        );

        VBox cfgRight = new VBox(8);
        cfgRight.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cfgRight, Priority.ALWAYS);

        Label buyLbl = new Label("Food Materials:");
        buyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        ComboBox<String> buyLocation = createFoodCityCombo();
        lockFoodCombo(buyLocation);

        Label sellLbl = new Label("Sell Location:");
        sellLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        ComboBox<String> sellLocation = createFoodCityCombo();
        sellLocation.setValue("Caerleon");
        lockFoodCombo(sellLocation);

        Label note = new Label("Result table uses cheapest entered material price and highest entered sell price.");
        note.setWrapText(true);
        note.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        cfgRight.getChildren().addAll(buyLbl, buyLocation, sellLbl, sellLocation, note);

        configSection.getChildren().addAll(cfgLeft, cfgCenter, cfgRight);

        List<List<PricePair>> ingredientPrices = new ArrayList<>();
        List<List<PricePair>> saucePrices = new ArrayList<>();
        List<PricePair> sellPrices = new ArrayList<>();
        List<ResultRow> resultRows = new ArrayList<>();

        VBox ingredientPriceTable = buildFoodPriceTable(recipe, ingredientPrices, saucePrices, sellPrices);
        VBox resultTable = buildFoodResultTable(recipe, quantity, specsPanel, category, ingredientPrices, saucePrices, sellPrices, resultRows);

        Runnable updateResults = () -> updateFoodResults(recipe, quantity, specsPanel, category,
                ingredientPrices, saucePrices, sellPrices, resultRows);

        quantity.textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());

        for (List<PricePair> materialPairs : ingredientPrices) {
            for (PricePair pair : materialPairs) {
                pair.api().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                pair.manual().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            }
        }

        for (List<PricePair> saucePairs : saucePrices) {
            for (PricePair pair : saucePairs) {
                pair.api().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                pair.manual().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            }
        }

        for (PricePair pair : sellPrices) {
            pair.api().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            pair.manual().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
        }

        whiteContent.getChildren().addAll(configSection, ingredientPriceTable, resultTable);

        reqPanel.bindControls(quantity, bonusCraft, craftingFocus, hoQuality, hoPower);

        int totalPerBatch = recipe.ingredients().stream()
                .mapToInt(Ingredient::quantity)
                .sum();

        int approxMatsPerItem = (int) Math.ceil(totalPerBatch / (double) Math.max(1, recipe.batchSize()));

        reqPanel.setFocusContext(
                recipe.tier(),
                approxMatsPerItem,
                () -> specsPanel != null ? CraftPanel.lookupFoodSpec(specsPanel, category) : 0
        );

        updateResults.run();

        ScrollPane contentScroll = new ScrollPane(whiteContent);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroll.setStyle("-fx-background-color: white;");

        VBox.setVgrow(contentScroll, Priority.ALWAYS);

        getChildren().addAll(redHeader, contentScroll);
    }

    private VBox buildFoodPriceTable(
            FoodRecipeData.Recipe recipe,
            List<List<PricePair>> ingredientPrices,
            List<List<PricePair>> saucePrices,
            List<PricePair> sellPrices
    ) {
        Label title = new Label("Ingredient prices");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        GridPane grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");
        grid.setVgap(4);
        grid.setHgap(0);

        int ingredientCols = recipe.ingredients().size() * 2;
        int sauceCols = FISH_SAUCES.size() * 2;
        int totalCols = 1 + ingredientCols + sauceCols + 2;
        double colPct = 100.0 / totalCols;

        for (int i = 0; i < totalCols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(colPct);
            cc.setHalignment(HPos.CENTER);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        int col = 0;

        grid.add(makeFoodHeader("City"), col++, 0);

        for (Ingredient ingredient : recipe.ingredients()) {
            Label header = makeFoodHeader(ingredient.name() + " x" + ingredient.quantity());
            GridPane.setColumnSpan(header, 2);
            grid.add(header, col, 0);
            col += 2;

            List<PricePair> pairsForIngredient = new ArrayList<>();
            ingredientPrices.add(pairsForIngredient);
        }

        for (String sauce : FISH_SAUCES) {
            Label header = makeFoodHeader(sauce);
            GridPane.setColumnSpan(header, 2);
            grid.add(header, col, 0);
            col += 2;

            List<PricePair> pairsForSauce = new ArrayList<>();
            saucePrices.add(pairsForSauce);
        }

        Label sellHeader = makeFoodHeader("Sell Price");
        GridPane.setColumnSpan(sellHeader, 2);
        grid.add(sellHeader, col, 0);

        col = 1;

        for (int i = 0; i < recipe.ingredients().size(); i++) {
            grid.add(makeFoodSubHeader("API"), col++, 1);
            grid.add(makeFoodSubHeader("Manual"), col++, 1);
        }

        for (int i = 0; i < FISH_SAUCES.size(); i++) {
            grid.add(makeFoodSubHeader("API"), col++, 1);
            grid.add(makeFoodSubHeader("Manual"), col++, 1);
        }

        grid.add(makeFoodSubHeader("API"), col++, 1);
        grid.add(makeFoodSubHeader("Manual"), col, 1);

        int row = 2;

        for (String city : CITIES) {
            col = 0;

            Label cityLabel = makeFoodData(city);
            cityLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334155;");
            grid.add(cityLabel, col++, row);

            for (int i = 0; i < recipe.ingredients().size(); i++) {
                PricePair pair = createFoodPricePair();
                ingredientPrices.get(i).add(pair);

                grid.add(pair.api(), col++, row);
                grid.add(pair.manual(), col++, row);
            }

            for (int i = 0; i < FISH_SAUCES.size(); i++) {
                PricePair pair = createFoodPricePair();
                saucePrices.get(i).add(pair);

                grid.add(pair.api(), col++, row);
                grid.add(pair.manual(), col++, row);
            }

            PricePair sellPair = createFoodPricePair();
            sellPrices.add(sellPair);

            grid.add(sellPair.api(), col++, row);
            grid.add(sellPair.manual(), col, row);

            row++;
        }

        VBox wrapper = new VBox(6, title, grid);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setFillWidth(true);

        return wrapper;
    }

    private VBox buildFoodResultTable(
            FoodRecipeData.Recipe recipe,
            TextField quantity,
            SpecsPanel specsPanel,
            String category,
            List<List<PricePair>> ingredientPrices,
            List<List<PricePair>> saucePrices,
            List<PricePair> sellPrices,
            List<ResultRow> resultRows
    ) {
        Label title = new Label("Food result by enchant");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        GridPane grid = new GridPane();
        grid.setMaxWidth(720);
        grid.setStyle(
                "-fx-background-color: #17243a;" +
                "-fx-border-color: #eab308;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 0;"
        );
        grid.setVgap(0);
        grid.setHgap(0);

        String[] headers = {"Enchant", "Demand", "Profit", "SPF", "Gain", "Cost", "Focus Cost"};

        for (int i = 0; i < headers.length; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / headers.length);
            cc.setHalignment(HPos.CENTER);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);

            Label header = makeDarkHeader(headers[i]);
            grid.add(header, i, 0);
        }

        for (int enchant = 0; enchant <= 3; enchant++) {
            int row = enchant + 1;

            Label enchantLabel = makeDarkData("." + enchant);
            TextField demandField = makeDarkInput();

            Label profitLabel = makeDarkData("-");
            Label spfLabel = makeDarkData("-");
            Label gainLabel = makeDarkData("-");
            Label costLabel = makeDarkData("-");
            Label focusLabel = makeDarkData("-");

            focusLabel.setStyle(darkCellStyle() + "-fx-text-fill: #ffffff; -fx-font-weight: bold;");

            grid.add(enchantLabel, 0, row);
            grid.add(demandField, 1, row);
            grid.add(profitLabel, 2, row);
            grid.add(spfLabel, 3, row);
            grid.add(gainLabel, 4, row);
            grid.add(costLabel, 5, row);
            grid.add(focusLabel, 6, row);

            resultRows.add(new ResultRow(enchant, demandField, profitLabel, spfLabel, gainLabel, costLabel, focusLabel));
        }

        VBox wrapper = new VBox(6, title, grid);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setFillWidth(true);

        return wrapper;
    }

    private void updateFoodResults(
            FoodRecipeData.Recipe recipe,
            TextField quantityField,
            SpecsPanel specsPanel,
            String category,
            List<List<PricePair>> ingredientPrices,
            List<List<PricePair>> saucePrices,
            List<PricePair> sellPrices,
            List<ResultRow> resultRows
    ) {
        long quantity = parseLong(quantityField.getText(), 1);
        int spec = specsPanel != null ? CraftPanel.lookupFoodSpec(specsPanel, category) : 0;
        int batchSize = Math.max(1, recipe.batchSize());

        double ingredientCostPerItem = 0.0;

        for (int i = 0; i < recipe.ingredients().size(); i++) {
            Ingredient ingredient = recipe.ingredients().get(i);
            double bestPrice = bestBuyPrice(ingredientPrices.get(i));

            ingredientCostPerItem += bestPrice * (ingredient.quantity() / (double) batchSize);
        }

        double bestSell = bestSellPrice(sellPrices);

        for (ResultRow row : resultRows) {
            double costPerItem = ingredientCostPerItem;

            if (row.enchant() > 0) {
                int sauceIndex = row.enchant() - 1;

                if (sauceIndex >= 0 && sauceIndex < saucePrices.size()) {
                    costPerItem += bestBuyPrice(saucePrices.get(sauceIndex));
                }
            }

            double totalCost = costPerItem * quantity;
            double totalGain = bestSell * quantity;
            double totalProfit = totalGain - totalCost;

            long focusPerItem = com.albionservant.data.FocusCostCalculator.forFood(recipe, row.enchant(), spec);
            long totalFocus = focusPerItem * quantity;

            row.cost().setText(formatSilver(totalCost));
            row.gain().setText(formatSilver(totalGain));
            row.profit().setText(formatSilver(totalProfit));
            row.focus().setText(String.format("%,d", totalFocus));

            if (totalFocus > 0) {
                row.spf().setText(String.format("%.2f", totalProfit / totalFocus));
            } else {
                row.spf().setText("-");
            }

            if (totalProfit > 0) {
                row.profit().setStyle(darkCellStyle() + "-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            } else if (totalProfit < 0) {
                row.profit().setStyle(darkCellStyle() + "-fx-text-fill: #f87171; -fx-font-weight: bold;");
            } else {
                row.profit().setStyle(darkCellStyle());
            }
        }
    }

    private StackPane buildFoodIcon(String itemName, Runnable onBack) {
        int iconSize = 128;

        StackPane iconPane = new StackPane();
        iconPane.setPrefSize(iconSize, iconSize);
        iconPane.setMinSize(iconSize, iconSize);
        iconPane.setMaxSize(iconSize, iconSize);
        iconPane.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8; -fx-cursor: hand;");
        iconPane.setOnMouseClicked(e -> onBack.run());

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxWidth(48);
        spinner.setMaxHeight(48);
        iconPane.getChildren().add(spinner);

        String foodIconUrl = com.albionservant.data.ItemRenderData.getFoodImageUrl(itemName);

        if (foodIconUrl == null) {
            iconPane.getChildren().clear();

            Label fallback = new Label(itemName.substring(0, Math.min(2, itemName.length())).toUpperCase());
            fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

            iconPane.getChildren().add(fallback);
            return iconPane;
        }

        Image img = new Image(foodIconUrl, iconSize, iconSize, true, true, true);

        img.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 1.0) {
                javafx.application.Platform.runLater(() -> {
                    iconPane.getChildren().clear();

                    if (img.isError()) {
                        Label fallback = new Label(itemName.substring(0, Math.min(2, itemName.length())).toUpperCase());
                        fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                        iconPane.getChildren().add(fallback);
                    } else {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(iconSize);
                        iv.setFitHeight(iconSize);
                        iv.setPreserveRatio(true);
                        iv.setSmooth(true);
                        iconPane.getChildren().add(iv);
                    }
                });
            }
        });

        return iconPane;
    }

    private static PricePair createFoodPricePair() {
        TextField api = new TextField("0");
        api.setEditable(false);
        api.setStyle("-fx-font-size: 12px; -fx-alignment: center; -fx-background-color: #f0f0f0;");
        api.setMaxWidth(Double.MAX_VALUE);

        TextField manual = new TextField("");
        manual.setPromptText("manual");
        manual.setStyle("-fx-font-size: 12px; -fx-alignment: center;");
        manual.setMaxWidth(Double.MAX_VALUE);

        return new PricePair(api, manual);
    }

    private static ComboBox<String> createFoodCityCombo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(CITIES);
        cb.setValue("Martlock");
        return cb;
    }

    private static void lockFoodCombo(ComboBox<?> comboBox) {
        comboBox.setMinWidth(FOOD_CONTROL_WIDTH);
        comboBox.setPrefWidth(FOOD_CONTROL_WIDTH);
        comboBox.setMaxWidth(FOOD_CONTROL_WIDTH);
    }

    private static Label makeFoodHeader(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 12px;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private static Label makeFoodSubHeader(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-font-size: 11px;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private static Label makeFoodData(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private static Label makeDarkHeader(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-alignment: center;" +
                "-fx-padding: 4 6 4 6;" +
                "-fx-border-color: #eab308;" +
                "-fx-border-width: 0 1 1 0;"
        );
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private static Label makeDarkData(String text) {
        Label label = new Label(text);
        label.setStyle(darkCellStyle());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private static TextField makeDarkInput() {
        TextField field = new TextField("");
        field.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-prompt-text-fill: #94a3b8;" +
                "-fx-background-color: #17243a;" +
                "-fx-alignment: center;" +
                "-fx-border-color: #eab308;" +
                "-fx-border-width: 0 1 1 0;"
        );
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private static String darkCellStyle() {
        return "-fx-font-size: 12px;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-alignment: center;" +
                "-fx-padding: 4 6 4 6;" +
                "-fx-border-color: #eab308;" +
                "-fx-border-width: 0 1 1 0;";
    }

    private static double bestBuyPrice(List<PricePair> pairs) {
        double best = 0.0;

        for (PricePair pair : pairs) {
            double price = effectivePrice(pair);

            if (price > 0 && (best == 0.0 || price < best)) {
                best = price;
            }
        }

        return best;
    }

    private static double bestSellPrice(List<PricePair> pairs) {
        double best = 0.0;

        for (PricePair pair : pairs) {
            double price = effectivePrice(pair);

            if (price > best) {
                best = price;
            }
        }

        return best;
    }

    private static double effectivePrice(PricePair pair) {
        String manual = pair.manual().getText();

        if (manual != null && !manual.trim().isEmpty()) {
            return parseDouble(manual, 0.0);
        }

        return parseDouble(pair.api().getText(), 0.0);
    }

    private static long parseLong(String value, long fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            String cleaned = value.trim().replace(" ", "").replace(",", "");
            if (cleaned.isEmpty()) {
                return fallback;
            }

            return Math.max(1L, Long.parseLong(cleaned));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        if (value == null) {
            return fallback;
        }

        try {
            String cleaned = value.trim().replace(" ", "").replace(",", "").replace("_", "");
            if (cleaned.isEmpty()) {
                return fallback;
            }

            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String formatSilver(double value) {
        if (Math.abs(value) < 0.5) {
            return "0";
        }

        return String.format("%,.0f", value);
    }
}


    // =========================================================================
    //  POTION DETAIL SUB-PANEL
    // =========================================================================

    private static class PotionDetailSubPanel extends VBox {

        private static final List<String> CITIES = PotionRecipeData.CITIES;

        public PotionDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack,
                                    SpecsPanel specsPanel, String category) {
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

            // ── Full 3-column config section (mirrors CraftDetailSubPanel) ──────
            HBox configSection = new HBox(30);
            configSection.setPadding(new Insets(20, 40, 20, 40));
            configSection.setAlignment(Pos.TOP_LEFT);
            configSection.setFillHeight(false);
            configSection.setMaxWidth(Double.MAX_VALUE);
            configSection.setStyle("-fx-background-color: #ffffff;");

            // LEFT: search + icon
            VBox cfgLeft = new VBox(12);
            cfgLeft.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cfgLeft, Priority.ALWAYS);
            TextField searchBar = new TextField();
            searchBar.setPromptText("Search ingredients...");
            searchBar.setStyle("-fx-font-size: 14px;");
            searchBar.setMaxWidth(Double.MAX_VALUE);
            Label iconLbl = new Label("⚗️");
            iconLbl.setStyle("-fx-font-size: 72px; -fx-cursor: hand;");
            iconLbl.setOnMouseClicked(e -> onBack.run());
            // ── Requirements calculator — inside left column below icon ──────
            RequirementsCalculatorPanel reqPanel = recipe != null
                    ? RequirementsCalculatorPanel.forPotion(recipe)
                    : new RequirementsCalculatorPanel(List.of(), 5);

            // Load potion icon async from render API — shows spinner while loading
            String potionIconUrl = recipe != null
                    ? com.albionservant.data.ItemRenderData.getPotionImageUrl(itemName)
                    : null;
            int ICON_SIZE = 128;
            javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
            iconPane.setPrefSize(ICON_SIZE, ICON_SIZE);
            iconPane.setMaxSize(ICON_SIZE, ICON_SIZE);
            iconPane.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8; -fx-cursor: hand;");
            iconPane.setOnMouseClicked(e -> onBack.run());

            ProgressIndicator spinner = new ProgressIndicator();
            spinner.setMaxWidth(48);
            spinner.setMaxHeight(48);
            iconPane.getChildren().add(spinner);

            if (potionIconUrl != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        potionIconUrl, ICON_SIZE, ICON_SIZE, true, true, true);
                img.progressProperty().addListener((obs, ov, nv) -> {
                    if (nv.doubleValue() >= 1.0) {
                        javafx.application.Platform.runLater(() -> {
                            iconPane.getChildren().clear();
                            if (!img.isError()) {
                                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                                iv.setFitWidth(ICON_SIZE);
                                iv.setFitHeight(ICON_SIZE);
                                iv.setPreserveRatio(true);
                                iconPane.getChildren().add(iv);
                            }
                        });
                    }
                });
            }
            cfgLeft.getChildren().addAll(searchBar, iconPane, reqPanel);

            // CENTER: quantity, station fee, demand type, crafting focus, bonus craft, HO
            VBox cfgCenter = new VBox(8);
            cfgCenter.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cfgCenter, Priority.ALWAYS);

            Label qtyLbl2 = new Label("Quantity:");
            qtyLbl2.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            TextField qtyField = new TextField("5");
            qtyField.setStyle("-fx-font-size: 14px;");
            qtyField.setMaxWidth(Double.MAX_VALUE);

            Label feeLbl = new Label("Station Fee:");
            feeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            TextField feeField = new TextField("0");
            feeField.setStyle("-fx-font-size: 14px;");
            feeField.setMaxWidth(Double.MAX_VALUE);

            Label demandLbl = new Label("Demand Type:");
            demandLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> demandType = new ComboBox<>();
            demandType.getItems().addAll("24h", "7d", "4w");
            demandType.setValue("24h");
            lockComboBoxWidth(demandType);

            CheckBox focusBox = new CheckBox("Crafting Focus");
            focusBox.setStyle("-fx-font-size: 13px;");

            Label bonusLbl = new Label("Bonus Craft:");
            bonusLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> bonusCraft = new ComboBox<>();
            bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
            bonusCraft.setValue("Royal City");
            lockComboBoxWidth(bonusCraft);

            VBox hoSect = new VBox(6);
            hoSect.setVisible(false);
            hoSect.setManaged(false);
            Label hoQlbl = new Label("Hideout Quality:");
            hoQlbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> hoQ = new ComboBox<>();
            hoQ.getItems().addAll("Q1","Q2","Q3","Q4","Q5","Q6");
            hoQ.setValue("Q5");
            hoQ.setMaxWidth(Double.MAX_VALUE);
            Label hoPLbl = new Label("Hideout Power Level:");
            hoPLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<Integer> hoPL = new ComboBox<>();
            hoPL.getItems().addAll(1,2,3,4,5,6,7,8,9);
            hoPL.setValue(5);
            hoPL.setMaxWidth(Double.MAX_VALUE);
            hoSect.getChildren().addAll(hoQlbl, hoQ, hoPLbl, hoPL);
            bonusCraft.setOnAction(e -> {
                boolean isHO = "HO".equals(bonusCraft.getValue());
                hoSect.setVisible(isHO);
                hoSect.setManaged(isHO);
            });

            cfgCenter.getChildren().addAll(
                    qtyLbl2, qtyField, feeLbl, feeField,
                    demandLbl, demandType, focusBox,
                    bonusLbl, bonusCraft,
                    reqPanel.getRrrLabel(),
                    hoSect
            );
            // RIGHT: ingredient buy locations + sell location
            VBox cfgRight = new VBox(8);
            cfgRight.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cfgRight, Priority.ALWAYS);

            String[] buyLabels = {"Ingredient-Buy1:", "Ingredient-Buy2:", "Ingredient-Buy3:", "Ingredient-Buy4:"};
            boolean[] withMedian = {false, false, true, true};
            for (int i = 0; i < buyLabels.length; i++) {
                Label bl = new Label(buyLabels[i]);
                bl.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
                ComboBox<String> bc = new ComboBox<>();
                bc.getItems().addAll("Bridgewatch","Martlock","Thetford","Fort Sterling","Lymhurst","Caerleon","Brecilien");
                if (withMedian[i]) bc.getItems().add("Median");
                bc.setValue("Martlock");
                bc.setMaxWidth(Double.MAX_VALUE);
                cfgRight.getChildren().addAll(bl, bc);
            }
            Label sellLbl2 = new Label("Sell-Location:");
            sellLbl2.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
            ComboBox<String> sellLoc = new ComboBox<>();
            sellLoc.getItems().addAll("Bridgewatch","Martlock","Thetford","Fort Sterling","Lymhurst","Caerleon","Brecilien");
            sellLoc.setValue("Martlock");
            sellLoc.setMaxWidth(Double.MAX_VALUE);
            cfgRight.getChildren().addAll(sellLbl2, sellLoc);

            configSection.getChildren().addAll(cfgLeft, cfgCenter, cfgRight);

            reqPanel.bindControls(qtyField, bonusCraft, focusBox, hoQ, hoPL);

            if (recipe != null) {
                int potionTotal = recipe.ingredients().stream()
                        .mapToInt(com.albionservant.data.PotionRecipeData.Ingredient::quantity)
                        .sum() / recipe.batchSize();
                reqPanel.setFocusContext(recipe.tier(), potionTotal,
                        () -> specsPanel != null ? lookupPotionSpec(specsPanel, category) : 0);
            }
            VBox whiteContent = new VBox(0);
            whiteContent.setFillWidth(true);
            whiteContent.setStyle("-fx-background-color: #ffffff;");

            if (recipe == null) {
                whiteContent.getChildren().add(new Label("Recipe not found: " + itemName));
            } else {
                whiteContent.getChildren().addAll(
                        buildPriceGrid(recipe),
                        new Separator(),
                        buildArcaneExtractSection(recipe),
                        new Separator(),
                        buildSummarySection()
                );
            }

            ScrollPane scroll = new ScrollPane(whiteContent);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: white;");
            VBox.setVgrow(scroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, configSection, scroll);
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
            Label batchLabel = new Label("Tier " + recipe.tier() + "   •   Batch: " + recipe.batchSize() + " potions");
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