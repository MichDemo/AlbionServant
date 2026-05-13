package com.albionservant.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Material requirements calculator with precise RRR computation.
 *
 * RRR formula (confirmed Albion wiki + forum):
 *   RRR = LPB / (100 + LPB)
 *
 * LPB components:
 *   Royal City:  18% base + 15% if specialized + focus (59%) + daily bonus
 *   Hideout:     base = 18 + (PL-1)*1%
 *                special = (2+zoneQuality)*5% + (PL-1)*2%
 *                LPB = base + (specialized ? special : 0) + focus + daily
 *   No Bonus:    0% + focus + daily
 *   Custom:      user-entered LPB%
 *
 * Batch sizes: 1 (gear), 10 (food), 5 (potions)
 */
public class RequirementsCalculatorPanel extends VBox {

    public record MaterialLine(String name, double quantityPerBatch) {}

    private enum LocationMode { NO_BONUS, ROYAL_CITY, HIDEOUT, CUSTOM }

    private final int batchSize;
    private final List<MaterialLine> materials;

    // ── Controls ──────────────────────────────────────────────────────────────
    private final TextField      quantityField    = new TextField("100");
    private final ToggleGroup    modeGroup        = new ToggleGroup();
    private final CheckBox       specializedCheck = new CheckBox("Specialized item for this location  (+15% Royal / zone biome bonus)");
    private final CheckBox       focusCheck       = new CheckBox("Using Focus  (+59%)");
    private final ComboBox<String> dailyBonusCombo = new ComboBox<>();

    // Hideout-specific
    private final Spinner<Integer> zoneQualitySpinner = new Spinner<>(1, 6, 1);
    private final Spinner<Integer> powerLevelSpinner  = new Spinner<>(1, 9, 1);
    private final VBox             hideoutControls    = new VBox(8);

    // Custom
    private final TextField customLPBField   = new TextField("0");
    private final VBox      customControls   = new VBox(8);

    // Output
    private final Label rrrLabel   = new Label();
    private final Label lpbLabel   = new Label();
    private final VBox  resultsBox = new VBox(4);

    public RequirementsCalculatorPanel(List<MaterialLine> materials, int batchSize) {
        this.materials = materials;
        this.batchSize = batchSize;

        setStyle("-fx-background-color: #1e293b; -fx-background-radius: 10;");
        setPadding(new Insets(22, 28, 26, 28));
        setSpacing(18);
        setFillWidth(true);

        getChildren().addAll(
                buildHeader(),
                buildQuantityRow(),
                buildModeSelector(),
                buildSharedControls(),
                buildRRRDisplay(),
                buildResultsSection()
        );

        // Wire all inputs to recalculate
        quantityField.textProperty().addListener((o, ov, nv) -> recalculate());
        modeGroup.selectedToggleProperty().addListener((o, ov, nv) -> { updateModeVisibility(); recalculate(); });
        specializedCheck.selectedProperty().addListener((o, ov, nv) -> recalculate());
        focusCheck.selectedProperty().addListener((o, ov, nv) -> recalculate());
        dailyBonusCombo.valueProperty().addListener((o, ov, nv) -> recalculate());
        zoneQualitySpinner.valueProperty().addListener((o, ov, nv) -> recalculate());
        powerLevelSpinner.valueProperty().addListener((o, ov, nv) -> recalculate());
        customLPBField.textProperty().addListener((o, ov, nv) -> recalculate());

        updateModeVisibility();
        recalculate();
    }

    // ── Section builders ─────────────────────────────────────────────────────

    private Label buildHeader() {
        Label t = new Label("📦  Material Requirements Calculator");
        t.setFont(Font.font("System", FontWeight.BOLD, 16));
        t.setTextFill(Color.WHITE);
        return t;
    }

    private HBox buildQuantityRow() {
        Label lbl = new Label("Desired Output Quantity");
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        quantityField.setStyle("-fx-font-size: 14px; -fx-background-color: #334155;"
                + "-fx-text-fill: #e2e8f0; -fx-background-radius: 6;");
        quantityField.setPrefWidth(140);

        Label batchLbl = new Label("(batch size: " + batchSize + " items)");
        batchLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        HBox row = new HBox(12, new VBox(4, lbl, quantityField), batchLbl);
        row.setAlignment(Pos.BOTTOM_LEFT);
        return row;
    }

    private VBox buildModeSelector() {
        Label title = new Label("Crafting Location");
        title.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        RadioButton noBonusBtn  = styledRadio("No Bonus  (Island / Guild Island)",       LocationMode.NO_BONUS);
        RadioButton royalBtn    = styledRadio("Royal City",                               LocationMode.ROYAL_CITY);
        RadioButton hideoutBtn  = styledRadio("Black Zone / Roads of Avalon Hideout",     LocationMode.HIDEOUT);
        RadioButton customBtn   = styledRadio("Custom  (enter LPB% directly)",            LocationMode.CUSTOM);

        royalBtn.setSelected(true);

        // Hideout sub-controls
        Label zqLbl = new Label("Zone Quality  (1 = Roads / outer BZ, 6 = inner BZ)");
        zqLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        zoneQualitySpinner.setPrefWidth(100);
        zoneQualitySpinner.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0;");

        Label plLbl = new Label("Power Level  (1 = no cores installed, 9 = max cores)");
        plLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        powerLevelSpinner.setPrefWidth(100);
        powerLevelSpinner.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0;");

        hideoutControls.getChildren().addAll(
                new HBox(12, new VBox(3, zqLbl, zoneQualitySpinner)),
                new HBox(12, new VBox(3, plLbl, powerLevelSpinner))
        );
        hideoutControls.setPadding(new Insets(0, 0, 0, 22));

        // Custom sub-controls
        Label customLbl = new Label("Local Production Bonus % (LPB) — enter value shown in-game");
        customLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        customLPBField.setStyle("-fx-font-size: 13px; -fx-background-color: #334155;"
                + "-fx-text-fill: #e2e8f0; -fx-background-radius: 6;");
        customLPBField.setPrefWidth(120);
        customControls.getChildren().addAll(new VBox(3, customLbl, customLPBField));
        customControls.setPadding(new Insets(0, 0, 0, 22));

        VBox box = new VBox(8, title, noBonusBtn, royalBtn,
                hideoutBtn, hideoutControls,
                customBtn, customControls);
        return box;
    }

    private VBox buildSharedControls() {
        specializedCheck.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        focusCheck.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        Label dailyLbl = new Label("Daily Activity Bonus");
        dailyLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        dailyBonusCombo.getItems().addAll("None (0%)", "Small (+10%)", "Large (+20%)");
        dailyBonusCombo.setValue("None (0%)");
        dailyBonusCombo.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0;");
        dailyBonusCombo.setPrefWidth(180);

        VBox box = new VBox(10, specializedCheck, focusCheck,
                new VBox(3, dailyLbl, dailyBonusCombo));
        return box;
    }

    private VBox buildRRRDisplay() {
        rrrLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #4ade80; -fx-font-weight: bold;");
        lpbLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        return new VBox(4, rrrLabel, lpbLabel);
    }

    private VBox buildResultsSection() {
        Label title = new Label("Required Materials:");
        title.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        resultsBox.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 8; -fx-padding: 14;");
        resultsBox.setFillWidth(true);
        return new VBox(8, title, resultsBox);
    }

    // ── Mode visibility ──────────────────────────────────────────────────────

    private void updateModeVisibility() {
        LocationMode mode = getMode();
        hideoutControls.setVisible(mode == LocationMode.HIDEOUT);
        hideoutControls.setManaged(mode == LocationMode.HIDEOUT);
        customControls.setVisible(mode == LocationMode.CUSTOM);
        customControls.setManaged(mode == LocationMode.CUSTOM);
        // Specialized only makes sense for Royal City and Hideout
        specializedCheck.setVisible(mode == LocationMode.ROYAL_CITY || mode == LocationMode.HIDEOUT);
        specializedCheck.setManaged(mode == LocationMode.ROYAL_CITY || mode == LocationMode.HIDEOUT);
    }

    // ── Calculation ──────────────────────────────────────────────────────────

    private void recalculate() {
        int quantity;
        try { quantity = Math.max(1, Integer.parseInt(quantityField.getText().trim())); }
        catch (NumberFormatException e) { quantity = 1; }

        double lpb = computeLPB();
        double rrr = lpb / (100.0 + lpb);
        double batches = Math.ceil((double) quantity / batchSize);

        rrrLabel.setText(String.format("RRR: %.2f%%   (saves ~%.2f%% of every material batch)", rrr * 100, rrr * 100));
        lpbLabel.setText(String.format("Local Production Bonus: %.2f%%   |   Batches: %.0f   |   Batch size: %d", lpb, batches, batchSize));

        resultsBox.getChildren().clear();

        // Header
        resultsBox.getChildren().add(resultRow("Material", "Gross needed", "Net needed  (after RRR)", "Saved", true));
        resultsBox.getChildren().add(new Separator());

        for (MaterialLine mat : materials) {
            double gross = batches * mat.quantityPerBatch();
            long   net   = (long) Math.ceil(gross * (1.0 - rrr));
            long   saved = (long) Math.floor(gross - net);
            resultsBox.getChildren().add(resultRow(
                    mat.name(),
                    fmt((long) Math.ceil(gross)),
                    fmt(net),
                    fmt(saved),
                    false
            ));
        }

        resultsBox.getChildren().add(new Separator());
        Label summary = new Label(String.format(
                "Output: %,d items in %.0f batch(es)  ·  RRR reduces material cost by %.1f%%  ·  "
                        + "Net savings: ~%.0f materials per 1000 used",
                quantity, batches, rrr * 100, rrr * 1000
        ));
        summary.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        resultsBox.getChildren().add(summary);
    }

    private double computeLPB() {
        double daily = switch (dailyBonusCombo.getValue()) {
            case "Small (+10%)" -> 10.0;
            case "Large (+20%)" -> 20.0;
            default             -> 0.0;
        };
        double focus = focusCheck.isSelected() ? 59.0 : 0.0;
        boolean specialized = specializedCheck.isSelected();

        return switch (getMode()) {
            case NO_BONUS  -> focus + daily;
            case ROYAL_CITY -> 18.0 + (specialized ? 15.0 : 0.0) + focus + daily;
            case HIDEOUT -> {
                int zq = zoneQualitySpinner.getValue();
                int pl = powerLevelSpinner.getValue();
                double base    = 18.0 + (pl - 1) * 1.0;
                double special = (2.0 + zq) * 5.0 + (pl - 1) * 2.0;
                yield base + (specialized ? special : 0.0) + focus + daily;
            }
            case CUSTOM -> {
                try { yield Math.max(0, Double.parseDouble(customLPBField.getText().trim())); }
                catch (NumberFormatException e) { yield 0.0; }
            }
        };
    }

    private LocationMode getMode() {
        if (modeGroup.getSelectedToggle() == null) return LocationMode.ROYAL_CITY;
        return (LocationMode) modeGroup.getSelectedToggle().getUserData();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private RadioButton styledRadio(String text, LocationMode mode) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(modeGroup);
        rb.setUserData(mode);
        rb.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        return rb;
    }

    private HBox resultRow(String name, String gross, String net, String saved, boolean header) {
        String baseStyle = header
                ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;"
                : "-fx-font-size: 13px; -fx-text-fill: #e2e8f0;";

        Label n = cell(name,  header ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;" : "-fx-font-size: 13px; -fx-text-fill: #e2e8f0;",           0.38);
        Label g = cell(gross, header ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;" : "-fx-font-size: 13px; -fx-text-fill: #94a3b8;",           0.22);
        Label ne= cell(net,   header ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4ade80;" : "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4ade80;", 0.22);
        Label sv= cell(saved, header ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #fb923c;" : "-fx-font-size: 13px; -fx-text-fill: #fb923c;",           0.18);

        HBox row = new HBox(n, g, ne, sv);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private Label cell(String text, String style, double pct) {
        Label l = new Label(text);
        l.setStyle(style);
        l.setMaxWidth(Double.MAX_VALUE);
        l.prefWidthProperty().bind(resultsBox.widthProperty().multiply(pct));
        return l;
    }

    private String fmt(long n) { return String.format("%,d", n); }

    // ── Static factories ─────────────────────────────────────────────────────

    public static RequirementsCalculatorPanel forGear(String itemName) {
        com.albionservant.data.CraftMaterialData.Materials mats =
                com.albionservant.data.CraftMaterialData.getMaterials(itemName);
        List<MaterialLine> lines = new ArrayList<>();
        lines.add(new MaterialLine(mats.material1(), 1.0));
        if (!com.albionservant.data.CraftMaterialData.NA.equals(mats.material2()))
            lines.add(new MaterialLine(mats.material2(), 1.0));
        com.albionservant.data.ArtifactData.ArtifactType art =
                com.albionservant.data.ArtifactData.getArtifactType(itemName);

        return new RequirementsCalculatorPanel(lines, 1);
    }

    public static RequirementsCalculatorPanel forFood(
            com.albionservant.data.FoodRecipeData.Recipe recipe) {
        List<MaterialLine> lines = new ArrayList<>();
        for (var ing : recipe.ingredients())
            lines.add(new MaterialLine(ing.name(), ing.quantity()));
        return new RequirementsCalculatorPanel(lines, 10);
    }

    public static RequirementsCalculatorPanel forPotion(
            com.albionservant.data.PotionRecipeData.PotionRecipe recipe) {
        List<MaterialLine> lines = new ArrayList<>();
        for (var ing : recipe.ingredients())
            lines.add(new MaterialLine(ing.name(), ing.quantity()));
        if (recipe.hasTrackingIngredient())
            lines.add(new MaterialLine(recipe.resolvedTrackingIngredient(), 1.0));
        return new RequirementsCalculatorPanel(lines, 5);
    }
}