package com.albionservant.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Item nutrition values generated from ao-data/ao-bin-dumps.
 *
 * Station charge:
 * nutritionCost * feePer100 / 100 * craftedQuantity
 */
public final class CraftingNutritionData {

    private static final String NUTRITION_RESOURCE =
            "/data/crafting-nutrition.properties";
    private static final String NAME_IDS_RESOURCE =
            "/data/crafting-item-name-ids.tsv";

    private static final Properties NUTRITION = loadNutrition();
    private static final Map<String, String> ITEM_IDS_BY_NAME =
            loadItemIdsByName();

    private CraftingNutritionData() {
    }

    public static double nutritionCost(String exactItemId) {
        if (exactItemId == null || exactItemId.isBlank()) {
            return Double.NaN;
        }

        String raw = NUTRITION.getProperty(exactItemId.trim());

        if (raw == null || raw.isBlank()) {
            return Double.NaN;
        }

        try {
            double value = Double.parseDouble(raw);
            return value >= 0.0 ? value : Double.NaN;
        } catch (NumberFormatException ignored) {
            return Double.NaN;
        }
    }

    public static double nutritionCostForEnchant(
            String baseItemId,
            int enchantment
    ) {
        if (baseItemId == null || baseItemId.isBlank()) {
            return Double.NaN;
        }

        String base = stripEnchant(baseItemId.trim());
        int safeEnchant = Math.max(0, enchantment);

        if (safeEnchant == 0) {
            return nutritionCost(base);
        }

        List<String> candidates = List.of(
                base + "@" + safeEnchant,
                base + "_LEVEL" + safeEnchant + "@" + safeEnchant,
                base + "_LEVEL" + safeEnchant
        );

        for (String candidate : candidates) {
            double value = nutritionCost(candidate);

            if (Double.isFinite(value)) {
                return value;
            }
        }

        return Double.NaN;
    }

    public static double nutritionCostForDisplayName(
            String displayName,
            int enchantment
    ) {
        String itemId = itemIdForDisplayName(displayName);
        return nutritionCostForEnchant(itemId, enchantment);
    }

    public static String itemIdForDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return null;
        }

        return ITEM_IDS_BY_NAME.get(normalizeDisplayName(displayName));
    }

    public static double stationFee(
            String exactItemId,
            double feePer100,
            long craftedQuantity
    ) {
        double safeFee = Math.max(0.0, feePer100);
        long safeQuantity = Math.max(0L, craftedQuantity);

        if (safeFee == 0.0 || safeQuantity == 0L) {
            return 0.0;
        }

        double nutrition = nutritionCost(exactItemId);

        if (!Double.isFinite(nutrition)) {
            return Double.NaN;
        }

        return nutrition * safeFee / 100.0 * safeQuantity;
    }

    public static double stationFeeForEnchant(
            String baseItemId,
            int enchantment,
            double feePer100,
            long craftedQuantity
    ) {
        double safeFee = Math.max(0.0, feePer100);
        long safeQuantity = Math.max(0L, craftedQuantity);

        if (safeFee == 0.0 || safeQuantity == 0L) {
            return 0.0;
        }

        double nutrition = nutritionCostForEnchant(
                baseItemId,
                enchantment
        );

        if (!Double.isFinite(nutrition)) {
            return Double.NaN;
        }

        return nutrition * safeFee / 100.0 * safeQuantity;
    }

    public static double stationFeeForDisplayName(
            String displayName,
            int enchantment,
            double feePer100,
            long craftedQuantity
    ) {
        double safeFee = Math.max(0.0, feePer100);
        long safeQuantity = Math.max(0L, craftedQuantity);

        if (safeFee == 0.0 || safeQuantity == 0L) {
            return 0.0;
        }

        double nutrition = nutritionCostForDisplayName(
                displayName,
                enchantment
        );

        if (!Double.isFinite(nutrition)) {
            return Double.NaN;
        }

        return nutrition * safeFee / 100.0 * safeQuantity;
    }

    public static double parseNonNegative(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }

        String normalized = text
                .trim()
                .replace(" ", "")
                .replace("_", "")
                .replace(',', '.');

        try {
            return Math.max(0.0, Double.parseDouble(normalized));
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private static String stripEnchant(String itemId) {
        int at = itemId.indexOf('@');
        return at < 0 ? itemId : itemId.substring(0, at);
    }

    private static String normalizeDisplayName(String value) {
        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private static Properties loadNutrition() {
        Properties properties = new Properties();

        try (InputStream input =
                     CraftingNutritionData.class.getResourceAsStream(
                             NUTRITION_RESOURCE
                     )) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing crafting nutrition resource: "
                                + NUTRITION_RESOURCE
                );
            }

            properties.load(input);
            return properties;

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load crafting nutrition data",
                    exception
            );
        }
    }

    private static Map<String, String> loadItemIdsByName() {
        Map<String, String> result = new HashMap<>();

        try (InputStream input =
                     CraftingNutritionData.class.getResourceAsStream(
                             NAME_IDS_RESOURCE
                     )) {

            if (input == null) {
                throw new IllegalStateException(
                        "Missing crafting item name map: "
                                + NAME_IDS_RESOURCE
                );
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }

                    int separator = line.indexOf('\t');

                    if (separator <= 0 || separator >= line.length() - 1) {
                        continue;
                    }

                    String name = line.substring(0, separator);
                    String itemId = line.substring(separator + 1);
                    result.putIfAbsent(name, itemId);
                }
            }

            return Map.copyOf(result);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load crafting item name map",
                    exception
            );
        }
    }
}
