package com.albionservant.gui;

import com.albionservant.AppConfig;
import com.albionservant.data.FoodRecipeData;
import com.albionservant.data.FoodRecipeData.Ingredient;
import com.albionservant.data.FoodRecipeData.Recipe;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FoodPanel extends VBox {

    private final VBox  contentArea = new VBox();
    private final HBox  internalTopBar = new HBox(15);
    private List<String> currentPath = new ArrayList<>();
    private Consumer<Boolean> onDetailModeListener;

    // Cities shown as price rows in the detail grid
    private static final List<String> CITIES = FoodRecipeData.CITIES;

    public FoodPanel() {
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(0));
        setStyle("-fx-background-color: #ef4444;");

        // ── Top bar (breadcrumb + back) ──────────────────────────────────────
        Label breadcrumbLabel = new Label("Food Categories");
        breadcrumbLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        HBox.setHgrow(breadcrumbLabel, Priority.ALWAYS);

        Button backButton = new Button("← Back");
        backButton.setStyle(AppConfig.BTN_PRIMARY);
        backButton.setOnMouseEntered(e -> backButton.setStyle(AppConfig.BTN_PRIMARY_HOVER));
        backButton.setOnMouseExited(e  -> backButton.setStyle(AppConfig.BTN_PRIMARY));
        backButton.setOnAction(e -> goBack());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        internalTopBar.getChildren().addAll(breadcrumbLabel, spacer, backButton);
        internalTopBar.setPadding(new Insets(14, 28, 14, 28));
        internalTopBar.setAlignment(Pos.CENTER_LEFT);
        internalTopBar.setStyle("-fx-background-color: #ef4444;");

        contentArea.setFillWidth(true);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(internalTopBar, scroll);
        refreshUI();
    }

    public void setOnDetailModeListener(Consumer<Boolean> listener) {
        this.onDetailModeListener = listener;
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    private void goBack() {
        if (!currentPath.isEmpty()) {
            currentPath.remove(currentPath.size() - 1);
            refreshUI();
        }
    }

    private void navigate(String item) {
        currentPath.add(item);
        refreshUI();
    }

    // ── UI refresh ──────────────────────────────────────────────────────────

    private void refreshUI() {
        contentArea.getChildren().clear();

        // Update breadcrumb
        String crumb = currentPath.isEmpty() ? "Food Categories"
                : String.join("  ►  ", currentPath);
        ((Label) internalTopBar.getChildren().get(0)).setText(crumb);

        internalTopBar.setVisible(true);
        internalTopBar.setManaged(true);

        if (currentPath.isEmpty()) {
            // ROOT: show 8 big category buttons centred
            showRootCategories();
        } else if (currentPath.size() == 1) {
            // CATEGORY: show item buttons
            showItemList(currentPath.get(0));
        } else {
            // ITEM: show detail panel
            String itemName = currentPath.get(currentPath.size() - 1);
            if (FoodRecipeData.hasRecipe(itemName)) {
                showDetail(itemName);
            } else {
                showItemList(currentPath.get(0));
            }
        }
    }

    // ── ROOT: centred category grid ─────────────────────────────────────────

    private void showRootCategories() {
        VBox center = new VBox(24);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(50, 40, 50, 40));
        center.setStyle("-fx-background-color: #f1f5f9;");
        VBox.setVgrow(center, Priority.ALWAYS);

        Label prompt = new Label("Choose a food category");
        prompt.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555;");

        // Two rows of 4
        List<String> cats = FoodRecipeData.getTopCategories();
        HBox row1 = buildCategoryRow(cats.subList(0, Math.min(4, cats.size())));
        HBox row2 = cats.size() > 4
                ? buildCategoryRow(cats.subList(4, cats.size()))
                : new HBox();
        row2.setAlignment(Pos.CENTER);

        center.getChildren().addAll(prompt, row1, row2);
        contentArea.getChildren().add(center);
    }

    private HBox buildCategoryRow(List<String> items) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        for (String cat : items) {
            Button btn = new Button(cat);
            btn.setPrefWidth(195);
            btn.setPrefHeight(65);
            btn.setStyle(AppConfig.BTN_ROOT);
            btn.setOnMouseEntered(e -> btn.setStyle(AppConfig.BTN_ROOT_HOVER));
            btn.setOnMouseExited(e  -> btn.setStyle(AppConfig.BTN_ROOT));
            btn.setOnAction(e -> navigate(cat));
            row.getChildren().add(btn);
        }
        return row;
    }

    // ── CATEGORY: item list ─────────────────────────────────────────────────

    private void showItemList(String category) {
        VBox page = new VBox(10);
        page.setPadding(new Insets(24, 28, 24, 28));
        page.setStyle("-fx-background-color: #f1f5f9;");
        VBox.setVgrow(page, Priority.ALWAYS);

        Label header = new Label(category);
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        page.getChildren().add(header);

        List<String> items = FoodRecipeData.getCategoryChildren(category);
        for (String item : items) {
            Button btn = new Button(item);
            btn.setPrefWidth(230);
            btn.setPrefHeight(54);
            btn.setStyle(AppConfig.BTN_ACTIVE);
            btn.setOnMouseEntered(e -> btn.setStyle(AppConfig.BTN_ACTIVE_HOVER));
            btn.setOnMouseExited(e  -> btn.setStyle(AppConfig.BTN_ACTIVE));
            btn.setOnAction(e -> navigate(item));
        }

        // Wrap into rows of 4
        FlowPane flow = new FlowPane(14, 14);
        flow.setPrefWrapLength(Double.MAX_VALUE);
        for (String item : items) {
            Button btn = new Button(item);
            btn.setPrefWidth(230);
            btn.setPrefHeight(54);
            btn.setStyle(AppConfig.BTN_ACTIVE);
            btn.setOnMouseEntered(e -> btn.setStyle(AppConfig.BTN_ACTIVE_HOVER));
            btn.setOnMouseExited(e  -> btn.setStyle(AppConfig.BTN_ACTIVE));
            btn.setOnAction(e -> navigate(item));
            flow.getChildren().add(btn);
        }

        page.getChildren().add(flow);
        contentArea.getChildren().add(page);
    }

    // ── DETAIL: recipe + city prices ────────────────────────────────────────

    private void showDetail(String itemName) {
        Recipe recipe = FoodRecipeData.getRecipe(itemName);
        if (recipe == null) return;

        if (onDetailModeListener != null) onDetailModeListener.accept(true);

        VBox page = new VBox(0);
        page.setFillWidth(true);
        page.setStyle("-fx-background-color: #ffffff;");
        VBox.setVgrow(page, Priority.ALWAYS);

        // ── Section A: top config bar ────────────────────────────────────────
        HBox configBar = buildConfigBar(recipe);
        page.getChildren().add(configBar);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #e2e8f0;");
        page.getChildren().add(sep1);

        // ── Section B: main price grid ───────────────────────────────────────
        VBox gridSection = buildPriceGrid(recipe);
        page.getChildren().add(gridSection);

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: #e2e8f0;");
        page.getChildren().add(sep2);

        // ── Section C: fish sauce enchant grid ───────────────────────────────
        VBox fishSection = buildFishSauceSection();
        page.getChildren().add(fishSection);

        // ── Section D: demand + summary ──────────────────────────────────────
        VBox demandSection = buildDemandSection();
        page.getChildren().add(demandSection);

        contentArea.getChildren().add(page);
    }

    // ── Config bar (quantity, station fee, bonus, sell location) ────────────

    private HBox buildConfigBar(Recipe recipe) {
        HBox bar = new HBox(30);
        bar.setPadding(new Insets(18, 28, 18, 28));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #f8fafc;");

        // Item info
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(recipe.name());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label tierLabel = new Label("Tier " + recipe.tier()
                + "   •   Batch: 10 items");
        tierLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        nameBox.getChildren().addAll(nameLabel, tierLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // Quantity
        VBox qtyBox  = labeledField("Quantity", "10");
        // Station fee
        VBox feeBox  = labeledField("Station Fee", "0");
        // Sell location
        VBox sellBox = labeledCombo("Sell At", CITIES);
        // Bonus craft
        VBox bonusBox = labeledCombo("Bonus Craft",
                List.of("Royal City", "Royal Island", "Royal City + Bonus", "HO"));

        bar.getChildren().addAll(nameBox, qtyBox, feeBox, sellBox, bonusBox);
        return bar;
    }

    // ── Main price grid ──────────────────────────────────────────────────────
    //
    // Columns:
    //   col 0      : City name
    //   col 1..N   : one col per ingredient  (API row on top, Manual input below)
    //   col N+1..  : one col per fish sauce  (Basic / Fancy / Special)
    //   last cols  : Demand | Profit | SPF | Gain | Cost | Focus Cost
    //
    // Rows:
    //   Row 0      : Header — ingredient names (API / Manual sub-header in row 1)
    //   Row 2      : "Price" label row (API prices from market, one per ingredient)
    //   Row 3..9   : City rows (7 cities) — buy price input per ingredient per city

    private VBox buildPriceGrid(Recipe recipe) {
        List<Ingredient> ingredients = recipe.ingredients();
        List<String> sauces = FoodRecipeData.FISH_SAUCES;

        int ingCols    = ingredients.size();
        int sauceCols  = sauces.size();
        int resultCols = 6; // Demand, Profit, SPF, Gain, Cost, Focus Cost
        int totalCols  = 1 + ingCols + sauceCols + resultCols;
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

        String[] resultHeaders = {"Demand", "Profit", "SPF", "Gain", "Cost", "Focus Cost"};

        // ── Row 0: ingredient group headers ──────────────────────────────────
        grid.add(headerCell(""), 0, 0);
        int col = 1;
        for (Ingredient ing : ingredients) {
            Label lbl = new Label(ing.name() + " ×" + ing.quantity());
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 12px;");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            lbl.setPadding(new Insets(6, 4, 6, 4));
            lbl.setWrapText(true);
            grid.add(lbl, col++, 0);
        }
        for (String sauce : sauces) {
            Label lbl = new Label(sauce);
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0ea5e9; -fx-font-size: 11px;");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            lbl.setPadding(new Insets(6, 4, 6, 4));
            lbl.setWrapText(true);
            grid.add(lbl, col++, 0);
        }
        for (String h : resultHeaders) {
            grid.add(headerCell(h), col++, 0);
        }

        // ── Row 1: sub-headers (API / Manual) for ingredient cols ─────────────
        grid.add(subHeaderCell(""), 0, 1);
        col = 1;
        for (int i = 0; i < ingCols; i++) {
            grid.add(subHeaderCell("API"), col++, 1);
        }
        for (int i = 0; i < sauceCols; i++) {
            grid.add(subHeaderCell("API"), col++, 1);
        }
        for (int i = 0; i < resultCols; i++) {
            grid.add(subHeaderCell(""), col++, 1);
        }

        // ── Row 2: "API Price" row (global best price from API) ───────────────
        grid.add(boldCell("API Price", "#334155"), 0, 2);
        col = 1;
        for (int i = 0; i < ingCols + sauceCols; i++) {
            grid.add(apiField(), col++, 2);
        }
        for (int i = 0; i < resultCols; i++) {
            grid.add(resultCell("—"), col++, 2);
        }

        // ── Rows 3–9: one row per city ────────────────────────────────────────
        int gridRow = 3;
        for (int c = 0; c < CITIES.size(); c++) {
            String city = CITIES.get(c);
            String rowBg = (c % 2 == 0) ? "#f8fafc" : "#ffffff";

            Label cityLbl = new Label(city);
            cityLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-text-fill: #1e293b; -fx-padding: 6 8 6 8;"
                    + "-fx-background-color: " + rowBg + ";");
            cityLbl.setMaxWidth(Double.MAX_VALUE);
            grid.add(cityLbl, 0, gridRow);

            col = 1;
            for (int i = 0; i < ingCols + sauceCols; i++) {
                grid.add(manualField(rowBg), col++, gridRow);
            }
            // Demand column — editable per city
            grid.add(manualField(rowBg), col++, gridRow); // Demand
            // Profit / SPF / Gain / Cost / Focus Cost — calculated, read-only
            for (int i = 1; i < resultCols; i++) {
                grid.add(calcCell("—", rowBg), col++, gridRow);
            }
            gridRow++;
        }

        VBox wrapper = new VBox(grid);
        wrapper.setFillWidth(true);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setPadding(new Insets(20, 28, 0, 28));
        return wrapper;
    }

    // ── Fish sauce section ───────────────────────────────────────────────────
    // Shows Basic / Fancy / Special fish sauce — same city-row structure

    private VBox buildFishSauceSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16, 28, 0, 28));
        box.setStyle("-fx-background-color: #ffffff;");

        Label title = new Label("🐟 Fish Sauce Enchanting  —  optional, shown per city");
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0ea5e9;");
        box.getChildren().add(title);

        // Fish sauce is already inline in the main grid columns — this section
        // just shows the output food sell prices per city per enchant level
        int enchantLevels = 3; // .1 / .2 / .3
        String[] levels = {".1 (Basic)", ".2 (Fancy)", ".3 (Special)"};

        GridPane grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setVgap(0);
        grid.setHgap(0);

        int totalCols = 1 + enchantLevels + 4; // city + 3 sell price cols + demand/profit/roi/focus
        double colPct = 100.0 / totalCols;
        for (int i = 0; i < totalCols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(colPct);
            cc.setHalignment(HPos.CENTER);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        // Header row
        grid.add(headerCell("City"), 0, 0);
        for (int i = 0; i < enchantLevels; i++) {
            Label lbl = new Label("Sell Price " + levels[i]);
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0ea5e9; -fx-font-size: 12px;");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            lbl.setPadding(new Insets(6, 4, 6, 4));
            grid.add(lbl, i + 1, 0);
        }
        String[] enchantResultHeaders = {"Demand", "Profit", "ROI", "Focus Cost"};
        for (int i = 0; i < enchantResultHeaders.length; i++) {
            grid.add(headerCell(enchantResultHeaders[i]), 1 + enchantLevels + i, 0);
        }

        // City rows
        for (int c = 0; c < CITIES.size(); c++) {
            String city = CITIES.get(c);
            String rowBg = (c % 2 == 0) ? "#f0f9ff" : "#e0f2fe";
            Label cityLbl = new Label(city);
            cityLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-text-fill: #1e293b; -fx-padding: 6 8 6 8;"
                    + "-fx-background-color: " + rowBg + ";");
            cityLbl.setMaxWidth(Double.MAX_VALUE);
            grid.add(cityLbl, 0, c + 1);
            for (int i = 0; i < enchantLevels; i++) {
                grid.add(manualField(rowBg), i + 1, c + 1);
            }
            for (int i = 0; i < enchantResultHeaders.length; i++) {
                grid.add(calcCell("—", rowBg), 1 + enchantLevels + i, c + 1);
            }
        }

        box.getChildren().add(grid);
        return box;
    }

    // ── Demand + summary section ─────────────────────────────────────────────

    private VBox buildDemandSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20, 28, 30, 28));
        box.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Calculation results / summary will appear here once prices are entered");
        title.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        box.getChildren().add(title);
        return box;
    }

    // ── Cell factory helpers ─────────────────────────────────────────────────

    private Label headerCell(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 12px;"
                + "-fx-padding: 6 4 6 4;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label subHeaderCell(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 3 4 3 4;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label boldCell(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-size: 12px;"
                + "-fx-padding: 6 8 6 8;");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private Label resultCell(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-padding: 4 4 4 4;");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label calcCell(String text, String bgColor) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-padding: 4 4 4 4;"
                + "-fx-background-color: " + bgColor + ";");
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
        tf.setStyle("-fx-font-size: 12px; -fx-alignment: center;"
                + "-fx-background-color: " + bgColor + ";");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private VBox labeledField(String label, String defaultVal) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        TextField tf = new TextField(defaultVal);
        tf.setStyle("-fx-font-size: 13px;");
        tf.setPrefWidth(90);
        VBox box = new VBox(3, lbl, tf);
        return box;
    }

    private VBox labeledCombo(String label, List<String> options) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(options);
        cb.setValue(options.get(0));
        cb.setPrefWidth(150);
        VBox box = new VBox(3, lbl, cb);
        return box;
    }
}