package com.albionservant.data;

/**
 * Focus cost calculator for crafting (gear, food, potions).
 *
 * Confirmed formulas from Albion Online wiki and forum (2024):
 *
 *   Base focus cost = 10 × totalMaterials × 1.75^(tier - 1)
 *     - tier: 2-8
 *     - totalMaterials: sum of all ingredient quantities per craft
 *       Gear: uses CraftQuantityData (mat1 + mat2 + 1 for artifact if present)
 *       Food: sum of all recipe ingredient quantities
 *       Potions: sum of all recipe ingredient quantities (per batch)
 *
 *   Focus reduction from specialization:
 *     exponent = (specLevel × MAIN_SPEC_BONUS) / 100
 *     reducedFocus = baseFocus × 0.5^exponent
 *
 *   MAIN_SPEC_BONUS = 2.8 per spec level (= 280 efficiency points per 100 spec)
 *   This matches the wiki: 0.5^(280/100) = 14.3% of base at max single spec.
 *
 *   For full reduction (all nodes maxed, 55,000 efficiency points):
 *   0.5^5.5 = 2.21% of base — this is the theoretical minimum.
 *
 *   Since AlbionServant tracks only the main item spec, the formula simplifies to:
 *     Focus = Base × 0.5^(specLevel × 2.8 / 100)
 *
 *   Reference: wiki.albiononline.com/wiki/Crafting_Focus
 *              forum.albiononline.com/Thread/126568 (cooking focus formula)
 *              forum.albiononline.com/Thread/198660 (analysis)
 */
public class FocusCostCalculator {

    /**
     * Efficiency bonus per spec level for the main item specialization.
     * Confirmed from forum post: T7 Soldier Boots uses 2.8 bonus factor.
     * This value applies to weapons, armor, food, and potions alike.
     */
    private static final double MAIN_SPEC_BONUS = 2.8;

    /**
     * Applies spec-level reduction to a base focus cost.
     *
     * @param baseFocus Base focus cost (from baseFocusCost())
     * @param specLevel Player's specialization level for this item (0-100)
     * @return Reduced focus cost, rounded up to nearest integer
     */
    public static long withSpec(double baseFocus, int specLevel) {
        double exponent = (specLevel * MAIN_SPEC_BONUS) / 100.0;
        return (long) Math.ceil(baseFocus * Math.pow(0.5, exponent));
    }

    /**
     * Computes the BASE focus cost per item (before spec reduction).
     *
     * @param tier           Item tier (2-8)
     * @param enchantLevel   Enchantment level (0-4); each adds one effective tier
     * @param totalMaterials Total raw materials consumed per single craft
     *
     * Confirmed formula: Focus = 10 × R × 1.75^(tier - 1 + enchantLevel)
     * Each enchant level multiplies focus by 1.75 — same as gaining one tier.
     * Verified: T4.3 Scholar Sandals (R=8) = 10×8×1.75^6 = 2299 ✓ (forum: 2293)
     */
    public static double baseFocusCost(int tier, int enchantLevel, int totalMaterials) {
        return 10.0 * totalMaterials * Math.pow(1.75, tier - 1 + enchantLevel);
    }

    /** Convenience overload for base (unenchanted) items */
    public static double baseFocusCost(int tier, int totalMaterials) {
        return baseFocusCost(tier, 0, totalMaterials);
    }

    /**
     * Full calculation: base + spec reduction in one call.
     *
     * @param tier           Item tier (2-8)
     * @param enchantLevel   Enchantment level (0-4)
     * @param totalMaterials Total raw materials per craft
     * @param specLevel      Player's spec level (0-100)
     */
    public static long compute(int tier, int enchantLevel, int totalMaterials, int specLevel) {
        return withSpec(baseFocusCost(tier, enchantLevel, totalMaterials), specLevel);
    }

    /** Convenience overload for unenchanted items */
    public static long compute(int tier, int totalMaterials, int specLevel) {
        return compute(tier, 0, totalMaterials, specLevel);
    }

    /**
     * Focus cost for a GEAR item — reads material counts from CraftQuantityData.
     *
     * @param itemName  Display name (e.g. "Broadsword")
     * @param tier      Crafting tier (2–8)
     * @param specLevel Player's main spec level for this item
     */
    public static long forGear(String itemName, int tier, int specLevel) {
        CraftQuantityData.Quantities q = CraftQuantityData.get(itemName);
        int total = q.mat1() + q.mat2();
        // Artifact adds 1 material slot (the artifact shard itself)
        if (ArtifactData.getArtifactType(itemName) != null) total += 1;
        return compute(tier, total, specLevel);
    }

    /**
     * Focus cost for a FOOD item — sums all ingredient quantities per batch.
     * The base focus is per item, so divide batch total by batchSize.
     *
     * @param recipe    Food recipe
     * @param specLevel Player's spec level for this food category
     */
    public static long forFood(FoodRecipeData.Recipe recipe, int specLevel) {
        int totalPerBatch = recipe.ingredients().stream()
                .mapToInt(FoodRecipeData.Ingredient::quantity)
                .sum();
        // Focus cost per item = totalPerBatch / batchSize — then scale back up
        double basePerItem = baseFocusCost(recipe.tier(), totalPerBatch / recipe.batchSize());
        return withSpec(basePerItem, specLevel);
    }

    /**
     * Focus cost for a POTION — sums all ingredient quantities per batch.
     *
     * @param recipe    Potion recipe
     * @param specLevel Player's spec level for this potion category
     */
    public static long forPotion(PotionRecipeData.PotionRecipe recipe, int specLevel) {
        int totalPerBatch = recipe.ingredients().stream()
                .mapToInt(PotionRecipeData.Ingredient::quantity)
                .sum();
        // Tracking ingredient contributes 1 unit per batch
        if (recipe.hasTrackingIngredient()) totalPerBatch += 1;
        double basePerItem = baseFocusCost(recipe.tier(), totalPerBatch / recipe.batchSize());
        return withSpec(basePerItem, specLevel);
    }

    /**
     * Returns a human-readable summary of the focus breakdown for display.
     *
     * @param tier           Tier
     * @param totalMaterials Total materials per craft
     * @param specLevel      Spec level
     */
    public static String summary(int tier, int totalMaterials, int specLevel) {
        double base    = baseFocusCost(tier, totalMaterials);
        long   reduced = withSpec(base, specLevel);
        double pct     = (base > 0) ? (reduced / base) * 100.0 : 100.0;
        return String.format(
                "Base: %,.0f  |  Spec %d  →  %,d focus  (%.1f%% of base)",
                base, specLevel, reduced, pct
        );
    }
}