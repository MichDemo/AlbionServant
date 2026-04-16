package com.albionservant.data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Constructs Albion Online render API URLs for T8 item icons.
 *
 * API: https://render.albiononline.com/v1/item/{identifier}.png
 *   identifier = unique internal ID  OR  localized display name
 *
 * All T8 items in-game carry the "Elder's " prefix on their display name.
 * The render service accepts this localized name directly, so no manual
 * ID mapping is needed. Items that have a unique name (like "Kingmaker" or
 * "Infinity Blade") still use "Elder's Kingmaker" at T8.
 *
 * Usage:
 *   String url = ItemRenderData.getT8ImageUrl("Broadsword");
 *   // → https://render.albiononline.com/v1/item/Elder%27s%20Broadsword.png
 */
public class ItemRenderData {

    private static final String BASE_URL = "https://render.albiononline.com/v1/item/";
    private static final String T8_PREFIX = "Elder's ";

    /**
     * Returns the full render URL for the T8 version of the given item display name.
     * The URL is safe for use in JavaFX's Image constructor.
     *
     * @param itemDisplayName  The display name as used in the choice tree, e.g. "Broadsword"
     * @return Full HTTPS URL to the item's T8 PNG icon, URL-encoded
     */
    public static String getT8ImageUrl(String itemDisplayName) {
        if (itemDisplayName == null || itemDisplayName.isBlank()) {
            return null;
        }
        String localized = T8_PREFIX + itemDisplayName;
        String encoded = URLEncoder.encode(localized, StandardCharsets.UTF_8)
                .replace("+", "%20");          // prefer %20 over + for spaces
        return BASE_URL + encoded + ".png";
    }

    /**
     * Returns the render URL for a specific unique internal ID (e.g. "T8_MAIN_SWORD").
     * Use this overload when you know the exact in-game unique name for maximum reliability.
     */
    public static String getUrlByUniqueId(String uniqueId) {
        if (uniqueId == null || uniqueId.isBlank()) return null;
        return BASE_URL + uniqueId + ".png";
    }
}