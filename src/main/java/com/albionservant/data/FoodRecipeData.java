package com.albionservant.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete food recipe data for Albion Online.
 *
 * Each recipe maps to an ordered list of Ingredient records.
 * Quantities represent ingredients needed per 10 food items (base batch).
 * Fish variant recipes include a fish ingredient + herb + milk on top of base.
 *
 * Fish sauce materials (Basic/Fancy/Special Fish Sauce) are embedded as
 * optional enchanting materials — not a separate panel.
 */
public class FoodRecipeData {

    public record Ingredient(String name, int quantity) {}
    public record Recipe(String name, int tier, List<Ingredient> ingredients) {}

    // Cities where food can be sold/bought — used for the city price rows
    public static final List<String> CITIES = List.of(
            "Brecilien", "Bridgewatch", "Caerleon",
            "Fort Sterling", "Lymhurst", "Martlock", "Thetford"
    );

    // Fish sauce tiers for enchanting
    public static final List<String> FISH_SAUCES = List.of(
            "Basic Fish Sauce", "Fancy Fish Sauce", "Special Fish Sauce"
    );

    private static final Map<String, List<String>> CATEGORY_CHILDREN = new LinkedHashMap<>();
    private static final Map<String, Recipe>       RECIPES           = new HashMap<>();

    static {
        // ── Choice tree: Food → categories → items ────────────────────────────
        CATEGORY_CHILDREN.put("Food", List.of(
                "Soups", "Salads", "Omelettes", "Pies",
                "Stews", "Roasts", "Sandwiches", "Fish & Other"
        ));

        // ── SOUPS ────────────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Soups", List.of(
                "Carrot Soup",
                "Greenmoor Clam Soup",
                "Wheat Soup",
                "Murkwater Clam Soup",
                "Cabbage Soup",
                "Blackbog Clam Soup"
        ));
        recipe("Carrot Soup",            1, ing("Carrot", 16));
        recipe("Greenmoor Clam Soup",    1, ing("Carrot", 2), ing("Greenmoor Clam", 1));
        recipe("Wheat Soup",             3, ing("Sheaf of Wheat", 48));
        recipe("Murkwater Clam Soup",    3, ing("Raw Chicken", 1), ing("Murkwater Clam", 1),
                ing("Sheaf of Wheat", 2), ing("Brightleaf Comfrey", 2));
        recipe("Cabbage Soup",           5, ing("Cabbage", 144));
        recipe("Blackbog Clam Soup",     5, ing("Cabbage", 6), ing("Raw Goose", 6),
                ing("Blackbog Clam", 1), ing("Dragon Teasel", 6));

        // ── SALADS ───────────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Salads", List.of(
                "Bean Salad",
                "Shallowshore Squid Salad",
                "Turnip Salad",
                "Midwater Octopus Salad",
                "Potato Salad",
                "Deepwater Kraken Salad"
        ));
        recipe("Bean Salad",                2, ing("Carrot", 8), ing("Bean", 8));
        recipe("Shallowshore Squid Salad",  2, ing("Bean", 1), ing("Arcane Agaric", 1),
                ing("Shallowshore Squid", 1));
        recipe("Turnip Salad",              4, ing("Sheaf of Wheat", 24), ing("Turnip", 24));
        recipe("Midwater Octopus Salad",    4, ing("Turnip", 2), ing("Raw Goat", 2),
                ing("Crenellated Burdock", 2), ing("Midwater Octopus", 1));
        recipe("Potato Salad",              6, ing("Potato", 24), ing("Cabbage", 24));
        recipe("Deepwater Kraken Salad",    6, ing("Potato", 2), ing("Deepwater Kraken", 1),
                ing("Elusive Foxglove", 2), ing("Raw Mutton", 2));

        // ── OMELETTES ────────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Omelettes", List.of(
                "Chicken Omelette",
                "Lowriver Crab Omelette",
                "Avalonian Chicken Omelette",
                "Goose Omelette",
                "Drybrook Crab Omelette",
                "Avalonian Goose Omelette",
                "Pork Omelette",
                "Dusthole Crab Omelette",
                "Avalonian Pork Omelette"
        ));
        recipe("Chicken Omelette",           3, ing("Sheaf of Wheat", 4), ing("Raw Chicken", 8),
                ing("Hen Eggs", 2));
        recipe("Lowriver Crab Omelette",     3, ing("Lowriver Crab", 1), ing("Hen Eggs", 1),
                ing("Brightleaf Comfrey", 1));
        recipe("Avalonian Chicken Omelette", 3, ing("Sheaf of Wheat", 4), ing("Raw Chicken", 8),
                ing("Hen Eggs", 2), ing("Avalonian Energy", 10));
        recipe("Goose Omelette",             5, ing("Cabbage", 12), ing("Raw Goose", 24),
                ing("Goose Eggs", 6));
        recipe("Drybrook Crab Omelette",     5, ing("Cabbage", 2), ing("Drybrook Crab", 1),
                ing("Dragon Teasel", 2), ing("Goose Eggs", 2));
        recipe("Avalonian Goose Omelette",   5, ing("Cabbage", 12), ing("Raw Goose", 24),
                ing("Goose Eggs", 6), ing("Avalonian Energy", 30));
        recipe("Pork Omelette",              7, ing("Bundle of Corn", 36), ing("Raw Pork", 72),
                ing("Goose Eggs", 18));
        recipe("Dusthole Crab Omelette",     7, ing("Bundle of Corn", 6), ing("Raw Pork", 6),
                ing("Dusthole Crab", 1), ing("Firetouched Mullein", 6));
        recipe("Avalonian Pork Omelette",    7, ing("Bundle of Corn", 36), ing("Raw Pork", 72),
                ing("Goose Eggs", 18), ing("Avalonian Energy", 90));

        // ── PIES ─────────────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Pies", List.of(
                "Chicken Pie",
                "Upland Coldeye Pie",
                "Goose Pie",
                "Mountain Blindeye Pie",
                "Pork Pie",
                "Frostpeak Deadeye Pie"
        ));
        recipe("Chicken Pie",            3, ing("Sheaf of Wheat", 2), ing("Raw Chicken", 8),
                ing("Flour", 4));
        recipe("Upland Coldeye Pie",     3, ing("Upland Coldeye", 1), ing("Flour", 1),
                ing("Hen Eggs", 1));
        recipe("Goose Pie",              5, ing("Cabbage", 6), ing("Raw Goose", 24),
                ing("Goat's Milk", 6), ing("Flour", 12));
        recipe("Mountain Blindeye Pie",  5, ing("Cabbage", 26), ing("Mountain Blindeye", 1),
                ing("Dragon Teasel", 2), ing("Goose Eggs", 2));
        recipe("Pork Pie",               7, ing("Bundle of Corn", 18), ing("Raw Pork", 72),
                ing("Flour", 36), ing("Sheep's Milk", 18));
        recipe("Frostpeak Deadeye Pie",  7, ing("Bundle of Corn", 6), ing("Raw Pork", 6),
                ing("Frostpeak Deadeye", 1), ing("Firetouched Mullein", 6));

        // ── STEWS ────────────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Stews", List.of(
                "Goat Stew",
                "Greenriver Eel Stew",
                "Avalonian Goat Stew",
                "Mutton Stew",
                "Redspring Eel Stew",
                "Avalonian Mutton Stew",
                "Beef Stew",
                "Deadwater Eel Stew",
                "Avalonian Beef Stew"
        ));
        recipe("Goat Stew",             4, ing("Turnip", 4), ing("Bread", 4),
                ing("Raw Goat", 8));
        recipe("Greenriver Eel Stew",   4, ing("Turnip", 1), ing("Greenriver Eel", 1),
                ing("Crenellated Burdock", 1));
        recipe("Avalonian Goat Stew",   4, ing("Carrots", 4), ing("Turnips", 4),
                ing("Raw Goat", 8), ing("Avalonian Energy", 10));
        recipe("Mutton Stew",           6, ing("Potato", 12), ing("Bread", 12),
                ing("Raw Mutton", 24));
        recipe("Redspring Eel Stew",    6, ing("Potato", 2), ing("Redspring Eel", 1),
                ing("Elusive Foxglove", 2), ing("Sheep's Milk", 2));
        recipe("Avalonian Mutton Stew", 6, ing("Cabbage", 12), ing("Potatoes", 12),
                ing("Raw Mutton", 24), ing("Avalonian Energy", 30));
        recipe("Beef Stew",             8, ing("Pumpkin", 36), ing("Bread", 36),
                ing("Raw Beef", 72));
        recipe("Deadwater Eel Stew",    8, ing("Pumpkin", 6), ing("Deadwater Eel", 1),
                ing("Ghoul Yarrow", 6), ing("Cow's Milk", 6));
        recipe("Avalonian Beef Stew",   8, ing("Bundle of Corn", 36), ing("Pumpkin", 36),
                ing("Raw Beef", 72), ing("Avalonian Energy", 90));

        // ── ROASTS ───────────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Roasts", List.of(
                "Roast Chicken",
                "Roasted Whitefog Snapper",
                "Roast Goose",
                "Roasted Clearhaze Snapper",
                "Roast Pork",
                "Roasted Puremist Snapper"
        ));
        recipe("Roast Chicken",             3, ing("Raw Chicken", 8), ing("Sheaf of Wheat", 4),
                ing("Brightleaf Comfrey", 2), ing("Goat's Milk", 2));
        recipe("Roasted Whitefog Snapper",  3, ing("Whitefog Snapper", 1), ing("Sheaf of Wheat", 2),
                ing("Brightleaf Comfrey", 1), ing("Goat's Milk", 1));
        recipe("Roast Goose",               5, ing("Raw Goose", 24), ing("Cabbage", 12),
                ing("Dragon Teasel", 6), ing("Goat's Milk", 6));
        recipe("Roasted Clearhaze Snapper", 5, ing("Clearhaze Snapper", 1), ing("Cabbage", 2),
                ing("Dragon Teasel", 1), ing("Goat's Milk", 1));
        recipe("Roast Pork",                7, ing("Raw Pork", 72), ing("Bundle of Corn", 36),
                ing("Firetouched Mullein", 18), ing("Sheep's Milk", 18));
        recipe("Roasted Puremist Snapper",  7, ing("Puremist Snapper", 1), ing("Bundle of Corn", 6),
                ing("Firetouched Mullein", 3), ing("Sheep's Milk", 3));

        // ── SANDWICHES ───────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Sandwiches", List.of(
                "Goat Sandwich",
                "Stonestream Lurcher Sandwich",
                "Avalonian Goat Sandwich",
                "Mutton Sandwich",
                "Rushwater Lurcher Sandwich",
                "Avalonian Mutton Sandwich",
                "Beef Sandwich",
                "Thunderfall Lurcher Sandwich",
                "Avalonian Beef Sandwich"
        ));
        recipe("Goat Sandwich",                4, ing("Bread", 4), ing("Goat's Butter", 2),
                ing("Raw Goat", 8));
        recipe("Stonestream Lurcher Sandwich", 4, ing("Goat's Butter", 1), ing("Turnip", 1),
                ing("Stonestream Lurcher", 1));
        recipe("Avalonian Goat Sandwich",      4, ing("Bread", 4), ing("Goat's Butter", 2),
                ing("Raw Goat", 8), ing("Avalonian Energy", 10));
        recipe("Mutton Sandwich",              6, ing("Bread", 12), ing("Raw Mutton", 24),
                ing("Sheep's Butter", 6));
        recipe("Rushwater Lurcher Sandwich",   6, ing("Rushwater Lurcher", 1), ing("Potato", 2),
                ing("Elusive Foxglove", 2), ing("Sheep's Butter", 2));
        recipe("Avalonian Mutton Sandwich",    6, ing("Bread", 12), ing("Raw Mutton", 24),
                ing("Sheep's Butter", 6), ing("Avalonian Energy", 30));
        recipe("Beef Sandwich",                8, ing("Bread", 36), ing("Cow's Butter", 18),
                ing("Raw Beef", 72));
        recipe("Thunderfall Lurcher Sandwich", 8, ing("Thunderfall Lurcher", 1), ing("Pumpkin", 6),
                ing("Ghoul Yarrow", 6), ing("Cow's Butter", 6));
        recipe("Avalonian Beef Sandwich",      8, ing("Bread", 36), ing("Cow's Butter", 18),
                ing("Raw Beef", 72), ing("Avalonian Energy", 90));

        // ── FISH & OTHER ─────────────────────────────────────────────────────
        CATEGORY_CHILDREN.put("Fish & Other", List.of(
                "Grilled Fish",
                "Seaweed Salad",
                "Basic Fish Sauce",
                "Fancy Fish Sauce",
                "Special Fish Sauce",
                "Bread",
                "Flour"
        ));
        recipe("Grilled Fish",       1, ing("Chopped Fish", 10));
        recipe("Seaweed Salad",      1, ing("Seaweed", 10));
        recipe("Basic Fish Sauce",   1, ing("Chopped Fish", 1), ing("Seaweed", 1));
        recipe("Fancy Fish Sauce",   1, ing("Chopped Fish", 2), ing("Seaweed", 2),
                ing("Salt Crystal", 1));
        recipe("Special Fish Sauce", 1, ing("Chopped Fish", 4), ing("Seaweed", 4),
                ing("Salt Crystal", 2), ing("Purified Venom", 1));
        recipe("Bread",              4, ing("Flour", 1));
        recipe("Flour",              3, ing("Sheaf of Wheat", 1));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Ingredient ing(String name, int qty) {
        return new Ingredient(name, qty);
    }

    @SafeVarargs
    private static void recipe(String name, int tier, Ingredient... ingredients) {
        RECIPES.put(name, new Recipe(name, tier, List.of(ingredients)));
    }

    public static List<String> getCategoryChildren(String category) {
        return CATEGORY_CHILDREN.getOrDefault(category, List.of());
    }

    public static Recipe getRecipe(String itemName) {
        return RECIPES.get(itemName);
    }

    public static boolean hasRecipe(String itemName) {
        return RECIPES.containsKey(itemName);
    }

    /** Returns all top-level food categories */
    public static List<String> getTopCategories() {
        return CATEGORY_CHILDREN.get("Food");
    }
}