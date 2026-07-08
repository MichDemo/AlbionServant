package com.albionservant.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact inline material list that lives in the icon column.
 * Shows: Material | Net quantity
 * Below: RRR value
 * Reads from existing Quantity TextField, Bonus Craft ComboBox, Focus CheckBox.
 */
public class RequirementsCalculatorPanel extends VBox {

    public record MaterialLine(String name, double quantityPerBatch, boolean noRrr) {
        /** Convenience constructor — RRR applies by default */
        public MaterialLine(String name, double quantityPerBatch) {
            this(name, quantityPerBatch, false);
        }
    }

    private final List<MaterialLine> materials;
    private final int                batchSize;
    private final VBox               rowsBox      = new VBox(3);
    private final Label              rrrLabel     = new Label();

    // External focus total field — set via setFocusTotalField()
    private TextField focusTotalField = null;
    // External focus total label — set via setFocusTotalLabel()
    private Label focusTotalLabel = null;

    // Set by the owning panel after construction
    private TextField         quantityField;
    private ComboBox<String>  bonusCraftCombo;
    private CheckBox          focusCheckBox;
    private ComboBox<String>  hoQualityCombo;
    private ComboBox<Integer> hoPowerCombo;

    // Focus cost context — set via setFocusContext()
    private int    itemTier          = 4;
    private int    totalMaterialsPerCraft = 16;
    private java.util.function.IntSupplier specSupplier = () -> 0;

    public RequirementsCalculatorPanel(List<MaterialLine> materials, int batchSize) {
        this.materials = materials;
        this.batchSize = batchSize;

        setSpacing(6);
        setFillWidth(true);
        setPadding(new Insets(10, 0, 0, 0));

        Label title = new Label("Required materials:");
        title.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: #ef4444;" +
                        "-fx-background-radius: 4;" +
                        "-fx-padding: 3 8 3 8;"
        );

        rowsBox.setFillWidth(true);
        rowsBox.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 10 8 10;"
        );

        rrrLabel.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-padding: 2 0 0 2;"
        );

        getChildren().addAll(title, rowsBox);
    }

    /** RRR label — place below Bonus Craft in the config column */
    public Label getRrrLabel() { return rrrLabel; }

    /**
     * Wires an external read-only TextField to display the total focus cost.
     * Place this field in the config center column labeled "Focus Cost:".
     */
    public void setFocusTotalField(TextField field) {
        this.focusTotalField = field;
        render();
    }

    /** Wires an external Label to display the total focus cost as a column header subtitle. */
    public void setFocusTotalLabel(Label label) {
        this.focusTotalLabel = label;
        // Don't render here — quantityField may not be wired yet
        // render() will be triggered by bindControls()
    }

    /**
     * Sets the context needed to compute focus costs.
     *
     * @param tier                 Item tier (2–8)
     * @param totalMaterialsPerCraft Sum of all materials per single craft (or per-item for food/potion)
     * @param specSupplier         Returns the current spec level (0–100) for this item
     */
    public void setFocusContext(int tier, int totalMaterialsPerCraft,
                                java.util.function.IntSupplier specSupplier) {
        this.itemTier              = tier;
        this.totalMaterialsPerCraft = totalMaterialsPerCraft;
        this.specSupplier          = specSupplier;
        render(); // re-render with focus context
    }

    public void bindControls(TextField quantityField,
                             ComboBox<String> bonusCraftCombo,
                             CheckBox focusCheckBox,
                             ComboBox<String> hoQualityCombo,
                             ComboBox<Integer> hoPowerCombo) {
        this.quantityField   = quantityField;
        this.bonusCraftCombo = bonusCraftCombo;
        this.focusCheckBox   = focusCheckBox;
        this.hoQualityCombo  = hoQualityCombo;
        this.hoPowerCombo    = hoPowerCombo;

        quantityField.textProperty().addListener((o, ov, nv)  -> render());
        bonusCraftCombo.valueProperty().addListener((o, ov, nv) -> render());
        focusCheckBox.selectedProperty().addListener((o, ov, nv) -> render());
        if (hoQualityCombo  != null) hoQualityCombo.valueProperty().addListener((o, ov, nv)  -> render());
        if (hoPowerCombo    != null) hoPowerCombo.valueProperty().addListener((o, ov, nv)    -> render());

        render();
    }

    public void bindControls(TextField quantityField,
                             ComboBox<String> bonusCraftCombo,
                             CheckBox focusCheckBox) {
        bindControls(quantityField, bonusCraftCombo, focusCheckBox, null, null);
    }

    // ── Render ───────────────────────────────────────────────────────────────

    private void render() {
        if (quantityField == null) return;

        int quantity;
        try { quantity = Math.max(1, Integer.parseInt(quantityField.getText().trim())); }
        catch (NumberFormatException e) { quantity = 1; }

        double lpb     = computeLPB();
        double rrr     = lpb / (100.0 + lpb);
        double batches = Math.ceil((double) quantity / batchSize);

        rowsBox.getChildren().clear();

        // Header row
        rowsBox.getChildren().add(row(
                lbl("Material",  0.62, "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;"),
                lbl("Net",       0.38, "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4ade80;")
        ));

        for (MaterialLine mat : materials) {
            double gross = batches * mat.quantityPerBatch();
            long   net   = mat.noRrr()
                    ? (long) Math.ceil(gross)                       // no RRR — e.g. Avalonian Energy
                    : (long) Math.ceil(gross * (1.0 - rrr));
            rowsBox.getChildren().add(row(
                    lbl(mat.name(),  0.62, "-fx-font-size: 14px; -fx-text-fill: #e2e8f0;"),
                    lbl(fmt(net),    0.38, "-fx-font-size: 14px; -fx-font-weight: bold;"
                            + (mat.noRrr() ? " -fx-text-fill: #0ea5e9;" : " -fx-text-fill: #4ade80;"))
            ));
        }

        rrrLabel.setText(String.format("RRR: %.2f%%  (LPB: %.1f%%)", rrr * 100, lpb));

        // Update external focus total field if wired
        int  spec      = specSupplier.getAsInt();
        long perItem   = com.albionservant.data.FocusCostCalculator.withSpec(
                com.albionservant.data.FocusCostCalculator.baseFocusCost(
                        itemTier, totalMaterialsPerCraft), spec);
        long total     = perItem * (long) batches;
        if (focusTotalField != null) {
            focusTotalField.setText(String.format("%,d", total));
        }
        if (focusTotalLabel != null) {
            focusTotalLabel.setText(String.format("Total: %,d", total));
        }
    }

    // ── LPB / RRR ────────────────────────────────────────────────────────────

    private double computeLPB() {
        boolean focus = focusCheckBox != null && focusCheckBox.isSelected();
        double  fb    = focus ? 59.0 : 0.0;
        String  loc   = bonusCraftCombo != null && bonusCraftCombo.getValue() != null
                ? bonusCraftCombo.getValue() : "Royal City";
        return switch (loc) {
            case "Royal Island"       -> fb;
            case "Royal City"         -> 18.0 + fb;
            case "Royal City + Bonus" -> 33.0 + fb;
            case "HO" -> {
                int zq = parseHoQuality();
                int pl = parseHoPower();
                yield 18.0 + (pl - 1) + (2.0 + zq) * 5.0 + (pl - 1) * 2.0 + fb;
            }
            default -> 18.0 + fb;
        };
    }

    private int parseHoQuality() {
        if (hoQualityCombo == null || hoQualityCombo.getValue() == null) return 1;
        try { return Integer.parseInt(hoQualityCombo.getValue().replace("Q", "").trim()); }
        catch (NumberFormatException e) { return 1; }
    }

    private int parseHoPower() {
        if (hoPowerCombo == null || hoPowerCombo.getValue() == null) return 1;
        return hoPowerCombo.getValue();
    }

    // ── Cell helpers ─────────────────────────────────────────────────────────

    private HBox row(Label... cells) {
        HBox r = new HBox();
        r.setMaxWidth(Double.MAX_VALUE);
        r.setAlignment(Pos.CENTER_LEFT);
        for (Label l : cells) r.getChildren().add(l);
        return r;
    }

    private Label lbl(String text, double pct, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        l.setMaxWidth(Double.MAX_VALUE);
        // Use percentage-based preferred width relative to parent
        // Avoid binding to rowsBox.widthProperty() which may be 0 at construction time
        if (pct >= 0.5) {
            HBox.setHgrow(l, Priority.ALWAYS);
        } else {
            HBox.setHgrow(l, Priority.SOMETIMES);
        }
        return l;
    }

    private String fmt(long n) { return String.format("%,d", n); }

    // ── Static factories ─────────────────────────────────────────────────────

    public static RequirementsCalculatorPanel forGear(String itemName) {
        var mats = com.albionservant.data.CraftMaterialData.getMaterials(itemName);
        var qty  = com.albionservant.data.CraftQuantityData.get(itemName);
        List<MaterialLine> lines = new ArrayList<>();
        lines.add(new MaterialLine(mats.material1(), qty.mat1()));
        if (!com.albionservant.data.CraftMaterialData.NA.equals(mats.material2()) && qty.mat2() > 0)
            lines.add(new MaterialLine(mats.material2(), qty.mat2()));
        var art = com.albionservant.data.ArtifactData.getArtifactType(itemName);
        if (art != null)
            lines.add(new MaterialLine(art.displayName, 1.0));
        return new RequirementsCalculatorPanel(lines, 1);
    }

    public static RequirementsCalculatorPanel forFood(
            com.albionservant.data.FoodRecipeData.Recipe recipe) {
        List<MaterialLine> lines = new ArrayList<>();
        for (var ing : recipe.ingredients())
            lines.add(new MaterialLine(ing.name(), ing.quantity(),
                    "Avalonian Energy".equals(ing.name())));
        return new RequirementsCalculatorPanel(lines, recipe.batchSize());
    }

    public static RequirementsCalculatorPanel forPotion(
            com.albionservant.data.PotionRecipeData.PotionRecipe recipe) {
        List<MaterialLine> lines = new ArrayList<>();
        for (var ing : recipe.ingredients())
            lines.add(new MaterialLine(ing.name(), ing.quantity(),
                    "Avalonian Energy".equals(ing.name())));
        if (recipe.hasTrackingIngredient())
            lines.add(new MaterialLine(recipe.resolvedTrackingIngredient(), 1.0, true));
        return new RequirementsCalculatorPanel(lines, recipe.batchSize());
    }
}