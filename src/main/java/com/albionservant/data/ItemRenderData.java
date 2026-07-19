package com.albionservant.data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Constructs Albion Online render API URLs for item icons.
 *
 * Render API:
 * https://render.albiononline.com/v1/item/{identifier}.png
 *
 * The render service accepts item UniqueName IDs. It also accepts localized item
 * names, but some T8 items have special display names, so UniqueName IDs are
 * more reliable for those.
 */
public class ItemRenderData {

    private static final String BASE_URL = "https://render.albiononline.com/v1/item/";
    private static final String T8_PREFIX = "Elder's ";

    private static final Map<String, String> FOOD_IDS = new HashMap<>();
    private static final Map<String, String> POTION_IDS = new HashMap<>();
    private static final Map<String, String> T8_GEAR_IDS = new HashMap<>();

    static {
        // ?? T8 gear IDs with problematic localized names ???????????????????
        putGear("Greataxe", "T8_2H_AXE");
        putGear("Great Axe", "T8_2H_AXE");
        putGear("The Hand of Khor", "T8_2H_AXE");

        putGear("Great Fire Staff", "T8_2H_FIRESTAFF");
        putGear("Great Firestaff", "T8_2H_FIRESTAFF");
        putGear("Great Fire", "T8_2H_FIRESTAFF");
        putGear("GFire", "T8_2H_FIRESTAFF");
        putGear("Vendetta's Wrath", "T8_2H_FIRESTAFF");

        putGear("Tome of Spells", "T8_OFF_BOOK");
        putGear("Tome", "T8_OFF_BOOK");
        putGear("Rosalia's Diary", "T8_OFF_BOOK");

        // ?? A few common T8 IDs, useful because UniqueName is always stable ?
        putGear("Battleaxe", "T8_MAIN_AXE");
        putGear("Halberd", "T8_2H_HALBERD");
        putGear("Fire Staff", "T8_MAIN_FIRESTAFF");
        putGear("Infernal Staff", "T8_2H_INFERNOSTAFF");

        // ?? Food icon IDs ??????????????????????????????????????????????????
        putFood("Carrot Soup", "T1_MEAL_SOUP");
        putFood("Greenmoor Clam Soup", "T1_MEAL_SOUP_FISH");
        putFood("Wheat Soup", "T3_MEAL_SOUP");
        putFood("Murkwater Clam Soup", "T3_MEAL_SOUP_FISH");
        putFood("Cabbage Soup", "T5_MEAL_SOUP");
        putFood("Blackbog Clam Soup", "T5_MEAL_SOUP_FISH");

        putFood("Bean Salad", "T2_MEAL_SALAD");
        putFood("Shallowshore Squid Salad", "T2_MEAL_SALAD_FISH");
        putFood("Turnip Salad", "T4_MEAL_SALAD");
        putFood("Midwater Octopus Salad", "T4_MEAL_SALAD_FISH");
        putFood("Potato Salad", "T6_MEAL_SALAD");
        putFood("Deepwater Kraken Salad", "T6_MEAL_SALAD_FISH");

        putFood("Chicken Omelette", "T3_MEAL_OMELETTE");
        putFood("Lowriver Crab Omelette", "T3_MEAL_OMELETTE_FISH");
        putFood("Avalonian Chicken Omelette", "T3_MEAL_OMELETTE_AVALON");
        putFood("Goose Omelette", "T5_MEAL_OMELETTE");
        putFood("Drybrook Crab Omelette", "T5_MEAL_OMELETTE_FISH");
        putFood("Avalonian Goose Omelette", "T5_MEAL_OMELETTE_AVALON");
        putFood("Pork Omelette", "T7_MEAL_OMELETTE");
        putFood("Dusthole Crab Omelette", "T7_MEAL_OMELETTE_FISH");
        putFood("Avalonian Pork Omelette", "T7_MEAL_OMELETTE_AVALON");

        putFood("Chicken Pie", "T3_MEAL_PIE");
        putFood("Upland Coldeye Pie", "T3_MEAL_PIE_FISH");
        putFood("Goose Pie", "T5_MEAL_PIE");
        putFood("Mountain Blindeye Pie", "T5_MEAL_PIE_FISH");
        putFood("Pork Pie", "T7_MEAL_PIE");
        putFood("Frostpeak Deadeye Pie", "T7_MEAL_PIE_FISH");

        putFood("Goat Stew", "T4_MEAL_STEW");
        putFood("Greenriver Eel Stew", "T4_MEAL_STEW_FISH");
        putFood("Avalonian Goat Stew", "T4_MEAL_STEW_AVALON");
        putFood("Mutton Stew", "T6_MEAL_STEW");
        putFood("Redspring Eel Stew", "T6_MEAL_STEW_FISH");
        putFood("Avalonian Mutton Stew", "T6_MEAL_STEW_AVALON");
        putFood("Beef Stew", "T8_MEAL_STEW");
        putFood("Deadwater Eel Stew", "T8_MEAL_STEW_FISH");
        putFood("Avalonian Beef Stew", "T8_MEAL_STEW_AVALON");

        putFood("Roast Chicken", "T3_MEAL_ROAST");
        putFood("Roasted Whitefog Snapper", "T3_MEAL_ROAST_FISH");
        putFood("Roast Goose", "T5_MEAL_ROAST");
        putFood("Roasted Clearhaze Snapper", "T5_MEAL_ROAST_FISH");
        putFood("Roast Pork", "T7_MEAL_ROAST");
        putFood("Roasted Puremist Snapper", "T7_MEAL_ROAST_FISH");

        putFood("Goat Sandwich", "T4_MEAL_SANDWICH");
        putFood("Stonestream Lurcher Sandwich", "T4_MEAL_SANDWICH_FISH");
        putFood("Avalonian Goat Sandwich", "T4_MEAL_SANDWICH_AVALON");
        putFood("Mutton Sandwich", "T6_MEAL_SANDWICH");
        putFood("Rushwater Lurcher Sandwich", "T6_MEAL_SANDWICH_FISH");
        putFood("Avalonian Mutton Sandwich", "T6_MEAL_SANDWICH_AVALON");
        putFood("Beef Sandwich", "T8_MEAL_SANDWICH");
        putFood("Thunderfall Lurcher Sandwich", "T8_MEAL_SANDWICH_FISH");
        putFood("Avalonian Beef Sandwich", "T8_MEAL_SANDWICH_AVALON");

        putFood("Grilled Fish", "T1_MEAL_FISH");
        putFood("Seaweed Salad", "T1_MEAL_SEAWEED");
        putFood("Basic Fish Sauce", "T1_FISHSAUCE_LEVEL1");
        putFood("Fancy Fish Sauce", "T1_FISHSAUCE_LEVEL2");
        putFood("Special Fish Sauce", "T1_FISHSAUCE_LEVEL3");
        putFood("Bread", "T4_BREAD");
        putFood("Flour", "T3_FLOUR");

        // ?? Potion IDs ?????????????????????????????????????????????????????
        // Only IDs that are known/stable are hardcoded.
        // Everything else falls back to localized display name.
        putPotion("Minor Healing Potion", "T2_POTION_HEAL");
        putPotion("Healing Potion", "T4_POTION_HEAL");
        putPotion("Major Healing Potion", "T6_POTION_HEAL");

        putPotion("Minor Energy Potion", "T2_POTION_ENERGY");
        putPotion("Energy Potion", "T4_POTION_ENERGY");
        putPotion("Major Energy Potion", "T6_POTION_ENERGY");

        putPotion("Invisibility Potion", "T8_POTION_INVISIBILITY");

        // Corrected IDs.
        putPotion("Major Poison Potion", "T8_POTION_COOLDOWN");
        putPotion("Major Hellfire Potion", "T8_POTION_LAVA");
        putPotion("Major Berserk Potion", "T8_POTION_BERSERK");
        putPotion("Major Tornado in a Bottle", "T8_POTION_TORNADO");

        // Intermediate alchemy ingredients.
        putPotion("Potato Schnapps", "T6_POTION_SCHNAPPS");
        putPotion("Corn Hooch", "T7_POTION_HOOCH");
        putPotion("Pumpkin Moonshine", "T8_POTION_MOONSHINE");
    }

    /**
     * T8 gear icon URL.
     *
     * First tries stable UniqueName mapping.
     * Falls back to "Elder's <display name>" for ordinary T8 items.
     */
    public static String getT8ImageUrl(String itemDisplayName) {
        if (itemDisplayName == null || itemDisplayName.isBlank()) {
            return null;
        }

        String cleaned = cleanDisplayName(itemDisplayName);

        if (looksLikeUniqueId(cleaned)) {
            return getUrlByUniqueId(cleaned);
        }

        String mapped = T8_GEAR_IDS.get(key(cleaned));

        if (mapped != null) {
            return getUrlByUniqueId(mapped);
        }

        return getUrlByLocalizedName(T8_PREFIX + cleaned);
    }

    /**
     * Icon URL by exact internal unique ID.
     */
    public static String getUrlByUniqueId(String uniqueId) {
        if (uniqueId == null || uniqueId.isBlank()) {
            return null;
        }

        return BASE_URL + encode(uniqueId) + ".png";
    }

    /**
     * Food item icon URL.
     */
    public static String getFoodImageUrl(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return null;
        }

        String id = FOOD_IDS.get(key(itemName));

        if (id != null) {
            return getUrlByUniqueId(id);
        }

        return getUrlByLocalizedName(itemName);
    }

    /**
     * Potion item icon URL.
     *
     * Uses corrected UniqueName IDs for known problematic potions.
     * Falls back to localized name for everything else.
     */
    public static String getPotionImageUrl(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return null;
        }

        String cleaned = cleanDisplayName(itemName);

        if (looksLikeUniqueId(cleaned)) {
            return getUrlByUniqueId(cleaned);
        }

        String id = POTION_IDS.get(key(cleaned));

        if (id != null) {
            return getUrlByUniqueId(id);
        }

        return getUrlByLocalizedName(cleaned);
    }

    /**
     * Generic localized-name render URL.
     */
    public static String getUrlByLocalizedName(String localizedName) {
        if (localizedName == null || localizedName.isBlank()) {
            return null;
        }

        return BASE_URL + encode(localizedName.trim()) + ".png";
    }

    private static void putFood(String displayName, String uniqueId) {
        FOOD_IDS.put(key(displayName), uniqueId);
    }

    private static void putPotion(String displayName, String uniqueId) {
        POTION_IDS.put(key(displayName), uniqueId);
    }

    private static void putGear(String displayName, String uniqueId) {
        T8_GEAR_IDS.put(key(displayName), uniqueId);
    }

    private static String key(String value) {
        return cleanDisplayName(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String cleanDisplayName(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value.trim();

        cleaned = cleaned.replaceAll("(?i)^T8\\s+", "");
        cleaned = cleaned.replaceAll("(?i)^Elder's\\s+", "");
        cleaned = cleaned.replaceAll("\\s+", " ");

        return cleaned.trim();
    }

    private static boolean looksLikeUniqueId(String value) {
        return value != null && value.matches("T\\d+_.+");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}
