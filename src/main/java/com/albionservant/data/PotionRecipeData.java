package com.albionservant.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete potion recipe data for Albion Online.
 * Potions craft in batches of 5 — all quantities are per-batch (5 potions).
 *
 * Tier naming convention:
 *   Minor  = T2/T3  (earliest unlocked)
 *   Base   = T4/T5  (mid-game)
 *   Major  = T6/T7/T8 (high-end)
 *
 * Sources: Albion Online wiki, albionfreemarket.com, mejoress.com recipe guide.
 * Note: mejoress guide has Invisibility/Major Poison descriptions swapped —
 * this file uses the CORRECT recipes from the wiki Template:Recipe page.
 */
public class PotionRecipeData {

    public record Ingredient(String name, int quantity) {}
    /**
     * @param trackingIngredient null for old potions; ingredient name for the 7 tracking potions.
     *                           The ingredient is tiered — the tier prefix (Rugged/Fine/Good/Excellent/Masterpiece)
     *                           is resolved automatically in the panel based on recipe tier.
     */
    public record PotionRecipe(String name, int tier,
                               List<Ingredient> ingredients,
                               String trackingIngredient) {
        /** Convenience: returns true if this potion needs a tracking ingredient */
        public boolean hasTrackingIngredient() { return trackingIngredient != null; }

        /** Resolves the tier-qualified ingredient name, e.g. "Fine Shadowpanther Claws" */
        public String resolvedTrackingIngredient() {
            if (trackingIngredient == null) return null;
            String prefix = switch (tier) {
                case 3, 4 -> "Rugged";
                case 5, 6 -> "Fine";
                case 7    -> "Excellent";
                case 8    -> "Masterpiece";
                default   -> "";
            };
            return prefix + " " + trackingIngredient;
        }
    }

    public static final List<String> CITIES = FoodRecipeData.CITIES;

    private static final Map<String, List<String>> CATEGORY_CHILDREN = new LinkedHashMap<>();
    private static final Map<String, PotionRecipe>  RECIPES           = new HashMap<>();

    static {
        // ── Choice tree ───────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Potion", List.of(
                "Healing Potions",
                "Energy Potions",
                "Gigantify Potions",
                "Resistance Potions",
                "Sticky Potions",
                "Poison Potions",
                "Invisibility Potions",
                "Cleansing Potions",
                "Calming Potions",
                "Acid Potions",
                "Berserk Potions",
                "Hellfire Potions",
                "Tornado in a Bottle",
                "Gathering Potions",
                "Ingredients"
        ));

        // ── HEALING POTIONS ───────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Healing Potions", List.of(
                "Minor Healing Potion",
                "Healing Potion",
                "Major Healing Potion"
        ));
        r("Minor Healing Potion",  2, i("Arcane Agaric", 8));
        r("Healing Potion",        4, i("Crenellated Burdock", 24), i("Hen Eggs", 6));
        r("Major Healing Potion",  6, i("Elusive Foxglove", 72), i("Goose Eggs", 18),
                i("Potato Schnapps", 18));

        // ── ENERGY POTIONS ────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Energy Potions", List.of(
                "Minor Energy Potion",
                "Energy Potion",
                "Major Energy Potion"
        ));
        r("Minor Energy Potion",   2, i("Arcane Agaric", 8));
        r("Energy Potion",         4, i("Crenellated Burdock", 24), i("Goat's Milk", 6));
        r("Major Energy Potion",   6, i("Elusive Foxglove", 72), i("Sheep's Milk", 18),
                i("Potato Schnapps", 18));

        // ── GIGANTIFY POTIONS ─────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Gigantify Potions", List.of(
                "Minor Gigantify Potion",
                "Gigantify Potion",
                "Major Gigantify Potion"
        ));
        r("Minor Gigantify Potion", 3, i("Brightleaf Comfrey", 8));
        r("Gigantify Potion",       5, i("Crenellated Burdock", 12),
                i("Dragon Teasel", 24), i("Goose Eggs", 6));
        r("Major Gigantify Potion", 7, i("Corn Hooch", 18), i("Goose Eggs", 18),
                i("Elusive Foxglove", 36), i("Firetouched Mullein", 72));

        // ── RESISTANCE POTIONS ────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Resistance Potions", List.of(
                "Minor Resistance Potion",
                "Resistance Potion",
                "Major Resistance Potion"
        ));
        r("Minor Resistance Potion", 3, i("Brightleaf Comfrey", 8));
        r("Resistance Potion",       5, i("Crenellated Burdock", 12),
                i("Dragon Teasel", 24), i("Goat's Milk", 6));
        r("Major Resistance Potion", 7, i("Crenellated Burdock", 36), i("Corn Hooch", 18),
                i("Sheep's Milk", 18), i("Elusive Foxglove", 36),
                i("Firetouched Mullein", 72));

        // ── STICKY POTIONS ────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Sticky Potions", List.of(
                "Minor Sticky Potion",
                "Sticky Potion",
                "Major Sticky Potion"
        ));
        r("Minor Sticky Potion", 3, i("Brightleaf Comfrey", 8));
        r("Sticky Potion",       5, i("Crenellated Burdock", 12),
                i("Dragon Teasel", 24), i("Goose Eggs", 6));
        r("Major Sticky Potion", 7, i("Crenellated Burdock", 36), i("Goose Eggs", 18),
                i("Elusive Foxglove", 36), i("Firetouched Mullein", 72));

        // ── POISON POTIONS ────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Poison Potions", List.of(
                "Minor Poison Potion",
                "Poison Potion",
                "Major Poison Potion"
        ));
        r("Minor Poison Potion", 4, i("Crenellated Burdock", 8), i("Brightleaf Comfrey", 4));
        r("Poison Potion",       6, i("Dragon Teasel", 12), i("Brightleaf Comfrey", 12),
                i("Sheep's Milk", 6), i("Elusive Foxglove", 24));
        // Confirmed from wiki Template:Recipe — mejoress descriptions were swapped
        r("Major Poison Potion", 8, i("Ghoul Yarrow", 72), i("Firetouched Mullein", 24),
                i("Dragon Teasel", 24), i("Cow's Milk", 18),
                i("Pumpkin Moonshine", 18));

        // ── INVISIBILITY POTIONS ──────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Invisibility Potions", List.of(
                "Invisibility Potion"
        ));
        // Confirmed from wiki Template:Recipe
        r("Invisibility Potion",  8, i("Ghoul Yarrow", 72), i("Firetouched Mullein", 36),
                i("Dragon Teasel", 36), i("Cow's Milk", 18),
                i("Pumpkin Moonshine", 18));

        // ── CLEANSING POTIONS (Sylvian Roots) ────────────────────────────────
        CATEGORY_CHILDREN.put("Cleansing Potions", List.of(
                "Minor Cleansing Potion",
                "Cleansing Potion",
                "Major Cleansing Potion"
        ));
        rt("Minor Cleansing Potion", 3, "Sylvian Roots",
                i("Brightleaf Comfrey", 8), i("Arcane Agaric", 4));
        rt("Cleansing Potion",       5, "Sylvian Roots",
                i("Crenellated Burdock", 24), i("Arcane Agaric", 12));
        rt("Major Cleansing Potion", 7, "Sylvian Roots",
                i("Elusive Foxglove", 36), i("Firetouched Mullein", 36), i("Goose Eggs", 18));

        // ── CALMING POTIONS (Shadowpanther Claws) ────────────────────────────
        CATEGORY_CHILDREN.put("Calming Potions", List.of(
                "Minor Calming Potion",
                "Calming Potion",
                "Major Calming Potion"
        ));
        rt("Minor Calming Potion", 3, "Shadowpanther Claws",
                i("Brightleaf Comfrey", 8), i("Arcane Agaric", 4));
        rt("Calming Potion",       5, "Shadowpanther Claws",
                i("Crenellated Burdock", 24), i("Arcane Agaric", 12));
        rt("Major Calming Potion", 8, "Shadowpanther Claws",
                i("Ghoul Yarrow", 72), i("Firetouched Mullein", 36),
                i("Dragon Teasel", 36), i("Cow's Milk", 18));

        // ── ACID POTIONS (Spirit Bear Paws) ──────────────────────────────────
        CATEGORY_CHILDREN.put("Acid Potions", List.of(
                "Minor Acid Potion",
                "Acid Potion",
                "Major Acid Potion"
        ));
        rt("Minor Acid Potion", 3, "Spirit Bear Paws",
                i("Brightleaf Comfrey", 8));
        rt("Acid Potion",       5, "Spirit Bear Paws",
                i("Crenellated Burdock", 12), i("Dragon Teasel", 24), i("Goat's Milk", 6));
        rt("Major Acid Potion", 8, "Spirit Bear Paws",
                i("Ghoul Yarrow", 72), i("Firetouched Mullein", 36),
                i("Elusive Foxglove", 36), i("Cow's Milk", 18), i("Pumpkin Moonshine", 18));

        // ── BERSERK POTIONS (Werewolf Fangs) ─────────────────────────────────
        CATEGORY_CHILDREN.put("Berserk Potions", List.of(
                "Minor Berserk Potion",
                "Berserk Potion",
                "Major Berserk Potion"
        ));
        rt("Minor Berserk Potion", 4, "Werewolf Fangs",
                i("Crenellated Burdock", 8), i("Brightleaf Comfrey", 4));
        rt("Berserk Potion",       6, "Werewolf Fangs",
                i("Dragon Teasel", 12), i("Brightleaf Comfrey", 12),
                i("Sheep's Milk", 6), i("Elusive Foxglove", 24));
        rt("Major Berserk Potion", 8, "Werewolf Fangs",
                i("Ghoul Yarrow", 72), i("Firetouched Mullein", 36),
                i("Dragon Teasel", 36), i("Cow's Milk", 18), i("Pumpkin Moonshine", 18));

        // ── HELLFIRE POTIONS (Imp Horns) ──────────────────────────────────────
        CATEGORY_CHILDREN.put("Hellfire Potions", List.of(
                "Minor Hellfire Potion",
                "Hellfire Potion",
                "Major Hellfire Potion"
        ));
        rt("Minor Hellfire Potion", 4, "Imp Horns",
                i("Crenellated Burdock", 8), i("Brightleaf Comfrey", 4));
        rt("Hellfire Potion",       6, "Imp Horns",
                i("Dragon Teasel", 24), i("Elusive Foxglove", 24), i("Sheep's Milk", 6));
        rt("Major Hellfire Potion", 8, "Imp Horns",
                i("Ghoul Yarrow", 72), i("Firetouched Mullein", 36),
                i("Elusive Foxglove", 36), i("Cow's Milk", 18), i("Pumpkin Moonshine", 18));

        // ── TORNADO IN A BOTTLE (Dawnbird Feathers) ──────────────────────────
        CATEGORY_CHILDREN.put("Tornado in a Bottle", List.of(
                "Minor Tornado in a Bottle",
                "Tornado in a Bottle",
                "Major Tornado in a Bottle"
        ));
        rt("Minor Tornado in a Bottle", 4, "Dawnbird Feathers",
                i("Crenellated Burdock", 12), i("Dragon Teasel", 12));
        rt("Tornado in a Bottle",       6, "Dawnbird Feathers",
                i("Dragon Teasel", 24), i("Elusive Foxglove", 24), i("Goat's Milk", 6));
        rt("Major Tornado in a Bottle", 8, "Dawnbird Feathers",
                i("Ghoul Yarrow", 72), i("Firetouched Mullein", 36),
                i("Elusive Foxglove", 36), i("Cow's Milk", 18), i("Pumpkin Moonshine", 18));

        // ── GATHERING POTIONS (Runestone Golem Teeth) ────────────────────────
        CATEGORY_CHILDREN.put("Gathering Potions", List.of(
                "Minor Gathering Potion",
                "Gathering Potion",
                "Major Gathering Potion"
        ));
        rt("Minor Gathering Potion", 3, "Runestone Golem Teeth",
                i("Brightleaf Comfrey", 8));
        rt("Gathering Potion",       5, "Runestone Golem Teeth",
                i("Crenellated Burdock", 12), i("Dragon Teasel", 24), i("Goat's Milk", 6));
        rt("Major Gathering Potion", 7, "Runestone Golem Teeth",
                i("Firetouched Mullein", 72), i("Elusive Foxglove", 36), i("Goose Eggs", 18));

        // ── INTERMEDIATE INGREDIENTS ──────────────────────────────────────────
        // These are crafted before being used in higher-tier potions
        CATEGORY_CHILDREN.put("Ingredients", List.of(
                "Potato Schnapps",
                "Corn Hooch",
                "Pumpkin Moonshine"
        ));
        r("Potato Schnapps",    6, i("Potato", 1));
        r("Corn Hooch",         7, i("Bundle of Corn", 1));
        r("Pumpkin Moonshine",  8, i("Pumpkin", 1));
    }

    // ── Arcane Extract tiers (for enchanting .1/.2/.3) ───────────────────────
    public static final List<String> ARCANE_EXTRACTS = List.of(
            "Basic Arcane Extract",    // → .1
            "Refined Arcane Extract",  // → .2
            "Pure Arcane Extract"      // → .3
    );

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Ingredient i(String name, int qty) {
        return new Ingredient(name, qty);
    }

    /** Standard potion — no tracking ingredient */
    @SafeVarargs
    private static void r(String name, int tier, Ingredient... ingredients) {
        RECIPES.put(name, new PotionRecipe(name, tier, List.of(ingredients), null));
    }

    /** Tracking potion — requires a tier-matched tracking ingredient */
    @SafeVarargs
    private static void rt(String name, int tier, String trackingIngredient, Ingredient... ingredients) {
        RECIPES.put(name, new PotionRecipe(name, tier, List.of(ingredients), trackingIngredient));
    }

    public static List<String> getTopCategories() {
        return CATEGORY_CHILDREN.get("Potion");
    }

    public static List<String> getCategoryChildren(String category) {
        return CATEGORY_CHILDREN.getOrDefault(category, List.of());
    }

    public static PotionRecipe getRecipe(String name) {
        return RECIPES.get(name);
    }

    public static boolean hasRecipe(String name) {
        return RECIPES.containsKey(name);
    }
}