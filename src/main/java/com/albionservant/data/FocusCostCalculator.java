package com.albionservant.data;

/**
 * Focus cost calculator for crafting: gear, food and potions.
 */
public class FocusCostCalculator {

    /**
     * Efficiency bonus per spec level for the main item specialization.
     */
    private static final double MAIN_SPEC_BONUS = 2.8;

    /**
     * Applies spec-level reduction to a base focus cost.
     */
    public static long withSpec(double baseFocus, int specLevel) {
        int safeSpec = Math.max(0, Math.min(100, specLevel));
        double exponent = (safeSpec * MAIN_SPEC_BONUS) / 100.0;
        return (long) Math.ceil(baseFocus * Math.pow(0.5, exponent));
    }

    /**
     * Computes the base focus cost before spec reduction.
     *
     * Formula:
     * Focus = 10 * totalMaterials * 1.75^(tier - 1 + enchantLevel)
     */
    public static double baseFocusCost(int tier, int enchantLevel, double totalMaterials) {
        int safeTier = Math.max(1, tier);
        int safeEnchant = Math.max(0, Math.min(4, enchantLevel));
        double safeMaterials = Math.max(0.0, totalMaterials);

        return 10.0 * safeMaterials * Math.pow(1.75, safeTier - 1 + safeEnchant);
    }

    public static double baseFocusCost(int tier, int enchantLevel, int totalMaterials) {
        return baseFocusCost(tier, enchantLevel, (double) totalMaterials);
    }

    public static double baseFocusCost(int tier, int totalMaterials) {
        return baseFocusCost(tier, 0, totalMaterials);
    }

    public static long compute(int tier, int enchantLevel, double totalMaterials, int specLevel) {
        return withSpec(baseFocusCost(tier, enchantLevel, totalMaterials), specLevel);
    }

    public static long compute(int tier, int enchantLevel, int totalMaterials, int specLevel) {
        return compute(tier, enchantLevel, (double) totalMaterials, specLevel);
    }

    public static long compute(int tier, int totalMaterials, int specLevel) {
        return compute(tier, 0, totalMaterials, specLevel);
    }

    /**
     * Focus cost for a gear item.
     */
    public static long forGear(String itemName, int tier, int specLevel) {
        CraftQuantityData.Quantities q = CraftQuantityData.get(itemName);

        int total = q.mat1() + q.mat2();

        if (ArtifactData.getArtifactType(itemName) != null) {
            total += 1;
        }

        return compute(tier, 0, total, specLevel);
    }

    /**
     * Focus cost for food, per finished food item.
     *
     * enchantLevel:
     * 0 = .0 food
     * 1 = .1 food, Basic Fish Sauce
     * 2 = .2 food, Fancy Fish Sauce
     * 3 = .3 food, Special Fish Sauce
     *
     * The recipe ingredient quantities are stored per batch, so this method
     * converts them to materials per finished item using recipe.batchSize().
     *
     * For enchanted food, this assumes one fish sauce per finished food item.
     */
    public static long forFood(FoodRecipeData.Recipe recipe, int enchantLevel, int specLevel) {
        if (recipe == null) {
            return 0L;
        }

        int batchSize = Math.max(1, recipe.batchSize());

        int totalPerBatch = recipe.ingredients().stream()
                .mapToInt(FoodRecipeData.Ingredient::quantity)
                .sum();

        double materialsPerItem = totalPerBatch / (double) batchSize;

        if (enchantLevel > 0) {
            materialsPerItem += 1.0;
        }

        return compute(recipe.tier(), enchantLevel, materialsPerItem, specLevel);
    }

    /**
     * Backwards-compatible overload for old code.
     */
    public static long forFood(FoodRecipeData.Recipe recipe, int specLevel) {
        return forFood(recipe, 0, specLevel);
    }

    /**
     * Focus cost for a potion, per finished potion item.
     */
    public static long forPotion(PotionRecipeData.PotionRecipe recipe, int specLevel) {
        if (recipe == null) {
            return 0L;
        }

        int batchSize = Math.max(1, recipe.batchSize());

        int totalPerBatch = recipe.ingredients().stream()
                .mapToInt(PotionRecipeData.Ingredient::quantity)
                .sum();

        if (recipe.hasTrackingIngredient()) {
            totalPerBatch += 1;
        }

        double materialsPerItem = totalPerBatch / (double) batchSize;

        return compute(recipe.tier(), 0, materialsPerItem, specLevel);
    }

    /**
     * Returns a human-readable summary of the focus breakdown for display.
     */
    public static String summary(int tier, int totalMaterials, int specLevel) {
        double base = baseFocusCost(tier, totalMaterials);
        long reduced = withSpec(base, specLevel);
        double pct = (base > 0) ? (reduced / base) * 100.0 : 100.0;

        return String.format(
                "Base: %,.0f | Spec %d -> %,d focus (%.1f%% of base)",
                base,
                specLevel,
                reduced,
                pct
        );
    }
}
