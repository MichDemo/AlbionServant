package com.albionservant.gui;

import com.albionservant.data.ArtifactData;
import com.albionservant.data.CraftData;
import com.albionservant.data.CraftMaterialData;
import com.albionservant.data.FoodRecipeData;
import com.albionservant.data.FoodRecipeData.Ingredient;
import com.albionservant.data.PotionRecipeData;
import com.albionservant.data.ItemRenderData;
import com.albionservant.integration.market.LocalMarketPriceService;
// ALBIONSERVANT_LOCAL_PRICE_PATCH
// ALBIONSERVANT_GEAR_QUALITY_PRICE_PATCH
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

import com.albionservant.data.CraftingNutritionData;
import com.albionservant.data.CraftingJournalData;
// ALBIONSERVANT_NUTRITION_RRR_PATCH_V5
// ALBIONSERVANT_CRAFTING_SPREADSHEET_FIXES_PATCH_V1\n
// ALBIONSERVANT_SHAPESHIFTER_DUAL_SPECIAL_MATERIALS_PATCH_V1
public class CraftPanel extends VBox {
    private static final double FORM_CONTROL_WIDTH = 190.0;

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
        setStyle("-fx-background-color: #4b5058;");

        internalTopBar.setAlignment(Pos.CENTER_LEFT);
        internalTopBar.setPadding(new Insets(14, 20, 14, 20));

        Label breadcrumbLabel = new Label();
        breadcrumbLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        HBox.setHgrow(breadcrumbLabel, Priority.ALWAYS);

        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("button-secondary");
        backButton.setOnAction(e -> goBackOneLevel());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        internalTopBar.getChildren().addAll(breadcrumbLabel, spacer, backButton);

        contentArea.setStyle("-fx-background-color: #f0f1f3;");
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
            centerBox.setStyle("-fx-background-color: #f0f1f3;");
            VBox.setVgrow(centerBox, Priority.ALWAYS);

            Label prompt = new Label("Choose a category to get started");
            prompt.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #707782;");

            HBox buttonsRow = new HBox(24);
            buttonsRow.setAlignment(Pos.CENTER);

            for (String option : rootOptions) {
                Button btn = new Button(option);
                btn.setPrefWidth(200);
                btn.setPrefHeight(64);
                btn.getStyleClass().add("button-root");
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
                            "-fx-background-color: " + (isNextColumn ? "#ffffff" : "#f0f1f3") + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-min-width: 228;" +
                            "-fx-border-color: #e6e8eb;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-width: 1;"
            );

            String headerText = isNextColumn
                    ? (currentPath.isEmpty() ? "Choose category" : currentPath.get(currentPath.size() - 1))
                    : (level == 0 ? "Main Categories" : currentPath.get(level - 1));
            Label columnHeader = new Label(headerText);
            columnHeader.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: " + (isNextColumn ? "#4b5058" : "#9ca2ab") + ";" +
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
                emptyMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca2ab; -fx-padding: 16 0 8 0;");
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
            redHeader.setStyle("-fx-background-color: #4b5058;");
            redHeader.setAlignment(Pos.CENTER_LEFT);

            Label breadcrumb = new Label(breadcrumbText);
            breadcrumb.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button backBtn = new Button("← Back");
            backBtn.getStyleClass().add("button-secondary");
            backBtn.setOnAction(e -> onBack.run());

            redHeader.getChildren().addAll(breadcrumb, spacer, backBtn);

            // ── SCROLLABLE WHITE CONTENT ──
            VBox whiteContent = new VBox(30);
            whiteContent.setPadding(new Insets(30, 40, 40, 40));
            whiteContent.setStyle("-fx-background-color: #f6f7f8;");
            whiteContent.setFillWidth(true);
            whiteContent.setMaxWidth(Double.MAX_VALUE);

            // ── TOP SECTION: 3 equal columns, stretches with window ──
            HBox topSection = new HBox(26);
            topSection.setAlignment(Pos.TOP_LEFT);
            topSection.setFillHeight(false);
            topSection.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(topSection, Priority.ALWAYS);

            // ── LEFT COLUMN: search bar + item icon (click = go back) ──
            VBox left = new VBox(12);
            left.setMinWidth(270);
            left.setPrefWidth(270);
            left.setMaxWidth(270);
            HBox.setHgrow(left, Priority.NEVER);

            TextField searchBar = new TextField();
            searchBar.setPromptText("Search tiers or materials...");
            searchBar.setStyle("-fx-font-size: 14px;");
            searchBar.setMinWidth(270);
            searchBar.setPrefWidth(270);
            searchBar.setMaxWidth(270);

            // Item icon — loaded async from render.albiononline.com (T8 image)
            StackPane iconWrapper = buildItemIcon(itemName, onBack);

            RequirementsCalculatorPanel reqPanel = RequirementsCalculatorPanel.forGear(itemName);
            reqPanel.setMinWidth(300);
            reqPanel.setPrefWidth(320);
            reqPanel.setMaxWidth(360);

            left.getChildren().addAll(searchBar, iconWrapper);

            // ── CENTER COLUMN: quantity, station fee, demand type, crafting focus, bonus craft, HO ──
            VBox center = new VBox(8);
            center.setMinWidth(230);
            center.setPrefWidth(260);
            center.setMaxWidth(320);
            HBox.setHgrow(center, Priority.SOMETIMES);

            Label quantityLbl = new Label("Quantity:");
            quantityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            TextField quantity = new TextField("200");
            quantity.setStyle("-fx-font-size: 14px;");
            quantity.setMinWidth(190);
            quantity.setPrefWidth(190);
            quantity.setMaxWidth(190);

            Label stationFeeLbl = new Label("Fee / 100 Nutrition:");
            stationFeeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            TextField stationFee = new TextField("999");
            stationFee.setStyle("-fx-font-size: 14px;");
            stationFee.setMinWidth(190);
            stationFee.setPrefWidth(190);
            stationFee.setMaxWidth(190);

            Label demandTypeLbl = new Label("Demand Type:");
            demandTypeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> demandType = new ComboBox<>();
            demandType.getItems().addAll("24h", "7d", "4w");
            demandType.setValue("24h");
            lockComboBoxWidth(demandType);

            CheckBox craftingFocus = new CheckBox("Crafting Focus");
            craftingFocus.setStyle("-fx-font-size: 13px;");


            CheckBox dailyBonus = new CheckBox("Daily Bonus");
            dailyBonus.setStyle("-fx-font-size: 13px;");

            ComboBox<String> dailyBonusValue = new ComboBox<>();
            dailyBonusValue.getItems().addAll("10%", "20%");
            dailyBonusValue.setValue("10%");
            dailyBonusValue.setMinWidth(82);
            dailyBonusValue.setPrefWidth(82);
            dailyBonusValue.setMaxWidth(82);
            dailyBonusValue.setDisable(true);

            dailyBonus.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                    dailyBonusValue.setDisable(!isSelected)
            );

            HBox dailyBonusRow = new HBox(8, dailyBonus, dailyBonusValue);
            dailyBonusRow.setAlignment(Pos.CENTER_LEFT);
            Label bonusCraftLbl = new Label("Bonus Craft:");
            bonusCraftLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> bonusCraft = new ComboBox<>();
            bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
            bonusCraft.setValue("Royal City");
            lockComboBoxWidth(bonusCraft);

            VBox hoSection = new VBox(6);
            hoSection.setVisible(false);
            hoSection.setManaged(false);

            Label hoQualityLbl = new Label("Hideout Quality:");
            hoQualityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> hoQuality = new ComboBox<>();
            hoQuality.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Q5", "Q6");
            hoQuality.setValue("Q5");
            lockComboBoxWidth(hoQuality);

            Label hoPowerLbl = new Label("Hideout Power Level:");
            hoPowerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
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
                    dailyBonusRow,
                    bonusCraftLbl, bonusCraft,
                    reqPanel.getRrrLabel(),
                    hoSection
            );

            // ── RIGHT COLUMN: material buy locations + sell location ──
            VBox right = new VBox(8);
            right.setMinWidth(320);
            right.setPrefWidth(360);
            right.setMaxWidth(420);
            HBox.setHgrow(right, Priority.NEVER);

            Label buy1Lbl = new Label("Material-Buy1:");
            buy1Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> buy1 = createCityCombo(false);
            lockComboBoxWidth(buy1);

            Label buy2Lbl = new Label("Material-Buy2:");
            buy2Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> buy2 = createCityCombo(false);
            lockComboBoxWidth(buy2);

            Label buy3Lbl = new Label("Material-Buy3:");
            buy3Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> buy3 = createCityCombo(true);
            lockComboBoxWidth(buy3);

            Label buy4Lbl = new Label("Material-Buy4:");
            buy4Lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> buy4 = createCityCombo(true);
            lockComboBoxWidth(buy4);

            Label sellQualityLbl = new Label("Sell-Quality:");
            sellQualityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> sellQuality = new ComboBox<>();
            sellQuality.getItems().addAll(
                    "Normal (Q1)",
                    "Good (Q2)",
                    "Outstanding (Q3)",
                    "Excellent (Q4)",
                    "Masterpiece (Q5)"
            );
            sellQuality.setValue("Normal (Q1)");
            lockComboBoxWidth(sellQuality);

            Label sellLbl = new Label("Sell-Location:");
            sellLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");
            ComboBox<String> sellLocation = createSellCombo();
            lockComboBoxWidth(sellLocation);

            right.getChildren().addAll(
                    buy1Lbl,
                    buy1,
                    buy2Lbl,
                    buy2,
                    buy3Lbl,
                    buy3,
                    buy4Lbl,
                    buy4,
                    sellQualityLbl,
                    sellQuality,
                    sellLbl,
                    sellLocation
            );


            VBox reqColumn = new VBox(8);
            reqColumn.setMinWidth(360);
            reqColumn.setPrefWidth(420);
            reqColumn.setMaxWidth(540);
            reqColumn.setAlignment(Pos.TOP_LEFT);
            HBox.setHgrow(reqColumn, Priority.NEVER);
            reqColumn.getChildren().add(reqPanel);

            topSection.getChildren().addAll(left, center, right, reqColumn);
            topSection.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(topSection, Priority.NEVER);

            // ── BOTTOM SUMMARY ──
            VBox bottom = new VBox(10);
            bottom.setStyle("-fx-background-color: #f1f3f5; -fx-padding: 20; -fx-background-radius: 8;");
            Label bottomTitle = new Label("Calculation results / summary will appear here");
            bottomTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #7d838c;");
            bottom.getChildren().add(bottomTitle);

            whiteContent.getChildren().addAll(topSection, buildTable(itemName, reqPanel, quantity, buy1, buy2, buy3, buy4, sellLocation, sellQuality, demandType, stationFee, craftingFocus), bottom);

            reqPanel.bindControls(quantity, bonusCraft, craftingFocus, dailyBonus, dailyBonusValue, hoQuality, hoPower);

            com.albionservant.data.CraftQuantityData.Quantities gearQty =
                    com.albionservant.data.CraftQuantityData.get(itemName);
            boolean gearHasArtifact =
                    com.albionservant.data.ArtifactData
                            .getArtifactType(itemName) != null;
            int gearTotalMats = gearQty.mat1() + gearQty.mat2()
                    + (gearHasArtifact
                    ? LocalMarketPriceService.artifactQuantity(itemName, 4)
                    : 0)
                    + LocalMarketPriceService
                    .additionalRequirementQuantity(itemName, 4);
            reqPanel.setFocusContext(4, gearTotalMats,
                    () -> specsPanel != null ? lookupGearSpec(specsPanel, itemName) : 0);

            ScrollPane contentScroll = new ScrollPane(whiteContent);
            contentScroll.setFitToWidth(true);
            contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setStyle("-fx-background-color: #f6f7f8;");
            VBox.setVgrow(contentScroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, contentScroll);
        }

        private VBox buildTable(
                String itemName,
                RequirementsCalculatorPanel reqPanel,
                TextField quantityField,
                ComboBox<String> buy1,
                ComboBox<String> buy2,
                ComboBox<String> buy3,
                ComboBox<String> buy4,
                ComboBox<String> sellLocation,
                ComboBox<String> sellQuality,
                ComboBox<String> demandType,
                TextField stationFeeField,
                CheckBox craftingFocus
        ) {
            ArtifactData.ArtifactType artifactType = ArtifactData.getArtifactType(itemName);
            boolean hasArtifact = artifactType != null;
            boolean isAvalonEnergy = hasArtifact && artifactType == ArtifactData.ArtifactType.AVALON_ENERGY;
            boolean hasAdditionalRequirement =
                    LocalMarketPriceService
                            .hasAdditionalCraftingRequirement(itemName);

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
            // next pair   : tiered artifact/remnant, when present
            // next pair   : additional special material, when present
            // final cols  : Demand, Costs, Sell Price, Focus Costs, Books, Fame, SPF, Profit, ROI
            int mat3Cols = hasArtifact ? 2 : 0;
            int mat4Cols = hasAdditionalRequirement ? 2 : 0;
            int totalCols = 1 + (MATERIALS.size() * 2)
                    + mat3Cols + mat4Cols + 9;

            int mat3StartCol = 1 + (MATERIALS.size() * 2);
            int mat4StartCol = mat3StartCol + mat3Cols;
            int resultStartCol = mat4StartCol + mat4Cols;

            String[] resultHeaders = {"Demand", "Costs", "Sell Price", "Focus Costs", "Books", "Fame", "SPF", "Profit", "ROI"};

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setStyle("-fx-background-color: #f4f5f6; -fx-padding: 15; -fx-background-radius: 8;");
            grid.setVgap(4);
            grid.setHgap(0);
            grid.setMinWidth(Region.USE_PREF_SIZE);

            for (int i = 0; i < totalCols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                double prefWidth = preferredCraftColumnWidth(
                        i,
                        resultStartCol,
                        mat3StartCol,
                        mat4StartCol,
                        hasArtifact,
                        hasAdditionalRequirement
                );
                cc.setMinWidth(Math.min(prefWidth, 90.0));
                cc.setPrefWidth(prefWidth);
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
                String mat3DisplayName = LocalMarketPriceService
                        .artifactColumnDisplayName(
                                itemName,
                                artifactType.displayName
                        );
                String mat3Label = (isAvalonEnergy ? "⚡ " : "🔮 ")
                        + mat3DisplayName;
                String mat3Color = isAvalonEnergy ? "#0ea5e9" : "#4b5058";
                Label mat3Hdr = new Label(mat3Label);
                mat3Hdr.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mat3Color + "; -fx-font-size: 13px;");
                mat3Hdr.setMaxWidth(Double.MAX_VALUE);
                mat3Hdr.setAlignment(Pos.CENTER);
                GridPane.setColumnSpan(mat3Hdr, 2);
                grid.add(mat3Hdr, mat3StartCol, 0);
            }
            if (hasAdditionalRequirement) {
                String additionalDisplayName = LocalMarketPriceService
                        .additionalRequirementColumnDisplayName(itemName);
                Label additionalHdr = new Label(
                        "🧪 " + additionalDisplayName
                );
                additionalHdr.setStyle(
                        "-fx-font-weight: bold;"
                                + "-fx-text-fill: #9a5b13;"
                                + "-fx-font-size: 13px;"
                );
                additionalHdr.setMaxWidth(Double.MAX_VALUE);
                additionalHdr.setAlignment(Pos.CENTER);
                additionalHdr.setTooltip(new Tooltip(
                        "Additional non-returnable recipe component"
                ));
                GridPane.setColumnSpan(additionalHdr, 2);
                grid.add(additionalHdr, mat4StartCol, 0);
            }
            for (int i = 0; i < resultHeaders.length; i++) {
                Label hdr = makeHeaderLabel(resultHeaders[i]);
                if (i == 3) {
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
            if (hasAdditionalRequirement) {
                grid.add(makeSubHeaderLabel("API"),    mat4StartCol,     1);
                grid.add(makeSubHeaderLabel("Manual"), mat4StartCol + 1, 1);
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

            // Colours for mat3 fields
            String mat3FieldBg = isAvalonEnergy ? "#e0f2fe" : "#ffe4e4";

            TextField majorArtifactApi = null;
            TextField majorArtifactManual = null;
            TextField majorAdditionalApi = null;
            TextField majorAdditionalManual = null;

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

                // Material 1 & 2 - exact local SQLite prices for this row.
                int tierNum = Character.getNumericValue(tier.charAt(0));
                int enchantNum = tier.contains(".")
                        ? Integer.parseInt(tier.substring(tier.indexOf('.') + 1))
                        : 0;
                int rowTotalMats = qty.mat1() + qty.mat2()
                        + (hasArtifact
                        ? LocalMarketPriceService.artifactQuantity(
                        itemName,
                        tierNum
                )
                        : 0)
                        + LocalMarketPriceService
                        .additionalRequirementQuantity(itemName, tierNum);

                List<TextField> rowApiFields = new ArrayList<>();
                List<TextField> rowManualFields = new ArrayList<>();
                List<Integer> rowMaterialQuantities = new ArrayList<>();
                List<Boolean> rowMaterialReturnable = new ArrayList<>();

                matCol = 1;
                int materialIndex = 0;

                for (Material mat : MATERIALS) {
                    boolean isNA = CraftMaterialData.NA.equals(mat.name());

                    TextField apiField =
                            new TextField(isNA ? "\u2014" : "...");

                    apiField.setEditable(false);
                    apiField.setStyle(
                            "-fx-font-size: 12px;"
                                    + "-fx-alignment: center;"
                                    + "-fx-background-color: "
                                    + (isNA
                                    ? "#e4e6e9; -fx-text-fill: #9ba0a8;"
                                    : "#f0f0f0;")
                    );
                    apiField.setMaxWidth(Double.MAX_VALUE);
                    apiField.setDisable(isNA);

                    TextField manualField = new TextField("");
                    manualField.setPromptText("manual");
                    manualField.setStyle(
                            "-fx-font-size: 12px;"
                                    + "-fx-alignment: center;"
                                    + (isNA
                                    ? " -fx-background-color: #e4e6e9;"
                                    : "")
                    );
                    manualField.setMaxWidth(Double.MAX_VALUE);
                    manualField.setDisable(isNA);

                    if (!isNA) {
                        int materialQuantity = materialIndex == 0
                                ? qty.mat1()
                                : qty.mat2();

                        ComboBox<String> materialCity = materialIndex == 0
                                ? buy1
                                : buy2;

                        String materialItemId =
                                LocalMarketPriceService.refinedMaterialItemId(
                                        mat.name(),
                                        tierNum,
                                        enchantNum
                                );

                        LocalMarketPriceService.bindMinSell(
                                apiField,
                                materialCity,
                                materialItemId
                        );

                        rowApiFields.add(apiField);
                        rowManualFields.add(manualField);
                        rowMaterialQuantities.add(materialQuantity);
                        rowMaterialReturnable.add(true);
                    }

                    grid.add(apiField, matCol, gridRow);
                    grid.add(manualField, matCol + 1, gridRow);

                    matCol += 2;
                    materialIndex++;
                }
                // Material 3 — only on the .0 row, spanning 5 rows vertically.
                // Each field is wrapped in a StackPane that fills the span height,
                // so the TextField stays at its natural height and centres visually.
                if (hasArtifact && isMajorTierRow) {
                    TextField mat3Api = new TextField("\u2014");
                    mat3Api.setEditable(false);
                    mat3Api.setStyle(
                            "-fx-font-size: 12px;"
                                    + "-fx-alignment: center;"
                                    + "-fx-background-color: "
                                    + mat3FieldBg
                                    + ";"
                    );
                    mat3Api.setMaxWidth(Double.MAX_VALUE);

                    TextField mat3Manual = new TextField("");
                    mat3Manual.setPromptText("manual");
                    mat3Manual.setStyle(
                            "-fx-font-size: 12px;"
                                    + "-fx-alignment: center;"
                                    + "-fx-background-color: "
                                    + mat3FieldBg
                                    + ";"
                    );
                    mat3Manual.setMaxWidth(Double.MAX_VALUE);

                    String artifactItemId =
                            LocalMarketPriceService.artifactItemId(
                                    itemName,
                                    artifactType.name(),
                                    tierNum
                            );

                    if (artifactItemId != null) {
                        LocalMarketPriceService.bindMinSell(
                                mat3Api,
                                buy3,
                                artifactItemId
                        );
                    } else {
                        mat3Api.setTooltip(new Tooltip(
                                "Exact artifact item ID is not mapped yet. "
                                        + "Use the Manual field."
                        ));
                    }

                    majorArtifactApi = mat3Api;
                    majorArtifactManual = mat3Manual;

                    StackPane apiPane = new StackPane(mat3Api);
                    apiPane.setAlignment(Pos.CENTER);
                    apiPane.setMaxWidth(Double.MAX_VALUE);
                    apiPane.setMaxHeight(Double.MAX_VALUE);
                    apiPane.setStyle(
                            "-fx-background-color: " + mat3FieldBg + ";"
                    );

                    StackPane manualPane =
                            new StackPane(mat3Manual);
                    manualPane.setAlignment(Pos.CENTER);
                    manualPane.setMaxWidth(Double.MAX_VALUE);
                    manualPane.setMaxHeight(Double.MAX_VALUE);
                    manualPane.setStyle(
                            "-fx-background-color: " + mat3FieldBg + ";"
                    );

                    GridPane.setRowSpan(apiPane, 5);
                    GridPane.setRowSpan(manualPane, 5);
                    GridPane.setFillHeight(apiPane, true);
                    GridPane.setFillHeight(manualPane, true);
                    GridPane.setValignment(
                            apiPane,
                            javafx.geometry.VPos.CENTER
                    );
                    GridPane.setValignment(
                            manualPane,
                            javafx.geometry.VPos.CENTER
                    );

                    grid.add(apiPane, mat3StartCol, gridRow);
                    grid.add(manualPane, mat3StartCol + 1, gridRow);
                }


                // Additional special material — Shapeshifter alchemy component.
                // Like artifacts, the component is non-enchanted, so one API /
                // Manual pair is shared by all five enchantment rows of a tier.
                if (hasAdditionalRequirement && isMajorTierRow) {
                    String additionalFieldBg = "#fff4df";
                    TextField additionalApi = new TextField("—");
                    additionalApi.setEditable(false);
                    additionalApi.setStyle(
                            "-fx-font-size: 12px;"
                                    + "-fx-alignment: center;"
                                    + "-fx-background-color: "
                                    + additionalFieldBg
                                    + ";"
                    );
                    additionalApi.setMaxWidth(Double.MAX_VALUE);

                    TextField additionalManual = new TextField("");
                    additionalManual.setPromptText("manual");
                    additionalManual.setStyle(
                            "-fx-font-size: 12px;"
                                    + "-fx-alignment: center;"
                                    + "-fx-background-color: "
                                    + additionalFieldBg
                                    + ";"
                    );
                    additionalManual.setMaxWidth(Double.MAX_VALUE);

                    String additionalItemId = LocalMarketPriceService
                            .additionalRequirementItemId(itemName, tierNum);
                    if (additionalItemId != null) {
                        LocalMarketPriceService.bindMinSell(
                                additionalApi,
                                buy4,
                                additionalItemId
                        );
                    } else {
                        additionalApi.setTooltip(new Tooltip(
                                "Additional material item ID is not mapped. "
                                        + "Use the Manual field."
                        ));
                    }

                    majorAdditionalApi = additionalApi;
                    majorAdditionalManual = additionalManual;

                    StackPane additionalApiPane =
                            new StackPane(additionalApi);
                    additionalApiPane.setAlignment(Pos.CENTER);
                    additionalApiPane.setMaxWidth(Double.MAX_VALUE);
                    additionalApiPane.setMaxHeight(Double.MAX_VALUE);
                    additionalApiPane.setStyle(
                            "-fx-background-color: "
                                    + additionalFieldBg
                                    + ";"
                    );

                    StackPane additionalManualPane =
                            new StackPane(additionalManual);
                    additionalManualPane.setAlignment(Pos.CENTER);
                    additionalManualPane.setMaxWidth(Double.MAX_VALUE);
                    additionalManualPane.setMaxHeight(Double.MAX_VALUE);
                    additionalManualPane.setStyle(
                            "-fx-background-color: "
                                    + additionalFieldBg
                                    + ";"
                    );

                    GridPane.setRowSpan(additionalApiPane, 5);
                    GridPane.setRowSpan(additionalManualPane, 5);
                    GridPane.setFillHeight(additionalApiPane, true);
                    GridPane.setFillHeight(additionalManualPane, true);
                    GridPane.setValignment(
                            additionalApiPane,
                            javafx.geometry.VPos.CENTER
                    );
                    GridPane.setValignment(
                            additionalManualPane,
                            javafx.geometry.VPos.CENTER
                    );

                    grid.add(additionalApiPane, mat4StartCol, gridRow);
                    grid.add(
                            additionalManualPane,
                            mat4StartCol + 1,
                            gridRow
                    );
                }

                if (hasArtifact && majorArtifactApi != null) {
                    rowApiFields.add(majorArtifactApi);
                    rowManualFields.add(majorArtifactManual);
                    rowMaterialQuantities.add(
                            LocalMarketPriceService.artifactQuantity(
                                    itemName,
                                    tierNum
                            )
                    );
                    rowMaterialReturnable.add(
                            LocalMarketPriceService.artifactReceivesReturns(
                                    itemName,
                                    artifactType.name(),
                                    tierNum
                            )
                    );
                }

                if (hasAdditionalRequirement
                        && majorAdditionalApi != null) {
                    rowApiFields.add(majorAdditionalApi);
                    rowManualFields.add(majorAdditionalManual);
                    rowMaterialQuantities.add(
                            LocalMarketPriceService
                                    .additionalRequirementQuantity(
                                            itemName,
                                            tierNum
                                    )
                    );
                    rowMaterialReturnable.add(
                            LocalMarketPriceService
                                    .additionalRequirementReceivesReturns(
                                            itemName,
                                            tierNum
                                    )
                    );
                }


                // Result value columns — Focus Costs × quantity, updates live
                String[] resultValues = {
                        "\u2014", "\u2014", "", "", "\u2014",
                        "\u2014", "\u2014", "\u2014", "\u2014"
                };

                javafx.beans.property.DoubleProperty rowBatchCost =
                        new javafx.beans.property.SimpleDoubleProperty(
                                Double.NaN
                        );

                java.util.function.LongSupplier requestedCrafts = () -> {
                    try {
                        return Math.max(
                                1L,
                                Long.parseLong(
                                        quantityField
                                                .getText()
                                                .trim()
                                                .replace(",", "")
                                                .replace("_", "")
                                )
                        );
                    } catch (NumberFormatException ignored) {
                        return 1L;
                    }
                };

                Label costCell = makeDataLabel("\u2014");
                costCell.setStyle(
                        "-fx-font-size: 13px;"
                                + "-fx-text-fill: #0f766e;"
                                + "-fx-font-weight: bold;"
                );

                TextField sellPriceApi = new TextField("...");
                sellPriceApi.setEditable(false);
                sellPriceApi.setMaxWidth(Double.MAX_VALUE);
                sellPriceApi.setStyle(
                        "-fx-font-size: 12px;"
                                + "-fx-alignment: center;"
                                + "-fx-background-color: #eef2ff;"
                                + "-fx-text-fill: #3730a3;"
                                + "-fx-font-weight: bold;"
                );

                String craftedItemId =
                        LocalMarketPriceService.gearItemId(
                                itemName,
                                tierNum,
                                enchantNum
                        );

                TextField demandCell = new TextField("...");
                demandCell.setEditable(false);
                demandCell.setMaxWidth(Double.MAX_VALUE);
                demandCell.setStyle(
                        "-fx-font-size: 12px;"
                                + "-fx-alignment: center;"
                                + "-fx-background-color: #ecfdf5;"
                                + "-fx-text-fill: #166534;"
                                + "-fx-font-weight: bold;"
                );

                if (craftedItemId == null) {
                    sellPriceApi.setText("\u2014");
                    demandCell.setText("\u2014");
                    String missingMapping =
                            "No market item ID mapping for " + itemName;
                    sellPriceApi.setTooltip(new Tooltip(missingMapping));
                    demandCell.setTooltip(new Tooltip(missingMapping));
                } else {
                    LocalMarketPriceService.bindMinSell(
                            sellPriceApi,
                            sellLocation,
                            sellQuality,
                            craftedItemId
                    );
                    LocalMarketPriceService.bindDemand(
                            demandCell,
                            sellLocation,
                            sellQuality,
                            demandType,
                            craftedItemId
                    );
                }

                Label booksCell = makeDataLabel("—");
                Label fameCell = makeDataLabel("—");
                Label spfCell = makeDataLabel("—");
                Label profitCell = makeDataLabel("\u2014");
                Label roiCell = makeDataLabel("\u2014");

                Runnable updateJournalCells = () -> {
                    long crafts = requestedCrafts.getAsLong();
                    long totalFame = CraftingJournalData.totalFame(
                            craftedItemId,
                            enchantNum,
                            crafts
                    );

                    fameCell.setText(totalFame <= 0L
                            ? "—"
                            : String.format(
                            java.util.Locale.US,
                            "%,d",
                            totalFame
                    ));
                    fameCell.setTooltip(new Tooltip(
                            "Craft fame per item: "
                                    + String.format(
                                    java.util.Locale.US,
                                    "%,d",
                                    CraftingJournalData.famePerItem(
                                            craftedItemId,
                                            enchantNum
                                    )
                            )
                                    + "\nBatch fame: "
                                    + String.format(
                                    java.util.Locale.US,
                                    "%,d",
                                    Math.max(0L, totalFame)
                            )
                    ));

                    booksCell.setText(CraftingJournalData.booksText(
                            craftedItemId,
                            tierNum,
                            totalFame
                    ));
                    booksCell.setTooltip(new Tooltip(
                            CraftingJournalData.booksTooltip(
                                    craftedItemId,
                                    tierNum,
                                    totalFame
                            )
                    ));
                };

                Runnable updateCostCell = () -> {
                    double grossReturnableCostPerCraft = 0.0;
                    double nonReturnableCostPerCraft = 0.0;
                    boolean hasEveryPrice = true;

                    for (int priceIndex = 0;
                         priceIndex < rowApiFields.size();
                         priceIndex++) {

                        TextField api = rowApiFields.get(priceIndex);
                        TextField manual = rowManualFields.get(priceIndex);

                        if (!LocalMarketPriceService.hasEffectivePrice(
                                api,
                                manual
                        )) {
                            hasEveryPrice = false;
                            break;
                        }

                        double lineCost =
                                LocalMarketPriceService
                                        .effectiveDisplayedPrice(api, manual)
                                        * rowMaterialQuantities.get(priceIndex);

                        boolean receivesReturns =
                                priceIndex < rowMaterialReturnable.size()
                                        && rowMaterialReturnable.get(priceIndex);

                        if (receivesReturns) {
                            grossReturnableCostPerCraft += lineCost;
                        } else {
                            nonReturnableCostPerCraft += lineCost;
                        }
                    }

                    if (!hasEveryPrice || rowApiFields.isEmpty()) {
                        costCell.setText("\u2014");
                        costCell.setTooltip(null);
                        spfCell.setText("—");
                        rowBatchCost.set(Double.NaN);
                        return;
                    }

                    long crafts = requestedCrafts.getAsLong();
                    double rrr = reqPanel.getRrrFraction();

                    double returnedMaterialCostPerCraft =
                            grossReturnableCostPerCraft * (1.0 - rrr)
                                    + nonReturnableCostPerCraft;

                    double feePer100 =
                            com.albionservant.data.CraftingNutritionData
                                    .parseNonNegative(
                                            stationFeeField.getText()
                                    );

                    double nutritionPerItem =
                            com.albionservant.data.CraftingNutritionData
                                    .nutritionCost(craftedItemId);

                    double stationCost =
                            com.albionservant.data.CraftingNutritionData
                                    .stationFee(
                                            craftedItemId,
                                            feePer100,
                                            crafts
                                    );

                    if (!Double.isFinite(stationCost)) {
                        costCell.setText("\u2014");
                        costCell.setTooltip(new Tooltip(
                                "Missing nutritionCost for "
                                        + craftedItemId
                        ));
                        rowBatchCost.set(Double.NaN);
                        return;
                    }

                    double materialsAfterReturns =
                            returnedMaterialCostPerCraft * crafts;
                    double batchCost = materialsAfterReturns + stationCost;

                    rowBatchCost.set(batchCost);
                    costCell.setText(
                            LocalMarketPriceService.formatSilver(batchCost)
                    );

                    String nutritionText = Double.isFinite(nutritionPerItem)
                            ? LocalMarketPriceService.formatSilver(
                            nutritionPerItem
                    )
                            : "\u2014";

                    costCell.setTooltip(new Tooltip(
                            "Materials after RRR: "
                                    + LocalMarketPriceService.formatSilver(
                                    materialsAfterReturns
                            )
                                    + "\nRRR: "
                                    + String.format(
                                    java.util.Locale.US,
                                    "%.2f%%",
                                    rrr * 100.0
                            )
                                    + "\nNutrition/item: "
                                    + nutritionText
                                    + "\nStation fee: "
                                    + LocalMarketPriceService.formatSilver(
                                    stationCost
                            )
                    ));

                    int liveSpec = specsPanel != null
                            ? lookupGearSpec(specsPanel, itemName)
                            : 0;
                    long focusPerItem = com.albionservant.data
                            .FocusCostCalculator
                            .compute(tierNum, enchantNum, rowTotalMats, liveSpec);
                    long totalFocus = focusPerItem * crafts;

                    if (craftingFocus != null
                            && craftingFocus.isSelected()
                            && totalFocus > 0L) {
                        double lpb = rrr >= 1.0
                                ? 0.0
                                : 100.0 * rrr / Math.max(0.000001, 1.0 - rrr);
                        double noFocusLpb = Math.max(0.0, lpb - 59.0);
                        double noFocusRrr = noFocusLpb / (100.0 + noFocusLpb);
                        double focusSavings = grossReturnableCostPerCraft
                                * Math.max(0.0, rrr - noFocusRrr)
                                * crafts;

                        spfCell.setText(String.format(
                                java.util.Locale.US,
                                "%.2f",
                                focusSavings / totalFocus
                        ));
                        spfCell.setTooltip(new Tooltip(
                                "Silver per Focus uses only the extra profit "
                                        + "created by Focus returns."
                                        + "\nFocus savings: "
                                        + LocalMarketPriceService.formatSilver(
                                        focusSavings
                                )
                                        + "\nFocus spent: "
                                        + String.format(
                                        java.util.Locale.US,
                                        "%,d",
                                        totalFocus
                                )
                        ));
                    } else {
                        spfCell.setText("\u2014");
                        spfCell.setTooltip(new Tooltip(
                                "Enable Crafting Focus to calculate Silver per Focus."
                        ));
                    }
                };

                Runnable updateProfitCells = () -> {
                    double batchCost = rowBatchCost.get();

                    if (!Double.isFinite(batchCost)
                            || !LocalMarketPriceService
                            .hasEffectivePrice(
                                    sellPriceApi,
                                    null
                            )) {

                        profitCell.setText("\u2014");
                        roiCell.setText("\u2014");
                        return;
                    }

                    double sellPricePerItem =
                            LocalMarketPriceService
                                    .effectiveDisplayedPrice(
                                            sellPriceApi,
                                            null
                                    );

                    double revenue =
                            sellPricePerItem
                                    * requestedCrafts.getAsLong();

                    double profit = revenue - batchCost;

                    profitCell.setText(
                            LocalMarketPriceService.formatSilver(
                                    profit
                            )
                    );

                    profitCell.setStyle(
                            "-fx-font-size: 13px;"
                                    + "-fx-font-weight: bold;"
                                    + "-fx-text-fill: "
                                    + (profit >= 0.0
                                    ? "#15803d;"
                                    : "#5d636d;")
                    );

                    if (batchCost <= 0.0) {
                        roiCell.setText("\u2014");
                    } else {
                        roiCell.setText(
                                String.format(
                                        java.util.Locale.US,
                                        "%.1f%%",
                                        profit / batchCost * 100.0
                                )
                        );
                    }
                };

                for (TextField api : rowApiFields) {
                    api.textProperty().addListener(
                            (observable, oldValue, newValue) ->
                                    updateCostCell.run()
                    );
                }

                for (TextField manual : rowManualFields) {
                    manual.textProperty().addListener(
                            (observable, oldValue, newValue) ->
                                    updateCostCell.run()
                    );
                }

                quantityField.textProperty().addListener(
                        (observable, oldValue, newValue) -> {
                            updateCostCell.run();
                            updateProfitCells.run();
                            updateJournalCells.run();
                        }
                );
                stationFeeField.textProperty().addListener(
                        (observable, oldValue, newValue) ->
                                updateCostCell.run()
                );

                reqPanel.rrrFractionProperty().addListener(
                        (observable, oldValue, newValue) ->
                                updateCostCell.run()
                );

                if (craftingFocus != null) {
                    craftingFocus.selectedProperty().addListener(
                            (observable, oldValue, newValue) ->
                                    updateCostCell.run()
                    );
                }


                sellPriceApi.textProperty().addListener(
                        (observable, oldValue, newValue) ->
                                updateProfitCells.run()
                );

                rowBatchCost.addListener(
                        (observable, oldValue, newValue) ->
                                updateProfitCells.run()
                );

                updateCostCell.run();
                updateProfitCells.run();
                updateJournalCells.run();

                for (int i = 0; i < resultValues.length; i++) {
                    if (i == 0) {
                        grid.add(
                                demandCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 1) {
                        grid.add(
                                costCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 2) {
                        grid.add(
                                sellPriceApi,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 3) {
                        Label focusCell = new Label("\u2014");
                        focusCell.setStyle(
                                "-fx-font-size: 13px;"
                                        + "-fx-text-fill: #818cf8;"
                                        + "-fx-font-weight: bold;"
                        );
                        focusCell.setMaxWidth(Double.MAX_VALUE);
                        focusCell.setAlignment(Pos.CENTER);

                        final int fTierNum = tierNum;
                        final int fEnchantNum = enchantNum;
                        final int fTotalMats = rowTotalMats;

                        Runnable updateFocusCell = () -> {
                            int liveSpec = specsPanel != null
                                    ? lookupGearSpec(
                                    specsPanel,
                                    itemName
                            )
                                    : 0;

                            long livePerItem =
                                    com.albionservant.data
                                            .FocusCostCalculator
                                            .compute(
                                                    fTierNum,
                                                    fEnchantNum,
                                                    fTotalMats,
                                                    liveSpec
                                            );

                            focusCell.setText(
                                    String.format(
                                            "%,d",
                                            livePerItem
                                                    * requestedCrafts
                                                    .getAsLong()
                                    )
                            );
                        };

                        updateFocusCell.run();

                        quantityField.textProperty().addListener(
                                (observable, oldValue, newValue) ->
                                        updateFocusCell.run()
                        );

                        grid.add(
                                focusCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 4) {
                        grid.add(
                                booksCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 5) {
                        grid.add(
                                fameCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 6) {
                        grid.add(
                                spfCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 7) {
                        grid.add(
                                profitCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else if (i == 8) {
                        grid.add(
                                roiCell,
                                resultStartCol + i,
                                gridRow
                        );

                    } else {
                        grid.add(
                                makeDataLabel(resultValues[i]),
                                resultStartCol + i,
                                gridRow
                        );
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
                            "-fx-border-color: #d6d9dd;" +
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
                fallback.setStyle("-fx-font-size: 48px; -fx-text-fill: #9ba0a8;");
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
                            fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");
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
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #aeb3ba; -fx-font-size: 13px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private Label makeHeaderLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5058; -fx-font-size: 13px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private Label makeSubHeaderLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #7d838c; -fx-font-size: 12px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private Label makeDataLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size: 13px;");
            l.setMaxWidth(Double.MAX_VALUE);
            l.setMinWidth(Region.USE_PREF_SIZE);
            l.setAlignment(Pos.CENTER);
            return l;
        }

        private double preferredCraftColumnWidth(
                int column,
                int resultStartCol,
                int mat3StartCol,
                int mat4StartCol,
                boolean hasArtifact,
                boolean hasAdditionalRequirement
        ) {
            if (column == 0) {
                return 68.0;
            }

            if (hasArtifact
                    && (column == mat3StartCol
                    || column == mat3StartCol + 1)) {
                return 150.0;
            }

            if (hasAdditionalRequirement
                    && (column == mat4StartCol
                    || column == mat4StartCol + 1)) {
                return 150.0;
            }

            if (column < resultStartCol) {
                return 96.0;
            }

            int resultIndex = column - resultStartCol;

            return switch (resultIndex) {
                case 0 -> 96.0;   // Demand
                case 1 -> 122.0;  // Costs
                case 2 -> 108.0;  // Sell Price
                case 3 -> 112.0;  // Focus Costs
                case 4 -> 124.0;  // Books
                case 5 -> 108.0;  // Fame
                case 6 -> 88.0;   // SPF
                case 7 -> 122.0;  // Profit
                case 8 -> 86.0;   // ROI
                default -> 96.0;
            };
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
        btn.getStyleClass().add("nav-tree-button");

        if (isSelected) {
            btn.getStyleClass().add("nav-tree-button-selected");
        } else if (isSidePanel) {
            btn.getStyleClass().add("nav-tree-button-traversed");
        } else {
            btn.getStyleClass().add("nav-tree-button-active");
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
        private static final double FOOD_CONTROL_WIDTH = 190.0;

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
            redHeader.setStyle("-fx-background-color: #4b5058;");
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
            backBtn.setMinWidth(82);
            backBtn.setPrefWidth(82);
            backBtn.setMaxWidth(82);
            backBtn.getStyleClass().add("button-primary");
            backBtn.setOnAction(e -> onBack.run());

            redHeader.getChildren().addAll(breadcrumb, tierLbl, hSpacer, backBtn);

            if (recipe == null) {
                Label missing = new Label("Missing food recipe: " + itemName);
                missing.setStyle("-fx-font-size: 16px; -fx-text-fill: #4b5058; -fx-padding: 24;");
                getChildren().addAll(redHeader, missing);
                return;
            }

            VBox whiteContent = new VBox(22);
            whiteContent.setPadding(new Insets(20, 40, 40, 40));
            whiteContent.setStyle("-fx-background-color: #f6f7f8;");
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
            quantityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            TextField quantity = new TextField("200");
            quantity.setStyle("-fx-font-size: 14px;");
            quantity.setMinWidth(120);
            quantity.setPrefWidth(120);
            quantity.setMaxWidth(120);

            Label stationFeeLbl = new Label("Fee / 100 Nutrition:");
            stationFeeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            TextField stationFee = new TextField("999");
            stationFee.setStyle("-fx-font-size: 14px;");
            stationFee.setMinWidth(120);
            stationFee.setPrefWidth(120);
            stationFee.setMaxWidth(120);

            Label demandTypeLbl = new Label("Demand Type:");
            demandTypeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> demandType = new ComboBox<>();
            demandType.getItems().addAll("24h", "7d", "4w");
            demandType.setValue("24h");
            lockFoodCombo(demandType);

            CheckBox craftingFocus = new CheckBox("Crafting Focus");

            CheckBox dailyBonus = new CheckBox("Daily Bonus");
            dailyBonus.setStyle("-fx-font-size: 13px;");

            ComboBox<String> dailyBonusValue = new ComboBox<>();
            dailyBonusValue.getItems().addAll("10%", "20%");
            dailyBonusValue.setValue("10%");
            dailyBonusValue.setMinWidth(82);
            dailyBonusValue.setPrefWidth(82);
            dailyBonusValue.setMaxWidth(82);
            dailyBonusValue.setDisable(true);

            dailyBonus.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                    dailyBonusValue.setDisable(!isSelected)
            );

            HBox dailyBonusRow = new HBox(8, dailyBonus, dailyBonusValue);
            dailyBonusRow.setAlignment(Pos.CENTER_LEFT);
            craftingFocus.setStyle("-fx-font-size: 13px;");

            Label bonusCraftLbl = new Label("Bonus Craft:");
            bonusCraftLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> bonusCraft = new ComboBox<>();
            bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
            bonusCraft.setValue("Royal City");
            lockFoodCombo(bonusCraft);

            VBox hoSection = new VBox(6);
            hoSection.setVisible(false);
            hoSection.setManaged(false);

            Label hoQualityLbl = new Label("Hideout Quality:");
            hoQualityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> hoQuality = new ComboBox<>();
            hoQuality.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Q5", "Q6");
            hoQuality.setValue("Q5");
            lockFoodCombo(hoQuality);

            Label hoPowerLbl = new Label("Hideout Power Level:");
            hoPowerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

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
                    dailyBonusRow,
                    bonusCraftLbl, bonusCraft,
                    reqPanel.getRrrLabel(),
                    hoSection
            );

            VBox cfgRight = new VBox(8);
            cfgRight.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cfgRight, Priority.ALWAYS);

            Label buyLbl = new Label("Food Materials:");
            buyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> buyLocation = createFoodCityCombo();
            lockFoodCombo(buyLocation);

            Label sellLbl = new Label("Sell Location:");
            sellLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> sellLocation = createFoodCityCombo();
            sellLocation.setValue("Caerleon");
            lockFoodCombo(sellLocation);

            Label note = new Label("Result table uses cheapest entered material price and highest entered sell price.");
            note.setWrapText(true);
            note.setStyle("-fx-font-size: 12px; -fx-text-fill: #707782;");

            cfgRight.getChildren().addAll(buyLbl, buyLocation, sellLbl, sellLocation, note);

            configSection.getChildren().addAll(cfgLeft, cfgCenter, cfgRight);

            List<List<PricePair>> ingredientPrices = new ArrayList<>();
            List<List<PricePair>> saucePrices = new ArrayList<>();
            List<PricePair> sellPrices = new ArrayList<>();
            List<ResultRow> resultRows = new ArrayList<>();

            VBox ingredientPriceTable = buildFoodPriceTable(recipe, ingredientPrices, saucePrices, sellPrices);
            VBox resultTable = buildFoodResultTable(
                    recipe,
                    quantity,
                    specsPanel,
                    category,
                    ingredientPrices,
                    saucePrices,
                    sellPrices,
                    resultRows
            );

            for (ResultRow row : resultRows) {
                String foodItemId = LocalMarketPriceService.foodItemId(
                        recipe.name(),
                        row.enchant()
                );
                LocalMarketPriceService.bindDemand(
                        row.demand(),
                        sellLocation,
                        demandType,
                        foodItemId
                );
            }

            Runnable updateResults = () -> updateFoodResults(
                    recipe,
                    quantity,
                    stationFee,
                    specsPanel,
                    category,
                    bonusCraft,
                    craftingFocus,
                    dailyBonus,
                    dailyBonusValue,
                    hoQuality,
                    hoPower,
                    ingredientPrices,
                    saucePrices,
                    sellPrices,
                    resultRows
            );

            quantity.textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            stationFee.textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());

            craftingFocus.selectedProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            bonusCraft.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            dailyBonus.selectedProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            dailyBonusValue.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            hoQuality.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            hoPower.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());

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

            reqPanel.bindControls(quantity, bonusCraft, craftingFocus, dailyBonus, dailyBonusValue, hoQuality, hoPower);

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
            contentScroll.setStyle("-fx-background-color: #f6f7f8;");

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
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setStyle("-fx-background-color: #f4f5f6; -fx-padding: 15; -fx-background-radius: 8;");
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
                Label header = makeFoodHeader(sauce + " x" + FoodRecipeData.getFishSauceQuantityPerBatch(recipe));
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
                cityLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2b2f35;");
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
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setStyle(
                    "-fx-background-color: #1b1e22;" +
                            "-fx-border-color: #50565f;" +
                            "-fx-border-width: 1;" +
                            "-fx-padding: 0;"
            );
            grid.setVgap(0);
            grid.setHgap(0);

            String[] headers = {"Enchant", "Demand", "Profit", "SPF", "Gain", "Cost", "Focus Cost"};

            for (int i = 0; i < headers.length; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setMinWidth(i == 0 ? 76.0 : 112.0);
                cc.setPrefWidth(i == 0 ? 82.0 : 126.0);
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
                TextField stationFeeField,
                SpecsPanel specsPanel,
                String category,
                ComboBox<String> bonusCraft,
                CheckBox craftingFocus,
                CheckBox dailyBonus,
                ComboBox<String> dailyBonusValue,
                ComboBox<String> hoQuality,
                ComboBox<Integer> hoPower,
                List<List<PricePair>> ingredientPrices,
                List<List<PricePair>> saucePrices,
                List<PricePair> sellPrices,
                List<ResultRow> resultRows
        ) {
            long quantity = parseLong(quantityField.getText(), 1);
            int spec = specsPanel != null ? CraftPanel.lookupFoodSpec(specsPanel, category) : 0;
            int batchSize = Math.max(1, recipe.batchSize());

            double lpb = computeFoodDailyLpb(
                    bonusCraft,
                    craftingFocus,
                    dailyBonus,
                    dailyBonusValue,
                    hoQuality,
                    hoPower
            );

            double rrr = lpb / (100.0 + lpb);
            double netCostMultiplier = 1.0 - rrr;

            double ingredientCostPerItem = 0.0;

            for (int i = 0; i < recipe.ingredients().size(); i++) {
                Ingredient ingredient = recipe.ingredients().get(i);
                double bestPrice = bestBuyPrice(ingredientPrices.get(i));

                ingredientCostPerItem += bestPrice * (ingredient.quantity() / (double) batchSize);
            }

            double bestSell = bestSellPrice(sellPrices);

            double feePer100 =
                    com.albionservant.data.CraftingNutritionData
                            .parseNonNegative(stationFeeField.getText());
            for (ResultRow row : resultRows) {
                double costPerItem = ingredientCostPerItem;

                if (row.enchant() > 0) {
                    int sauceIndex = row.enchant() - 1;

                    if (sauceIndex >= 0 && sauceIndex < saucePrices.size()) {
                        costPerItem += bestBuyPrice(saucePrices.get(sauceIndex)) * FoodRecipeData.getFishSauceQuantityPerItem(recipe);
                    }
                }

                double stationCost =
                        com.albionservant.data.CraftingNutritionData
                                .stationFeeForDisplayName(
                                        recipe.name(),
                                        row.enchant(),
                                        feePer100,
                                        quantity
                                );

                if (!Double.isFinite(stationCost)) {
                    row.cost().setText("-");
                    row.cost().setTooltip(new Tooltip(
                            "Missing nutritionCost for "
                                    + recipe.name()
                                    + " enchant "
                                    + row.enchant()
                    ));
                    row.profit().setText("-");
                    row.spf().setText("-");
                    continue;
                }

                double materialCostAfterReturns =
                        costPerItem * netCostMultiplier * quantity;
                double totalCost = materialCostAfterReturns + stationCost;
                row.cost().setTooltip(new Tooltip(
                        "Materials after RRR: "
                                + formatSilver(materialCostAfterReturns)
                                + "\nRRR: "
                                + String.format(
                                java.util.Locale.US,
                                "%.2f%%",
                                rrr * 100.0
                        )
                                + "\nStation fee: "
                                + formatSilver(stationCost)
                ));
                double totalGain = bestSell * quantity;
                double totalProfit = totalGain - totalCost;

                long focusPerItem = com.albionservant.data.FocusCostCalculator.forFood(recipe, row.enchant(), spec);
                long totalFocus = focusPerItem * quantity;

                row.cost().setText(formatSilver(totalCost));
                row.gain().setText(formatSilver(totalGain));
                row.profit().setText(formatSilver(totalProfit));
                row.focus().setText(String.format("%,d", totalFocus));

                if (craftingFocus != null
                        && craftingFocus.isSelected()
                        && totalFocus > 0) {
                    double noFocusLpb = Math.max(0.0, lpb - 59.0);
                    double noFocusRrr = noFocusLpb / (100.0 + noFocusLpb);
                    double focusSavings = costPerItem
                            * Math.max(0.0, rrr - noFocusRrr)
                            * quantity;

                    row.spf().setText(String.format(
                            java.util.Locale.US,
                            "%.2f",
                            focusSavings / totalFocus
                    ));
                    row.spf().setTooltip(new Tooltip(
                            "Silver per Focus uses only the extra profit "
                                    + "created by Focus returns."
                                    + "\nFocus savings: "
                                    + formatSilver(focusSavings)
                                    + "\nFocus spent: "
                                    + String.format(
                                    java.util.Locale.US,
                                    "%,d",
                                    totalFocus
                            )
                    ));
                } else {
                    row.spf().setText("-");
                    row.spf().setTooltip(new Tooltip(
                            "Enable Crafting Focus to calculate Silver per Focus."
                    ));
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


        private double computeFoodDailyLpb(
                ComboBox<String> bonusCraft,
                CheckBox craftingFocus,
                CheckBox dailyBonus,
                ComboBox<String> dailyBonusValue,
                ComboBox<String> hoQuality,
                ComboBox<Integer> hoPower
        ) {
            double lpb;

            String loc = bonusCraft != null && bonusCraft.getValue() != null
                    ? bonusCraft.getValue()
                    : "Royal City";

            switch (loc) {
                case "Royal Island" -> lpb = 0.0;
                case "Royal City" -> lpb = 18.0;
                case "Royal City + Bonus" -> lpb = 33.0;
                case "HO" -> {
                    int zq = parseFoodHoQuality(hoQuality);
                    int pl = parseFoodHoPower(hoPower);

                    lpb = 18.0
                            + (pl - 1)
                            + (2.0 + zq) * 5.0
                            + (pl - 1) * 2.0;
                }
                default -> lpb = 18.0;
            }

            if (craftingFocus != null && craftingFocus.isSelected()) {
                lpb += 59.0;
            }

            if (dailyBonus != null && dailyBonus.isSelected()) {
                lpb += parseFoodDailyBonus(dailyBonusValue);
            }

            return lpb;
        }

        private double parseFoodDailyBonus(ComboBox<String> dailyBonusValue) {
            if (dailyBonusValue == null || dailyBonusValue.getValue() == null) {
                return 10.0;
            }

            String digits = dailyBonusValue.getValue().replaceAll("[^0-9]", "");

            if (digits.isEmpty()) {
                return 10.0;
            }

            try {
                return Double.parseDouble(digits);
            } catch (NumberFormatException ignored) {
                return 10.0;
            }
        }

        private int parseFoodHoQuality(ComboBox<String> hoQuality) {
            if (hoQuality == null || hoQuality.getValue() == null) {
                return 1;
            }

            try {
                return Integer.parseInt(hoQuality.getValue().replace("Q", "").trim());
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }

        private int parseFoodHoPower(ComboBox<Integer> hoPower) {
            if (hoPower == null || hoPower.getValue() == null) {
                return 1;
            }

            return hoPower.getValue();
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
                fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");

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
                            fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");
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
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5058; -fx-font-size: 12px;");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            return label;
        }

        private static Label makeFoodSubHeader(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #707782; -fx-font-size: 11px;");
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
                            "-fx-border-color: #50565f;" +
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
            label.setMinWidth(Region.USE_PREF_SIZE);
            label.setAlignment(Pos.CENTER);
            return label;
        }

        private static TextField makeDarkInput() {
            TextField field = new TextField("...");
            field.setEditable(false);
            field.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-prompt-text-fill: #9ca2ab;" +
                            "-fx-background-color: #1b1e22;" +
                            "-fx-alignment: center;" +
                            "-fx-border-color: #50565f;" +
                            "-fx-border-width: 0 1 1 0;"
            );
            field.setMaxWidth(Double.MAX_VALUE);
            field.setMinWidth(92.0);
            return field;
        }

        private static String darkCellStyle() {
            return "-fx-font-size: 12px;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-alignment: center;" +
                    "-fx-padding: 4 6 4 6;" +
                    "-fx-border-color: #50565f;" +
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
        private static final List<String> ARCANE_EXTRACTS = PotionRecipeData.ARCANE_EXTRACTS;
        private static final double POTION_CONTROL_WIDTH = 190.0;

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

        public PotionDetailSubPanel(String itemName, String breadcrumbText, Runnable onBack,
                                    SpecsPanel specsPanel, String category) {
            setPadding(new Insets(0));
            setSpacing(0);
            setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(this, Priority.ALWAYS);

            PotionRecipeData.PotionRecipe recipe = PotionRecipeData.getRecipe(itemName);

            HBox redHeader = new HBox(15);
            redHeader.setPadding(new Insets(14, 40, 14, 40));
            redHeader.setStyle("-fx-background-color: #4b5058;");
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
            backBtn.setMinWidth(82);
            backBtn.setPrefWidth(82);
            backBtn.setMaxWidth(82);
            backBtn.getStyleClass().add("button-primary");
            backBtn.setOnAction(e -> onBack.run());

            redHeader.getChildren().addAll(breadcrumb, tierLbl, hSpacer, backBtn);

            if (recipe == null) {
                Label missing = new Label("Missing potion recipe: " + itemName);
                missing.setStyle("-fx-font-size: 16px; -fx-text-fill: #4b5058; -fx-padding: 24;");
                getChildren().addAll(redHeader, missing);
                return;
            }

            VBox whiteContent = new VBox(22);
            whiteContent.setPadding(new Insets(20, 40, 40, 40));
            whiteContent.setStyle("-fx-background-color: #f6f7f8;");
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

            StackPane iconPane = buildPotionIcon(itemName, onBack);

            RequirementsCalculatorPanel reqPanel = RequirementsCalculatorPanel.forPotion(recipe);
            cfgLeft.getChildren().addAll(searchBar, iconPane, reqPanel);

            VBox cfgCenter = new VBox(8);
            cfgCenter.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cfgCenter, Priority.ALWAYS);

            Label quantityLbl = new Label("Quantity:");
            quantityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            TextField quantity = new TextField("200");
            quantity.setStyle("-fx-font-size: 14px;");
            quantity.setMinWidth(120);
            quantity.setPrefWidth(120);
            quantity.setMaxWidth(120);

            Label stationFeeLbl = new Label("Fee / 100 Nutrition:");
            stationFeeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            TextField stationFee = new TextField("999");
            stationFee.setStyle("-fx-font-size: 14px;");
            stationFee.setMinWidth(120);
            stationFee.setPrefWidth(120);
            stationFee.setMaxWidth(120);

            Label demandTypeLbl = new Label("Demand Type:");
            demandTypeLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> demandType = new ComboBox<>();
            demandType.getItems().addAll("24h", "7d", "4w");
            demandType.setValue("24h");
            lockPotionCombo(demandType);

            CheckBox craftingFocus = new CheckBox("Crafting Focus");
            craftingFocus.setStyle("-fx-font-size: 13px;");


            CheckBox dailyBonus = new CheckBox("Daily Bonus");
            dailyBonus.setStyle("-fx-font-size: 13px;");

            ComboBox<String> dailyBonusValue = new ComboBox<>();
            dailyBonusValue.getItems().addAll("10%", "20%");
            dailyBonusValue.setValue("10%");
            dailyBonusValue.setMinWidth(82);
            dailyBonusValue.setPrefWidth(82);
            dailyBonusValue.setMaxWidth(82);
            dailyBonusValue.setDisable(true);

            dailyBonus.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                    dailyBonusValue.setDisable(!isSelected)
            );

            HBox dailyBonusRow = new HBox(8, dailyBonus, dailyBonusValue);
            dailyBonusRow.setAlignment(Pos.CENTER_LEFT);
            Label bonusCraftLbl = new Label("Bonus Craft:");
            bonusCraftLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> bonusCraft = new ComboBox<>();
            bonusCraft.getItems().addAll("Royal Island", "Royal City", "Royal City + Bonus", "HO");
            bonusCraft.setValue("Royal City");
            lockPotionCombo(bonusCraft);

            VBox hoSection = new VBox(6);
            hoSection.setVisible(false);
            hoSection.setManaged(false);

            Label hoQualityLbl = new Label("Hideout Quality:");
            hoQualityLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> hoQuality = new ComboBox<>();
            hoQuality.getItems().addAll("Q1", "Q2", "Q3", "Q4", "Q5", "Q6");
            hoQuality.setValue("Q5");
            lockPotionCombo(hoQuality);

            Label hoPowerLbl = new Label("Hideout Power Level:");
            hoPowerLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<Integer> hoPower = new ComboBox<>();
            hoPower.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9);
            hoPower.setValue(5);
            lockPotionCombo(hoPower);

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
                    dailyBonusRow,
                    bonusCraftLbl, bonusCraft,
                    reqPanel.getRrrLabel(),
                    hoSection
            );

            VBox cfgRight = new VBox(8);
            cfgRight.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cfgRight, Priority.ALWAYS);

            Label buyLbl = new Label("Potion Materials:");
            buyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> buyLocation = createPotionCityCombo();
            lockPotionCombo(buyLocation);

            Label sellLbl = new Label("Sell Location:");
            sellLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3136;");

            ComboBox<String> sellLocation = createPotionCityCombo();
            sellLocation.setValue("Caerleon");
            lockPotionCombo(sellLocation);

            Label note = new Label("Result table uses cheapest entered material price and highest entered sell price.");
            note.setWrapText(true);
            note.setStyle("-fx-font-size: 12px; -fx-text-fill: #707782;");

            cfgRight.getChildren().addAll(buyLbl, buyLocation, sellLbl, sellLocation, note);

            configSection.getChildren().addAll(cfgLeft, cfgCenter, cfgRight);

            List<List<PricePair>> ingredientPrices = new ArrayList<>();
            List<PricePair> trackingPrices = new ArrayList<>();
            List<List<PricePair>> extractPrices = new ArrayList<>();
            List<PricePair> sellPrices = new ArrayList<>();
            List<ResultRow> resultRows = new ArrayList<>();

            VBox ingredientPriceTable = buildPotionPriceTable(recipe, ingredientPrices, trackingPrices, extractPrices, sellPrices);
            VBox resultTable = buildPotionResultTable(resultRows);

            for (ResultRow row : resultRows) {
                String potionItemId = LocalMarketPriceService.potionItemId(
                        recipe.name(),
                        row.enchant()
                );
                LocalMarketPriceService.bindDemand(
                        row.demand(),
                        sellLocation,
                        demandType,
                        potionItemId
                );
            }

            Runnable updateResults = () -> updatePotionResults(
                    recipe,
                    quantity,
                    stationFee,
                    specsPanel,
                    category,
                    bonusCraft,
                    craftingFocus,
                    dailyBonus,
                    dailyBonusValue,
                    hoQuality,
                    hoPower,
                    ingredientPrices,
                    trackingPrices,
                    extractPrices,
                    sellPrices,
                    resultRows
            );

            quantity.textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            stationFee.textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());

            craftingFocus.selectedProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            bonusCraft.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            dailyBonus.selectedProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            dailyBonusValue.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            hoQuality.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            hoPower.valueProperty().addListener((obs, oldVal, newVal) -> updateResults.run());

            for (List<PricePair> materialPairs : ingredientPrices) {
                for (PricePair pair : materialPairs) {
                    pair.api().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                    pair.manual().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                }
            }

            for (PricePair pair : trackingPrices) {
                pair.api().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                pair.manual().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            }

            for (List<PricePair> extractPairs : extractPrices) {
                for (PricePair pair : extractPairs) {
                    pair.api().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                    pair.manual().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                }
            }

            for (PricePair pair : sellPrices) {
                pair.api().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
                pair.manual().textProperty().addListener((obs, oldVal, newVal) -> updateResults.run());
            }

            whiteContent.getChildren().addAll(configSection, ingredientPriceTable, resultTable);

            reqPanel.bindControls(quantity, bonusCraft, craftingFocus, dailyBonus, dailyBonusValue, hoQuality, hoPower);

            int totalPerBatch = 0;
            for (Object ingObj : recipe.ingredients()) {
                PotionRecipeData.Ingredient ingredient = (PotionRecipeData.Ingredient) ingObj;
                totalPerBatch += ingredient.quantity();
            }

            if (recipe.hasTrackingIngredient()) {
                totalPerBatch += 1;
            }

            int approxMatsPerItem = (int) Math.ceil(totalPerBatch / (double) Math.max(1, recipe.batchSize()));

            reqPanel.setFocusContext(
                    recipe.tier(),
                    approxMatsPerItem,
                    () -> specsPanel != null ? CraftPanel.lookupPotionSpec(specsPanel, category) : 0
            );

            updateResults.run();

            ScrollPane contentScroll = new ScrollPane(whiteContent);
            contentScroll.setFitToWidth(true);
            contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            contentScroll.setStyle("-fx-background-color: #f6f7f8;");

            VBox.setVgrow(contentScroll, Priority.ALWAYS);

            getChildren().addAll(redHeader, contentScroll);
        }

        private VBox buildPotionPriceTable(
                PotionRecipeData.PotionRecipe recipe,
                List<List<PricePair>> ingredientPrices,
                List<PricePair> trackingPrices,
                List<List<PricePair>> extractPrices,
                List<PricePair> sellPrices
        ) {
            Label title = new Label("Potion ingredient prices");
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setStyle("-fx-background-color: #f4f5f6; -fx-padding: 15; -fx-background-radius: 8;");
            grid.setVgap(4);
            grid.setHgap(0);

            int ingredientCount = recipe.ingredients().size();
            int trackingCols = recipe.hasTrackingIngredient() ? 2 : 0;
            int totalCols = 1 + (ingredientCount * 2) + trackingCols + (ARCANE_EXTRACTS.size() * 2) + 2;
            double colPct = 100.0 / totalCols;

            for (int i = 0; i < totalCols; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPercentWidth(colPct);
                cc.setHalignment(HPos.CENTER);
                cc.setFillWidth(true);
                grid.getColumnConstraints().add(cc);
            }

            int col = 0;

            grid.add(makePotionHeader("City"), col++, 0);

            for (Object ingObj : recipe.ingredients()) {
                PotionRecipeData.Ingredient ingredient = (PotionRecipeData.Ingredient) ingObj;

                Label header = makePotionHeader(ingredient.name() + " x" + ingredient.quantity());
                GridPane.setColumnSpan(header, 2);
                grid.add(header, col, 0);
                col += 2;

                ingredientPrices.add(new ArrayList<>());
            }

            if (recipe.hasTrackingIngredient()) {
                Label header = makePotionHeader(recipe.resolvedTrackingIngredient() + " x1");
                GridPane.setColumnSpan(header, 2);
                grid.add(header, col, 0);
                col += 2;
            }

            for (String extract : ARCANE_EXTRACTS) {
                Label header = makePotionHeader(extract + " x" + PotionRecipeData.getArcaneExtractQuantityPerBatch(recipe));
                GridPane.setColumnSpan(header, 2);
                grid.add(header, col, 0);
                col += 2;

                extractPrices.add(new ArrayList<>());
            }

            Label sellHeader = makePotionHeader("Sell Price");
            GridPane.setColumnSpan(sellHeader, 2);
            grid.add(sellHeader, col, 0);

            col = 1;

            for (int i = 0; i < ingredientCount; i++) {
                grid.add(makePotionSubHeader("API"), col++, 1);
                grid.add(makePotionSubHeader("Manual"), col++, 1);
            }

            if (recipe.hasTrackingIngredient()) {
                grid.add(makePotionSubHeader("API"), col++, 1);
                grid.add(makePotionSubHeader("Manual"), col++, 1);
            }

            for (int i = 0; i < ARCANE_EXTRACTS.size(); i++) {
                grid.add(makePotionSubHeader("API"), col++, 1);
                grid.add(makePotionSubHeader("Manual"), col++, 1);
            }

            grid.add(makePotionSubHeader("API"), col++, 1);
            grid.add(makePotionSubHeader("Manual"), col, 1);

            int row = 2;

            for (String city : CITIES) {
                col = 0;

                Label cityLabel = makePotionData(city);
                cityLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2b2f35;");
                grid.add(cityLabel, col++, row);

                for (int i = 0; i < ingredientCount; i++) {
                    PricePair pair = createPotionPricePair();
                    ingredientPrices.get(i).add(pair);

                    grid.add(pair.api(), col++, row);
                    grid.add(pair.manual(), col++, row);
                }

                if (recipe.hasTrackingIngredient()) {
                    PricePair pair = createPotionPricePair();
                    trackingPrices.add(pair);

                    grid.add(pair.api(), col++, row);
                    grid.add(pair.manual(), col++, row);
                }

                for (int i = 0; i < ARCANE_EXTRACTS.size(); i++) {
                    PricePair pair = createPotionPricePair();
                    extractPrices.get(i).add(pair);

                    grid.add(pair.api(), col++, row);
                    grid.add(pair.manual(), col++, row);
                }

                PricePair sellPair = createPotionPricePair();
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

        private VBox buildPotionResultTable(List<ResultRow> resultRows) {
            Label title = new Label("Potion result by enchant");
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");

            GridPane grid = new GridPane();
            grid.setMaxWidth(Double.MAX_VALUE);
            grid.setStyle(
                    "-fx-background-color: #1b1e22;" +
                            "-fx-border-color: #50565f;" +
                            "-fx-border-width: 1;" +
                            "-fx-padding: 0;"
            );
            grid.setVgap(0);
            grid.setHgap(0);

            String[] headers = {"Enchant", "Demand", "Profit", "SPF", "Gain", "Cost", "Focus Cost"};

            for (int i = 0; i < headers.length; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setMinWidth(i == 0 ? 76.0 : 112.0);
                cc.setPrefWidth(i == 0 ? 82.0 : 126.0);
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

        private void updatePotionResults(
                PotionRecipeData.PotionRecipe recipe,
                TextField quantityField,
                TextField stationFeeField,
                SpecsPanel specsPanel,
                String category,
                ComboBox<String> bonusCraft,
                CheckBox craftingFocus,
                CheckBox dailyBonus,
                ComboBox<String> dailyBonusValue,
                ComboBox<String> hoQuality,
                ComboBox<Integer> hoPower,
                List<List<PricePair>> ingredientPrices,
                List<PricePair> trackingPrices,
                List<List<PricePair>> extractPrices,
                List<PricePair> sellPrices,
                List<ResultRow> resultRows
        ) {
            long quantity = parseLong(quantityField.getText(), 1);
            int spec = specsPanel != null ? CraftPanel.lookupPotionSpec(specsPanel, category) : 0;
            int batchSize = Math.max(1, recipe.batchSize());

            double lpb = computePotionDailyLpb(
                    bonusCraft,
                    craftingFocus,
                    dailyBonus,
                    dailyBonusValue,
                    hoQuality,
                    hoPower
            );

            double rrr = lpb / (100.0 + lpb);
            double netCostMultiplier = 1.0 - rrr;

            double baseCostPerItem = 0.0;

            for (int i = 0; i < recipe.ingredients().size(); i++) {
                PotionRecipeData.Ingredient ingredient = (PotionRecipeData.Ingredient) recipe.ingredients().get(i);
                double bestPrice = bestBuyPrice(ingredientPrices.get(i));

                baseCostPerItem += bestPrice * (ingredient.quantity() / (double) batchSize);
            }

            if (recipe.hasTrackingIngredient()) {
                baseCostPerItem += bestBuyPrice(trackingPrices) / batchSize;
            }

            double bestSell = bestSellPrice(sellPrices);

            double feePer100 =
                    com.albionservant.data.CraftingNutritionData
                            .parseNonNegative(stationFeeField.getText());
            for (ResultRow row : resultRows) {
                double costPerItem = baseCostPerItem;

                if (row.enchant() > 0) {
                    int extractIndex = row.enchant() - 1;

                    if (extractIndex >= 0 && extractIndex < extractPrices.size()) {
                        costPerItem += bestBuyPrice(extractPrices.get(extractIndex)) * PotionRecipeData.getArcaneExtractQuantityPerItem(recipe);
                    }
                }

                double stationCost =
                        com.albionservant.data.CraftingNutritionData
                                .stationFeeForDisplayName(
                                        recipe.name(),
                                        row.enchant(),
                                        feePer100,
                                        quantity
                                );

                if (!Double.isFinite(stationCost)) {
                    row.cost().setText("-");
                    row.cost().setTooltip(new Tooltip(
                            "Missing nutritionCost for "
                                    + recipe.name()
                                    + " enchant "
                                    + row.enchant()
                    ));
                    row.profit().setText("-");
                    row.spf().setText("-");
                    continue;
                }

                double materialCostAfterReturns =
                        costPerItem * netCostMultiplier * quantity;
                double totalCost = materialCostAfterReturns + stationCost;
                row.cost().setTooltip(new Tooltip(
                        "Materials after RRR: "
                                + formatSilver(materialCostAfterReturns)
                                + "\nRRR: "
                                + String.format(
                                java.util.Locale.US,
                                "%.2f%%",
                                rrr * 100.0
                        )
                                + "\nStation fee: "
                                + formatSilver(stationCost)
                ));
                double totalGain = bestSell * quantity;
                double totalProfit = totalGain - totalCost;

                long focusPerItem = com.albionservant.data.FocusCostCalculator.forPotion(recipe, row.enchant(), spec);
                long totalFocus = focusPerItem * quantity;

                row.cost().setText(formatSilver(totalCost));
                row.gain().setText(formatSilver(totalGain));
                row.profit().setText(formatSilver(totalProfit));
                row.focus().setText(String.format("%,d", totalFocus));

                if (craftingFocus != null
                        && craftingFocus.isSelected()
                        && totalFocus > 0) {
                    double noFocusLpb = Math.max(0.0, lpb - 59.0);
                    double noFocusRrr = noFocusLpb / (100.0 + noFocusLpb);
                    double focusSavings = costPerItem
                            * Math.max(0.0, rrr - noFocusRrr)
                            * quantity;

                    row.spf().setText(String.format(
                            java.util.Locale.US,
                            "%.2f",
                            focusSavings / totalFocus
                    ));
                    row.spf().setTooltip(new Tooltip(
                            "Silver per Focus uses only the extra profit "
                                    + "created by Focus returns."
                                    + "\nFocus savings: "
                                    + formatSilver(focusSavings)
                                    + "\nFocus spent: "
                                    + String.format(
                                    java.util.Locale.US,
                                    "%,d",
                                    totalFocus
                            )
                    ));
                } else {
                    row.spf().setText("-");
                    row.spf().setTooltip(new Tooltip(
                            "Enable Crafting Focus to calculate Silver per Focus."
                    ));
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


        private double computePotionDailyLpb(
                ComboBox<String> bonusCraft,
                CheckBox craftingFocus,
                CheckBox dailyBonus,
                ComboBox<String> dailyBonusValue,
                ComboBox<String> hoQuality,
                ComboBox<Integer> hoPower
        ) {
            double lpb;

            String loc = bonusCraft != null && bonusCraft.getValue() != null
                    ? bonusCraft.getValue()
                    : "Royal City";

            switch (loc) {
                case "Royal Island" -> lpb = 0.0;
                case "Royal City" -> lpb = 18.0;
                case "Royal City + Bonus" -> lpb = 33.0;
                case "HO" -> {
                    int zq = parsePotionHoQuality(hoQuality);
                    int pl = parsePotionHoPower(hoPower);

                    lpb = 18.0
                            + (pl - 1)
                            + (2.0 + zq) * 5.0
                            + (pl - 1) * 2.0;
                }
                default -> lpb = 18.0;
            }

            if (craftingFocus != null && craftingFocus.isSelected()) {
                lpb += 59.0;
            }

            if (dailyBonus != null && dailyBonus.isSelected()) {
                lpb += parsePotionDailyBonus(dailyBonusValue);
            }

            return lpb;
        }

        private double parsePotionDailyBonus(ComboBox<String> dailyBonusValue) {
            if (dailyBonusValue == null || dailyBonusValue.getValue() == null) {
                return 10.0;
            }

            String digits = dailyBonusValue.getValue().replaceAll("[^0-9]", "");

            if (digits.isEmpty()) {
                return 10.0;
            }

            try {
                return Double.parseDouble(digits);
            } catch (NumberFormatException ignored) {
                return 10.0;
            }
        }

        private int parsePotionHoQuality(ComboBox<String> hoQuality) {
            if (hoQuality == null || hoQuality.getValue() == null) {
                return 1;
            }

            try {
                return Integer.parseInt(hoQuality.getValue().replace("Q", "").trim());
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }

        private int parsePotionHoPower(ComboBox<Integer> hoPower) {
            if (hoPower == null || hoPower.getValue() == null) {
                return 1;
            }

            return hoPower.getValue();
        }

        private StackPane buildPotionIcon(String itemName, Runnable onBack) {
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

            String potionIconUrl = com.albionservant.data.ItemRenderData.getPotionImageUrl(itemName);

            if (potionIconUrl == null) {
                iconPane.getChildren().clear();

                Label fallback = new Label(itemName.substring(0, Math.min(2, itemName.length())).toUpperCase());
                fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");

                iconPane.getChildren().add(fallback);
                return iconPane;
            }

            Image img = new Image(potionIconUrl, iconSize, iconSize, true, true, true);

            img.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    javafx.application.Platform.runLater(() -> {
                        iconPane.getChildren().clear();

                        if (img.isError()) {
                            Label fallback = new Label(itemName.substring(0, Math.min(2, itemName.length())).toUpperCase());
                            fallback.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4b5058;");
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

        private static PricePair createPotionPricePair() {
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

        private static ComboBox<String> createPotionCityCombo() {
            ComboBox<String> cb = new ComboBox<>();
            cb.getItems().addAll(CITIES);
            cb.setValue("Martlock");
            return cb;
        }

        private static void lockPotionCombo(ComboBox<?> comboBox) {
            comboBox.setMinWidth(POTION_CONTROL_WIDTH);
            comboBox.setPrefWidth(POTION_CONTROL_WIDTH);
            comboBox.setMaxWidth(POTION_CONTROL_WIDTH);
        }

        private static Label makePotionHeader(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5058; -fx-font-size: 12px;");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            return label;
        }

        private static Label makePotionSubHeader(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #707782; -fx-font-size: 11px;");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            return label;
        }

        private static Label makePotionData(String text) {
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
                            "-fx-border-color: #50565f;" +
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
            label.setMinWidth(Region.USE_PREF_SIZE);
            label.setAlignment(Pos.CENTER);
            return label;
        }

        private static TextField makeDarkInput() {
            TextField field = new TextField("...");
            field.setEditable(false);
            field.setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-prompt-text-fill: #9ca2ab;" +
                            "-fx-background-color: #1b1e22;" +
                            "-fx-alignment: center;" +
                            "-fx-border-color: #50565f;" +
                            "-fx-border-width: 0 1 1 0;"
            );
            field.setMaxWidth(Double.MAX_VALUE);
            field.setMinWidth(92.0);
            return field;
        }

        private static String darkCellStyle() {
            return "-fx-font-size: 12px;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-alignment: center;" +
                    "-fx-padding: 4 6 4 6;" +
                    "-fx-border-color: #50565f;" +
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

}