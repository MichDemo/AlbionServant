package com.albionservant.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
/**
 * Compact inline material list that lives in the icon column.
 * Shows: Material | Net quantity
 * Below: RRR value
 *
 * Reads from existing Quantity TextField, Bonus Craft ComboBox,
 * Focus CheckBox and optional Daily Bonus controls.
 */
// ALBIONSERVANT_NUTRITION_RRR_PATCH_V5
// ALBIONSERVANT_CRAFTING_SPREADSHEET_FIXES_PATCH_V1
public class RequirementsCalculatorPanel extends VBox {

    public record MaterialLine(String name, double quantityPerBatch, boolean noRrr) {
        /**
         * Convenience constructor ? RRR applies by default.
         */
        public MaterialLine(String name, double quantityPerBatch) {
            this(name, quantityPerBatch, false);
        }
    }

    private final List<MaterialLine> materials;
    private final int batchSize;

    private final VBox rowsBox = new VBox(3);
    private final Label rrrLabel = new Label();

    private final ReadOnlyDoubleWrapper rrrFraction =
            new ReadOnlyDoubleWrapper(0.0);
    private TextField focusTotalField = null;
    private Label focusTotalLabel = null;

    private TextField quantityField;
    private ComboBox<?> bonusCraftCombo;
    private CheckBox focusCheckBox;
    private CheckBox dailyBonusCheckBox;
    private ComboBox<?> dailyBonusCombo;
    private ComboBox<?> hoQualityCombo;
    private ComboBox<?> hoPowerCombo;

    private int itemTier = 4;
    private int totalMaterialsPerCraft = 16;
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
                "-fx-background-color: #4b5058;" +
                "-fx-background-radius: 4;" +
                "-fx-padding: 3 8 3 8;"
        );

        rowsBox.setFillWidth(true);
        rowsBox.setStyle(
                "-fx-background-color: #202328;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 10 8 10;"
        );

        rrrLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #9ca2ab;" +
                "-fx-padding: 2 0 0 2;"
        );

        getChildren().addAll(title, rowsBox);
    }

    public Label getRrrLabel() {
        return rrrLabel;
    }

    public double getRrrFraction() {
        return rrrFraction.get();
    }

    public ReadOnlyDoubleProperty rrrFractionProperty() {
        return rrrFraction.getReadOnlyProperty();
    }

    public void setFocusTotalField(TextField field) {
        this.focusTotalField = field;
        render();
    }

    public void setFocusTotalLabel(Label label) {
        this.focusTotalLabel = label;
    }

    public void setFocusContext(int tier, int totalMaterialsPerCraft, java.util.function.IntSupplier specSupplier) {
        this.itemTier = tier;
        this.totalMaterialsPerCraft = totalMaterialsPerCraft;
        this.specSupplier = specSupplier;
        render();
    }

    public void bindControls(
            TextField quantityField,
            ComboBox<?> bonusCraftCombo,
            CheckBox focusCheckBox,
            ComboBox<?> hoQualityCombo,
            ComboBox<?> hoPowerCombo
    ) {
        bindControls(
                quantityField,
                bonusCraftCombo,
                focusCheckBox,
                null,
                null,
                hoQualityCombo,
                hoPowerCombo
        );
    }

    public void bindControls(
            TextField quantityField,
            ComboBox<?> bonusCraftCombo,
            CheckBox focusCheckBox,
            CheckBox dailyBonusCheckBox,
            ComboBox<?> dailyBonusCombo,
            ComboBox<?> hoQualityCombo,
            ComboBox<?> hoPowerCombo
    ) {
        this.quantityField = quantityField;
        this.bonusCraftCombo = bonusCraftCombo;
        this.focusCheckBox = focusCheckBox;
        this.dailyBonusCheckBox = dailyBonusCheckBox;
        this.dailyBonusCombo = dailyBonusCombo;
        this.hoQualityCombo = hoQualityCombo;
        this.hoPowerCombo = hoPowerCombo;

        if (quantityField != null) {
            quantityField.textProperty().addListener((o, ov, nv) -> render());
        }

        if (bonusCraftCombo != null) {
            bonusCraftCombo.valueProperty().addListener((o, ov, nv) -> render());
        }

        if (focusCheckBox != null) {
            focusCheckBox.selectedProperty().addListener((o, ov, nv) -> render());
        }

        if (dailyBonusCheckBox != null) {
            dailyBonusCheckBox.selectedProperty().addListener((o, ov, nv) -> render());
        }

        if (dailyBonusCombo != null) {
            dailyBonusCombo.valueProperty().addListener((o, ov, nv) -> render());
        }

        if (hoQualityCombo != null) {
            hoQualityCombo.valueProperty().addListener((o, ov, nv) -> render());
        }

        if (hoPowerCombo != null) {
            hoPowerCombo.valueProperty().addListener((o, ov, nv) -> render());
        }

        render();
    }

    public void bindControls(TextField quantityField, ComboBox<?> bonusCraftCombo, CheckBox focusCheckBox) {
        bindControls(quantityField, bonusCraftCombo, focusCheckBox, null, null, null, null);
    }

    private void render() {
        if (quantityField == null) {
            return;
        }

        int quantity;

        try {
            quantity = Math.max(1, Integer.parseInt(quantityField.getText().trim()));
        } catch (NumberFormatException e) {
            quantity = 1;
        }

        double lpb = computeLPB();
        double rrr = lpb / (100.0 + lpb);
        rrrFraction.set(rrr);
        double batches = Math.ceil((double) quantity / batchSize);

        rowsBox.getChildren().clear();

        rowsBox.getChildren().add(row(
                lbl("Material", 0.62, "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #9ca2ab;"),
                lbl("Net", 0.38, "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4ade80;")
        ));

        for (MaterialLine mat : materials) {
            double gross = batches * mat.quantityPerBatch();

            long net = mat.noRrr()
                    ? (long) Math.ceil(gross)
                    : (long) Math.ceil(gross * (1.0 - rrr));

            rowsBox.getChildren().add(row(
                    lbl(mat.name(), 0.62, "-fx-font-size: 14px; -fx-text-fill: #e6e8eb;"),
                    lbl(
                            fmt(net),
                            0.38,
                            "-fx-font-size: 14px; -fx-font-weight: bold;" +
                                    (mat.noRrr()
                                            ? " -fx-text-fill: #0ea5e9;"
                                            : " -fx-text-fill: #4ade80;")
                    )
            ));
        }

        rrrLabel.setText(String.format("RRR: %.2f%% (LPB: %.1f%%)", rrr * 100, lpb));

        int spec = specSupplier.getAsInt();

        long perItem = com.albionservant.data.FocusCostCalculator.withSpec(
                com.albionservant.data.FocusCostCalculator.baseFocusCost(
                        itemTier,
                        totalMaterialsPerCraft
                ),
                spec
        );

        long total = perItem * (long) batches;

        if (focusTotalField != null) {
            focusTotalField.setText(String.format("%,d", total));
        }

        if (focusTotalLabel != null) {
            focusTotalLabel.setText(String.format("Total: %,d", total));
        }
    }

    private double computeLPB() {
        double lpb;

        String loc = bonusCraftCombo != null && bonusCraftCombo.getValue() != null
                ? String.valueOf(bonusCraftCombo.getValue())
                : "Royal City";

        switch (loc) {
            case "Royal Island" -> lpb = 0.0;
            case "Royal City" -> lpb = 18.0;
            case "Royal City + Bonus" -> lpb = 33.0;
            case "HO" -> {
                int zq = parseHoQuality();
                int pl = parseHoPower();

                lpb = 18.0
                        + (pl - 1)
                        + (2.0 + zq) * 5.0
                        + (pl - 1) * 2.0;
            }
            default -> lpb = 18.0;
        }

        if (focusCheckBox != null && focusCheckBox.isSelected()) {
            lpb += 59.0;
        }

        if (dailyBonusCheckBox != null && dailyBonusCheckBox.isSelected()) {
            lpb += parseDailyBonus();
        }

        return lpb;
    }

    private double parseDailyBonus() {
        if (dailyBonusCombo == null || dailyBonusCombo.getValue() == null) {
            return 10.0;
        }

        String raw = String.valueOf(dailyBonusCombo.getValue());
        String digits = raw.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return 10.0;
        }

        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return 10.0;
        }
    }

    private int parseHoQuality() {
        if (hoQualityCombo == null || hoQualityCombo.getValue() == null) {
            return 1;
        }

        try {
            return Integer.parseInt(String.valueOf(hoQualityCombo.getValue()).replace("Q", "").trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int parseHoPower() {
        if (hoPowerCombo == null || hoPowerCombo.getValue() == null) {
            return 1;
        }

        Object value = hoPowerCombo.getValue();

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private HBox row(Label... cells) {
        HBox r = new HBox();
        r.setMaxWidth(Double.MAX_VALUE);
        r.setAlignment(Pos.CENTER_LEFT);

        for (Label l : cells) {
            r.getChildren().add(l);
        }

        r.setMinWidth(320);

        return r;
    }

    private Label lbl(String text, double pct, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setTooltip(text == null || text.isBlank() ? null : new Tooltip(text));

        if (pct >= 0.5) {
            l.setMinWidth(220);
            l.setPrefWidth(260);
            HBox.setHgrow(l, Priority.ALWAYS);
        } else {
            l.setMinWidth(95);
            l.setPrefWidth(110);
            HBox.setHgrow(l, Priority.NEVER);
        }

        return l;
    }

    private String fmt(long n) {
        return String.format("%,d", n);
    }

    public static RequirementsCalculatorPanel forGear(String itemName) {
        var mats = com.albionservant.data.CraftMaterialData.getMaterials(itemName);
        var qty = com.albionservant.data.CraftQuantityData.get(itemName);

        List<MaterialLine> lines = new ArrayList<>();

        lines.add(new MaterialLine(mats.material1(), qty.mat1()));

        if (!com.albionservant.data.CraftMaterialData.NA.equals(mats.material2()) && qty.mat2() > 0) {
            lines.add(new MaterialLine(mats.material2(), qty.mat2()));
        }

        var art = com.albionservant.data.ArtifactData.getArtifactType(itemName);

        if (art != null) {
            String artifactName = com.albionservant.integration.market
                    .LocalMarketPriceService
                    .artifactColumnDisplayName(itemName, art.displayName);
            int artifactQuantity = com.albionservant.integration.market
                    .LocalMarketPriceService
                    .artifactQuantity(itemName, 4);
            boolean returnable = com.albionservant.integration.market
                    .LocalMarketPriceService
                    .artifactReceivesReturns(itemName, art.name(), 4);

            lines.add(new MaterialLine(
                    artifactName,
                    Math.max(1, artifactQuantity),
                    !returnable
            ));
        }

        return new RequirementsCalculatorPanel(lines, 1);
    }

    public static RequirementsCalculatorPanel forFood(
            com.albionservant.data.FoodRecipeData.Recipe recipe
    ) {
        List<MaterialLine> lines = new ArrayList<>();

        for (var ing : recipe.ingredients()) {
            lines.add(new MaterialLine(ing.name(), ing.quantity(), "Avalonian Energy".equals(ing.name())));
        }

        return new RequirementsCalculatorPanel(lines, recipe.batchSize());
    }

    public static RequirementsCalculatorPanel forPotion(
            com.albionservant.data.PotionRecipeData.PotionRecipe recipe
    ) {
        List<MaterialLine> lines = new ArrayList<>();

        for (var ing : recipe.ingredients()) {
            lines.add(new MaterialLine(ing.name(), ing.quantity(), "Avalonian Energy".equals(ing.name())));
        }

        if (recipe.hasTrackingIngredient()) {
            lines.add(new MaterialLine(recipe.resolvedTrackingIngredient(), 1.0, true));
        }

        return new RequirementsCalculatorPanel(lines, recipe.batchSize());
    }
}
