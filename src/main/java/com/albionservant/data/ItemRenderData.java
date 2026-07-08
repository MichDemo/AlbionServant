package com.albionservant.data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Constructs Albion Online render API URLs for item icons.
 * API: https://render.albiononline.com/v1/item/{identifier}.png
 */
public class ItemRenderData {

    private static final String BASE_URL   = "https://render.albiononline.com/v1/item/";
    private static final String T8_PREFIX  = "Elder's ";

    // ── Food icon IDs ─────────────────────────────────────────────────────────
    private static final Map<String, String> FOOD_IDS = new HashMap<>();
    static {
        // Soups
        FOOD_IDS.put("Carrot Soup",                  "T1_MEAL_SOUP");
        FOOD_IDS.put("Greenmoor Clam Soup",           "T1_MEAL_SOUP_FISH");
        FOOD_IDS.put("Wheat Soup",                    "T3_MEAL_SOUP");
        FOOD_IDS.put("Murkwater Clam Soup",           "T3_MEAL_SOUP_FISH");
        FOOD_IDS.put("Cabbage Soup",                  "T5_MEAL_SOUP");
        FOOD_IDS.put("Blackbog Clam Soup",            "T5_MEAL_SOUP_FISH");
        // Salads
        FOOD_IDS.put("Bean Salad",                    "T2_MEAL_SALAD");
        FOOD_IDS.put("Shallowshore Squid Salad",      "T2_MEAL_SALAD_FISH");
        FOOD_IDS.put("Turnip Salad",                  "T4_MEAL_SALAD");
        FOOD_IDS.put("Midwater Octopus Salad",        "T4_MEAL_SALAD_FISH");
        FOOD_IDS.put("Potato Salad",                  "T6_MEAL_SALAD");
        FOOD_IDS.put("Deepwater Kraken Salad",        "T6_MEAL_SALAD_FISH");
        // Omelettes
        FOOD_IDS.put("Chicken Omelette",              "T3_MEAL_OMELETTE");
        FOOD_IDS.put("Lowriver Crab Omelette",        "T3_MEAL_OMELETTE_FISH");
        FOOD_IDS.put("Avalonian Chicken Omelette",    "T3_MEAL_OMELETTE_AVALON");
        FOOD_IDS.put("Goose Omelette",                "T5_MEAL_OMELETTE");
        FOOD_IDS.put("Drybrook Crab Omelette",        "T5_MEAL_OMELETTE_FISH");
        FOOD_IDS.put("Avalonian Goose Omelette",      "T5_MEAL_OMELETTE_AVALON");
        FOOD_IDS.put("Pork Omelette",                 "T7_MEAL_OMELETTE");
        FOOD_IDS.put("Dusthole Crab Omelette",        "T7_MEAL_OMELETTE_FISH");
        FOOD_IDS.put("Avalonian Pork Omelette",       "T7_MEAL_OMELETTE_AVALON");
        // Pies
        FOOD_IDS.put("Chicken Pie",                   "T3_MEAL_PIE");
        FOOD_IDS.put("Upland Coldeye Pie",            "T3_MEAL_PIE_FISH");
        FOOD_IDS.put("Goose Pie",                     "T5_MEAL_PIE");
        FOOD_IDS.put("Mountain Blindeye Pie",         "T5_MEAL_PIE_FISH");
        FOOD_IDS.put("Pork Pie",                      "T7_MEAL_PIE");
        FOOD_IDS.put("Frostpeak Deadeye Pie",         "T7_MEAL_PIE_FISH");
        // Stews
        FOOD_IDS.put("Goat Stew",                     "T4_MEAL_STEW");
        FOOD_IDS.put("Greenriver Eel Stew",           "T4_MEAL_STEW_FISH");
        FOOD_IDS.put("Avalonian Goat Stew",           "T4_MEAL_STEW_AVALON");
        FOOD_IDS.put("Mutton Stew",                   "T6_MEAL_STEW");
        FOOD_IDS.put("Redspring Eel Stew",            "T6_MEAL_STEW_FISH");
        FOOD_IDS.put("Avalonian Mutton Stew",         "T6_MEAL_STEW_AVALON");
        FOOD_IDS.put("Beef Stew",                     "T8_MEAL_STEW");
        FOOD_IDS.put("Deadwater Eel Stew",            "T8_MEAL_STEW_FISH");
        FOOD_IDS.put("Avalonian Beef Stew",           "T8_MEAL_STEW_AVALON");
        // Roasts
        FOOD_IDS.put("Roast Chicken",                 "T3_MEAL_ROAST");
        FOOD_IDS.put("Roasted Whitefog Snapper",      "T3_MEAL_ROAST_FISH");
        FOOD_IDS.put("Roast Goose",                   "T5_MEAL_ROAST");
        FOOD_IDS.put("Roasted Clearhaze Snapper",     "T5_MEAL_ROAST_FISH");
        FOOD_IDS.put("Roast Pork",                    "T7_MEAL_ROAST");
        FOOD_IDS.put("Roasted Puremist Snapper",      "T7_MEAL_ROAST_FISH");
        // Sandwiches
        FOOD_IDS.put("Goat Sandwich",                 "T4_MEAL_SANDWICH");
        FOOD_IDS.put("Stonestream Lurcher Sandwich",  "T4_MEAL_SANDWICH_FISH");
        FOOD_IDS.put("Avalonian Goat Sandwich",       "T4_MEAL_SANDWICH_AVALON");
        FOOD_IDS.put("Mutton Sandwich",               "T6_MEAL_SANDWICH");
        FOOD_IDS.put("Rushwater Lurcher Sandwich",    "T6_MEAL_SANDWICH_FISH");
        FOOD_IDS.put("Avalonian Mutton Sandwich",     "T6_MEAL_SANDWICH_AVALON");
        FOOD_IDS.put("Beef Sandwich",                 "T8_MEAL_SANDWICH");
        FOOD_IDS.put("Thunderfall Lurcher Sandwich",  "T8_MEAL_SANDWICH_FISH");
        FOOD_IDS.put("Avalonian Beef Sandwich",       "T8_MEAL_SANDWICH_AVALON");
        // Fish & Other
        FOOD_IDS.put("Grilled Fish",                  "T1_MEAL_FISH");
        FOOD_IDS.put("Seaweed Salad",                 "T1_MEAL_SEAWEED");
        FOOD_IDS.put("Basic Fish Sauce",              "T1_FISHSAUCE_LEVEL1");
        FOOD_IDS.put("Fancy Fish Sauce",              "T1_FISHSAUCE_LEVEL2");
        FOOD_IDS.put("Special Fish Sauce",            "T1_FISHSAUCE_LEVEL3");
        FOOD_IDS.put("Bread",                         "T4_BREAD");
        FOOD_IDS.put("Flour",                         "T3_FLOUR");
    }

    // ── Potion icon IDs ───────────────────────────────────────────────────────
    private static final Map<String, String> POTION_IDS = new HashMap<>();
    static {
        // Uses localized "Elder's/Grandmaster's/etc." names where unique IDs are uncertain.
        // For items where internal ID is confirmed from ao-bin-dumps, uses ID directly.
        POTION_IDS.put("Minor Healing Potion",          "T2_POTION_HEAL");
        POTION_IDS.put("Healing Potion",                "T4_POTION_HEAL");
        POTION_IDS.put("Major Healing Potion",          "T6_POTION_HEAL");
        POTION_IDS.put("Minor Energy Potion",           "T2_POTION_ENERGY");
        POTION_IDS.put("Energy Potion",                 "T4_POTION_ENERGY");
        POTION_IDS.put("Major Energy Potion",           "T6_POTION_ENERGY");
        POTION_IDS.put("Minor Gigantify Potion",        "T3_POTION_GIGANTIFY");
        POTION_IDS.put("Gigantify Potion",              "T5_POTION_GIGANTIFY");
        POTION_IDS.put("Major Gigantify Potion",        "T7_POTION_GIGANTIFY");
        POTION_IDS.put("Minor Resistance Potion",       "T3_POTION_RESISTANCE");
        POTION_IDS.put("Resistance Potion",             "T5_POTION_RESISTANCE");
        POTION_IDS.put("Major Resistance Potion",       "T7_POTION_RESISTANCE");
        POTION_IDS.put("Minor Sticky Potion",           "T3_POTION_STICKY");
        POTION_IDS.put("Sticky Potion",                 "T5_POTION_STICKY");
        POTION_IDS.put("Major Sticky Potion",           "T7_POTION_STICKY");
        POTION_IDS.put("Minor Poison Potion",           "T4_POTION_POISON");
        POTION_IDS.put("Poison Potion",                 "T6_POTION_POISON");
        POTION_IDS.put("Major Poison Potion",           "T8_POTION_POISON");
        // Invisibility — single tier, confirmed internal ID from ao-bin-dumps
        POTION_IDS.put("Invisibility Potion",           "T8_POTION_INVISIBILITY");
        // Cleansing — CLEANSINGSHIELD is the confirmed internal suffix
        POTION_IDS.put("Minor Cleansing Potion",        "T3_POTION_CLEANSINGSHIELD");
        POTION_IDS.put("Cleansing Potion",              "T5_POTION_CLEANSINGSHIELD");
        POTION_IDS.put("Major Cleansing Potion",        "T7_POTION_CLEANSINGSHIELD");
        // Calming — MOB_RESET is the confirmed internal suffix
        POTION_IDS.put("Minor Calming Potion",          "T3_POTION_MOB_RESET");
        POTION_IDS.put("Calming Potion",                "T5_POTION_MOB_RESET");
        POTION_IDS.put("Major Calming Potion",          "T8_POTION_MOB_RESET");
        // Acid
        POTION_IDS.put("Minor Acid Potion",             "T3_POTION_ACID");
        POTION_IDS.put("Acid Potion",                   "T5_POTION_ACID");
        POTION_IDS.put("Major Acid Potion",             "T8_POTION_ACID");
        // Berserk
        POTION_IDS.put("Minor Berserk Potion",          "T4_POTION_BERSERK");
        POTION_IDS.put("Berserk Potion",                "T6_POTION_BERSERK");
        POTION_IDS.put("Major Berserk Potion",          "T8_POTION_BERSERK");
        // Hellfire
        POTION_IDS.put("Minor Hellfire Potion",         "T4_POTION_HELLFIRE");
        POTION_IDS.put("Hellfire Potion",               "T6_POTION_HELLFIRE");
        POTION_IDS.put("Major Hellfire Potion",         "T8_POTION_HELLFIRE");
        // Tornado in a Bottle
        POTION_IDS.put("Minor Tornado in a Bottle",     "T4_POTION_TORNADO");
        POTION_IDS.put("Tornado in a Bottle",           "T6_POTION_TORNADO");
        POTION_IDS.put("Major Tornado in a Bottle",     "T8_POTION_TORNADO");
        // Gathering
        POTION_IDS.put("Minor Gathering Potion",        "T3_POTION_GATHERING");
        POTION_IDS.put("Gathering Potion",              "T5_POTION_GATHERING");
        POTION_IDS.put("Major Gathering Potion",        "T7_POTION_GATHERING");
        // Intermediate ingredients
        POTION_IDS.put("Potato Schnapps",               "T6_POTION_SCHNAPPS");
        POTION_IDS.put("Corn Hooch",                    "T7_POTION_HOOCH");
        POTION_IDS.put("Pumpkin Moonshine",             "T8_POTION_MOONSHINE");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** T8 gear icon URL using Elder's prefix */
    public static String getT8ImageUrl(String itemDisplayName) {
        if (itemDisplayName == null || itemDisplayName.isBlank()) return null;
        String localized = T8_PREFIX + itemDisplayName;
        return BASE_URL + URLEncoder.encode(localized, StandardCharsets.UTF_8)
                .replace("+", "%20") + ".png";
    }

    /** Icon URL by exact internal unique ID */
    public static String getUrlByUniqueId(String uniqueId) {
        if (uniqueId == null || uniqueId.isBlank()) return null;
        return BASE_URL + uniqueId + ".png";
    }

    /** Food item icon URL — uses tier-specific internal IDs */
    public static String getFoodImageUrl(String itemName) {
        String id = FOOD_IDS.get(itemName);
        return id != null ? getUrlByUniqueId(id) : null;
    }

    /** Potion item icon URL — tries internal ID first, falls back to localized name */
    public static String getPotionImageUrl(String itemName) {
        String id = POTION_IDS.get(itemName);
        if (id != null) return getUrlByUniqueId(id);
        // Fallback: use the localized display name directly (render API accepts it)
        return BASE_URL + URLEncoder.encode(itemName, StandardCharsets.UTF_8)
                .replace("+", "%20") + ".png";
    }
}