package com.albionservant.integration.market;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// ALBIONSERVANT_RESOURCE_PRICE_FALLBACK_PATCH
// ALBIONSERVANT_AODP_RESOURCE_ITEM_ID_PATCH
// ALBIONSERVANT_CITY_PORTAL_MARKET_MERGE_PATCH
// ALBIONSERVANT_PRICE_ORDER_FIRST_PATCH
// ALBIONSERVANT_REALTIME_MARKET_REFRESH_PATCH_V3_FORCED_POLL
// ALBIONSERVANT_REALTIME_NO_BLINK_PATCH
// ALBIONSERVANT_PRICE_LOOKUP_PATCH_V3_TOLERANT
// ALBIONSERVANT_SHAPESHIFTER_DUAL_SPECIAL_MATERIALS_PATCH_V1
// ALBIONSERVANT_SHAPESHIFTER_STRICT_TIER_MAPPING_PATCH_V2
public final class LocalMarketPriceService {

    private static final long PRICE_SCALE = 10_000L;

    private static final ExecutorService QUERY_EXECUTOR =
            Executors.newFixedThreadPool(2, runnable -> {
                Thread thread = new Thread(
                        runnable,
                        "albionservant-local-market-price"
                );
                thread.setDaemon(true);
                return thread;
            });


    // ALBIONSERVANT_REALTIME_MARKET_REFRESH_PATCH
    // ALBIONSERVANT_REALTIME_MARKET_REFRESH_PATCH_V3_FORCED_POLL
    private static final long MARKET_AUTO_REFRESH_INTERVAL_MILLIS = 2_000L;

    private static final ConcurrentLinkedQueue<Runnable>
            MARKET_REFRESH_CALLBACKS = new ConcurrentLinkedQueue<>();

    private static final AtomicBoolean MARKET_REFRESH_WATCHER_STARTED =
            new AtomicBoolean(false);

    private static final ScheduledExecutorService MARKET_REFRESH_WATCHER =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(
                        runnable,
                        "albionservant-market-refresh-watcher"
                );
                thread.setDaemon(true);
                return thread;
            });

    private static final Map<String, String> MARKET_LOCATION_IDS = Map.ofEntries(
            Map.entry("THETFORD", "0007"),
            Map.entry("LYMHURST", "1002"),
            Map.entry("BRIDGEWATCH", "2004"),
            Map.entry("CAERLEON", "3005"),
            Map.entry("BLACK MARKET", "3005"),
            Map.entry("BLACKMARKET", "3005"),
            Map.entry("MARTLOCK", "3008"),
            Map.entry("FORT STERLING", "4002"),
            Map.entry("BRECILIEN", "5003")
    );

    private static final String DEFAULT_SERVER = "EUROPE";
    private static final String ITEM_ALBION_IDS_RESOURCE =
            "/data/item-albion-ids.tsv";
    private static final String DISPLAY_ITEM_NAME_IDS_RESOURCE =
            "/data/item-display-name-ids.tsv";
    private static final String CRAFTING_ITEM_NAME_IDS_RESOURCE =
            "/data/crafting-item-name-ids.tsv";
    private static final String CRAFTING_EXTRA_REQUIREMENTS_RESOURCE =
            "/data/crafting-extra-requirements.tsv";
    private static final String CRAFTING_ADDITIONAL_REQUIREMENTS_RESOURCE =
            "/data/crafting-additional-requirements.tsv";

    private static volatile Map<String, Long> itemAlbionIds;
    private static volatile Map<String, String> craftingItemNameIds;
    private static volatile Map<String, Map<Integer, CraftingExtraRequirement>> craftingExtraRequirements;
    private static volatile Map<String, Map<Integer, CraftingExtraRequirement>> craftingAdditionalRequirements;

    private static final DecimalFormat SILVER_FORMAT;

    static {
        DecimalFormatSymbols symbols =
                DecimalFormatSymbols.getInstance(Locale.US);

        SILVER_FORMAT = new DecimalFormat("#,##0.##", symbols);
        SILVER_FORMAT.setGroupingUsed(true);
    }

    private LocalMarketPriceService() {
    }

    /**
     * Normal-quality lookup. This is used for resources and refined materials.
     */
    public static void bindMinSell(
            TextField apiField,
            ComboBox<String> cityBox,
            String itemId
    ) {
        bindMinSellInternal(
                apiField,
                cityBox,
                null,
                itemId,
                true
        );
    }

    /**
     * Quality-aware lookup used by the gear crafting output price.
     */
    public static void bindMinSell(
            TextField apiField,
            ComboBox<String> cityBox,
            ComboBox<String> qualityBox,
            String itemId
    ) {
        bindMinSellInternal(
                apiField,
                cityBox,
                qualityBox,
                itemId,
                false
        );
    }

    /**
     * Binds a read-only demand field to local SQLite market_history rows.
     * Used for items without quality, such as food and potions.
     */
    public static void bindDemand(
            TextField demandField,
            ComboBox<String> cityBox,
            ComboBox<String> demandTypeBox,
            String itemId
    ) {
        bindDemand(
                demandField,
                cityBox,
                null,
                demandTypeBox,
                itemId
        );
    }

    /**
     * Binds a read-only demand field to local SQLite market_history rows.
     * Demand type maps to AODP Timescale: 24h=0, 7d=1, 4w=2.
     */
    /**
     * Binds a read-only demand field to local SQLite market_history rows.
     * Demand type maps to AODP Timescale: 24h=0, 7d=1, 4w=2.
     */
    public static void bindDemand(
            TextField demandField,
            ComboBox<String> cityBox,
            ComboBox<String> qualityBox,
            ComboBox<String> demandTypeBox,
            String itemId
    ) {
        if (demandField == null || cityBox == null || demandTypeBox == null) {
            return;
        }

        demandField.setEditable(false);

        AtomicLong requestVersion = new AtomicLong();

        Runnable refresh = () -> {
            long version = requestVersion.incrementAndGet();
            String city = cityBox.getValue();
            int qualityLevel = qualityBox == null
                    ? 1
                    : qualityLevel(qualityBox.getValue());
            int timescale = demandTimescale(demandTypeBox.getValue());
            Long albionId = albionIdForItemId(itemId);

            if (albionId == null || isBlank(city)) {
                setFieldTextIfChanged(demandField, "\u2014");
                setFieldTooltipIfChanged(
                        demandField,
                        albionId == null
                                ? "No Albion numeric ID mapping for " + itemId
                                : "Select a city to load demand"
                );
                return;
            }

            /*
             * No-blink refresh: do not overwrite a stable visible value with
             * "..." during the periodic v3 poll. Show the loading marker only
             * before the first value is displayed.
             */
            if (isBlank(demandField.getText())) {
                setFieldTextIfChanged(demandField, "...");
            }

            QUERY_EXECUTOR.submit(() -> {
                DemandQueryResult demand = null;
                String error = null;

                try {
                    demand = findDemandAmount(
                            albionId,
                            city,
                            qualityLevel,
                            timescale
                    );
                } catch (SQLException exception) {
                    error = exception.getMessage();
                }

                DemandQueryResult finalDemand = demand;
                String finalError = error;

                Platform.runLater(() -> {
                    if (requestVersion.get() != version) {
                        return;
                    }

                    if (finalDemand == null) {
                        setFieldTextIfChanged(demandField, "\u2014");
                        setFieldTooltipIfChanged(
                                demandField,
                                isBlank(finalError)
                                        ? "No local market_history demand for "
                                        + itemId
                                        + " in "
                                        + city
                                        + " ("
                                        + demandTypeLabel(timescale)
                                        + ")"
                                        : "SQLite error: " + finalError
                        );
                        return;
                    }

                    setFieldTextIfChanged(
                            demandField,
                            formatLong(finalDemand.itemAmount())
                    );
                    setFieldTooltipIfChanged(
                            demandField,
                            itemId
                                    + " | AlbionId="
                                    + albionId
                                    + " | "
                                    + city
                                    + " | Q"
                                    + qualityLevel
                                    + " | "
                                    + demandTypeLabel(timescale)
                                    + " | rows="
                                    + finalDemand.rowCount()
                                    + " | observed="
                                    + formatObservedAt(
                                    finalDemand.latestObservedAt()
                            )
                    );
                });
            });
        };

        cityBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> refresh.run()
        );

        if (qualityBox != null) {
            qualityBox.valueProperty().addListener(
                    (observable, oldValue, newValue) -> refresh.run()
            );
        }

        demandTypeBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> refresh.run()
        );

        registerMarketAutoRefresh(refresh);
        refresh.run();
    }

    public static String foodItemId(String displayName, int enchantment) {
        return displayNameItemId(displayName, enchantment);
    }

    public static String potionItemId(String displayName, int enchantment) {
        return displayNameItemId(displayName, enchantment);
    }

    private static void bindMinSellInternal(
            TextField apiField,
            ComboBox<String> cityBox,
            ComboBox<String> qualityBox,
            String itemId,
            boolean resourceLookup
    ) {
        if (apiField == null || cityBox == null) {
            return;
        }

        AtomicLong requestVersion = new AtomicLong();

        Runnable refresh = () -> {
            long version = requestVersion.incrementAndGet();
            String city = cityBox.getValue();
            int qualityLevel = qualityBox == null
                    ? 1
                    : qualityLevel(qualityBox.getValue());

            if (isBlank(itemId) || isBlank(city)) {
                setFieldTextIfChanged(apiField, "\u2014");
                setFieldTooltipIfChanged(apiField, null);
                return;
            }

            /*
             * No-blink refresh: do not replace the current value with "..."
             * on every background poll. This keeps realtime updates active but
             * avoids visible flicker when the price did not change.
             */
            if (isBlank(apiField.getText())) {
                setFieldTextIfChanged(apiField, "...");
            }

            QUERY_EXECUTOR.submit(() -> {
                Long rawPrice = null;
                String error = null;

                try {
                    rawPrice = findMinSellRaw(
                            itemId,
                            city,
                            qualityLevel,
                            resourceLookup
                    );
                } catch (SQLException exception) {
                    error = exception.getMessage();
                }

                Long finalRawPrice = rawPrice;
                String finalError = error;

                Platform.runLater(() -> {
                    if (requestVersion.get() != version) {
                        return;
                    }

                    if (finalRawPrice == null || finalRawPrice <= 0L) {
                        setFieldTextIfChanged(apiField, "\u2014");

                        String detail = isBlank(finalError)
                                ? "No local Q"
                                + qualityLevel
                                + " price for "
                                + itemId
                                + " in "
                                + city
                                : "SQLite error: " + finalError;

                        setFieldTooltipIfChanged(apiField, detail);
                        return;
                    }

                    setFieldTextIfChanged(apiField, formatRawPrice(finalRawPrice));
                    setFieldTooltipIfChanged(
                            apiField,
                            itemId
                                    + " | "
                                    + city
                                    + " | Q"
                                    + qualityLevel
                                    + " | raw="
                                    + finalRawPrice
                    );
                });
            });
        };

        cityBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> refresh.run()
        );

        if (qualityBox != null) {
            qualityBox.valueProperty().addListener(
                    (observable, oldValue, newValue) -> refresh.run()
            );
        }

        registerMarketAutoRefresh(refresh);
        refresh.run();
    }

    // ALBIONSERVANT_REALTIME_NO_BLINK_PATCH
    private static void setFieldTextIfChanged(TextField field, String text) {
        if (field == null) {
            return;
        }

        String newText = text == null ? "" : text;
        String oldText = field.getText();

        if (!newText.equals(oldText)) {
            field.setText(newText);
        }
    }

    private static void setFieldTooltipIfChanged(TextField field, String text) {
        if (field == null) {
            return;
        }

        String newText = isBlank(text) ? null : text;
        Tooltip oldTooltip = field.getTooltip();
        String oldText = oldTooltip == null ? null : oldTooltip.getText();

        if (newText == null) {
            if (oldTooltip != null) {
                field.setTooltip(null);
            }
            return;
        }

        if (!newText.equals(oldText)) {
            field.setTooltip(new Tooltip(newText));
        }
    }

    private static void registerMarketAutoRefresh(Runnable refresh) {
        if (refresh == null) {
            return;
        }

        /*
         * Keep a strong reference. The v2 patch used WeakReference<Runnable>;
         * lambdas created inside bindMinSellInternal/bindDemand can be garbage
         * collected after initial binding, so the watcher may silently lose all
         * visible UI fields. A small strong list is safer for this desktop app.
         */
        MARKET_REFRESH_CALLBACKS.add(refresh);
        startMarketRefreshWatcher();
    }

    private static void startMarketRefreshWatcher() {
        if (!MARKET_REFRESH_WATCHER_STARTED.compareAndSet(false, true)) {
            return;
        }

        /*
         * Polling is intentionally unconditional. SQLite WAL updates and some
         * antivirus/cloud-sync setups do not always change timestamps in a way
         * the previous file-signature watcher could see. The registered fields
         * re-query SQLite and update only with the newest result.
         */
        MARKET_REFRESH_WATCHER.scheduleWithFixedDelay(
                () -> {
                    try {
                        Platform.runLater(
                                LocalMarketPriceService::refreshRegisteredMarketBindings
                        );
                    } catch (IllegalStateException ignored) {
                        // JavaFX runtime is not available yet or is shutting down.
                    }
                },
                MARKET_AUTO_REFRESH_INTERVAL_MILLIS,
                MARKET_AUTO_REFRESH_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
        );
    }

    private static void refreshIfDatabaseChanged() {
        try {
            Platform.runLater(LocalMarketPriceService::refreshRegisteredMarketBindings);
        } catch (IllegalStateException ignored) {
            // JavaFX runtime is not available yet or is shutting down.
        }
    }

    private static void refreshRegisteredMarketBindings() {
        for (Runnable refresh : MARKET_REFRESH_CALLBACKS) {
            try {
                refresh.run();
            } catch (RuntimeException ignored) {
                // Keep the watcher alive even if one stale UI binding fails.
            }
        }
    }

    // ALBIONSERVANT_REALTIME_MARKET_REFRESH_PATCH_V2_WAL
    private static long databaseSignature() {
        Path database = databasePath();
        long signature = 17L;
        boolean found = false;

        for (Path candidate : List.of(
                database,
                Paths.get(database.toString() + "-wal"),
                Paths.get(database.toString() + "-shm")
        )) {
            try {
                if (!Files.exists(candidate)) {
                    continue;
                }

                found = true;
                long modifiedAt = Files.getLastModifiedTime(candidate).toMillis();
                long size = Files.size(candidate);

                signature = signature * 31L
                        + candidate.getFileName().toString().hashCode();
                signature = signature * 31L + modifiedAt;
                signature = signature * 31L + size;
            } catch (IOException ignored) {
                // A file can disappear while SQLite checkpoints WAL. Ignore it.
            }
        }

        return found ? signature : 0L;
    }

    public static int qualityLevel(String selectedQuality) {
        if (isBlank(selectedQuality)) {
            return 1;
        }

        String normalized = selectedQuality
                .trim()
                .toUpperCase(Locale.ROOT);

        if (normalized.contains("Q5")
                || normalized.contains("MASTERPIECE")) {
            return 5;
        }

        if (normalized.contains("Q4")
                || normalized.contains("EXCELLENT")) {
            return 4;
        }

        if (normalized.contains("Q3")
                || normalized.contains("OUTSTANDING")) {
            return 3;
        }

        if (normalized.contains("Q2")
                || normalized.contains("GOOD")) {
            return 2;
        }

        return 1;
    }

    public static String gearItemId(
            String itemName,
            int tier,
            int enchantment
    ) {
        return GearMarketItemData.itemId(
                itemName,
                tier,
                enchantment
        );
    }

    public static String withEnchant(String baseItemId, int enchantment) {
        if (isBlank(baseItemId)) {
            return baseItemId;
        }

        String cleanBase = baseItemId.trim();

        int suffixIndex = cleanBase.indexOf('@');
        if (suffixIndex >= 0) {
            cleanBase = cleanBase.substring(0, suffixIndex);
        }

        /*
         * AODP market IDs for enchanted resources and refined materials use:
         * T5_METALBAR_LEVEL1@1
         * T5_ORE_LEVEL1@1
         * rather than T5_METALBAR@1 / T5_ORE@1.
         */
        cleanBase = cleanBase.replaceFirst("_LEVEL[1-4]$", "");

        if (enchantment <= 0) {
            return cleanBase;
        }

        int normalizedEnchantment = Math.max(1, Math.min(4, enchantment));

        return cleanBase
                + "_LEVEL"
                + normalizedEnchantment
                + "@"
                + normalizedEnchantment;
    }

    public static String refinedMaterialItemId(
            String materialName,
            int tier,
            int enchantment
    ) {
        if (isBlank(materialName)) {
            return null;
        }

        String suffix = switch (materialName.trim().toLowerCase(Locale.ROOT)) {
            case "metal bars", "metal bar" -> "METALBAR";
            case "planks", "plank" -> "PLANKS";
            case "leather" -> "LEATHER";
            case "cloth" -> "CLOTH";
            case "stone blocks", "stone block" -> "STONEBLOCK";
            default -> null;
        };

        if (suffix == null) {
            return null;
        }

        return withEnchant("T" + tier + "_" + suffix, enchantment);
    }

    // ALBIONSERVANT_EXACT_ARTIFACT_NAMES_PATCH
    public record CraftingExtraRequirement(
            String itemId,
            String displayName,
            int count,
            boolean returnable
    ) {}

    public static String artifactColumnDisplayName(
            String itemName,
            String fallbackDisplayName
    ) {
        CraftingExtraRequirement requirement = firstCraftingExtraRequirement(
                itemName
        );

        if (requirement != null && !isBlank(requirement.displayName())) {
            return stripTierPrefix(requirement.displayName());
        }

        return isBlank(fallbackDisplayName)
                ? "Artifact"
                : fallbackDisplayName;
    }

    public static String artifactDisplayName(
            String itemName,
            int tier
    ) {
        CraftingExtraRequirement requirement = craftingExtraRequirement(
                itemName,
                tier
        );

        return requirement == null ? null : requirement.displayName();
    }

    public static String artifactItemId(
            String itemName,
            String artifactTypeName,
            int tier
    ) {
        CraftingExtraRequirement requirement = craftingExtraRequirement(
                itemName,
                tier
        );

        if (requirement != null && !isBlank(requirement.itemId())) {
            return requirement.itemId();
        }

        if ("AVALON_ENERGY".equalsIgnoreCase(artifactTypeName)) {
            return "QUESTITEM_TOKEN_AVALON";
        }

        return null;
    }

    public static String artifactItemId(
            String artifactTypeName,
            int tier
    ) {
        return artifactItemId(null, artifactTypeName, tier);
    }

    public static int artifactQuantity(
            String itemName,
            int tier
    ) {
        CraftingExtraRequirement requirement = craftingExtraRequirement(
                itemName,
                tier
        );

        return requirement == null ? 1 : Math.max(1, requirement.count());
    }

    public static boolean artifactReceivesReturns(
            String itemName,
            String artifactTypeName,
            int tier
    ) {
        CraftingExtraRequirement requirement = craftingExtraRequirement(
                itemName,
                tier
        );

        if (requirement != null) {
            return requirement.returnable();
        }

        return !"AVALON_ENERGY".equalsIgnoreCase(artifactTypeName);
    }


    /**
     * Returns true when a recipe contains a special non-refined component in
     * addition to its normal materials/artifact. Shapeshifter alchemy pieces
     * are the first supported use case.
     */
    public static boolean hasAdditionalCraftingRequirement(String itemName) {
        return firstAdditionalCraftingRequirement(itemName) != null;
    }

    public static String additionalRequirementColumnDisplayName(
            String itemName
    ) {
        CraftingExtraRequirement requirement =
                firstAdditionalCraftingRequirement(itemName);

        if (requirement == null || isBlank(requirement.displayName())) {
            return "Additional Material";
        }

        String baseName = stripTierPrefix(requirement.displayName())
                .replaceFirst(
                        "(?i)^(Rugged|Fine|Excellent)\\s+",
                        ""
                );
        String tierPattern = additionalRequirementTierPattern(itemName);

        return isBlank(tierPattern)
                ? baseName
                : baseName + " [" + tierPattern + "]";
    }

    public static String additionalRequirementDisplayName(
            String itemName,
            int craftedTier
    ) {
        CraftingExtraRequirement requirement =
                additionalCraftingRequirement(itemName, craftedTier);
        return requirement == null ? null : requirement.displayName();
    }

    public static int additionalRequirementSourceTier(
            String itemName,
            int craftedTier
    ) {
        CraftingExtraRequirement requirement =
                additionalCraftingRequirement(itemName, craftedTier);
        return requirement == null
                ? 0
                : requirementItemTier(requirement.itemId());
    }

    public static String additionalRequirementTierPattern(String itemName) {
        if (isBlank(itemName)) {
            return "";
        }

        Map<Integer, CraftingExtraRequirement> byTier =
                craftingAdditionalRequirements().get(
                        normalizeDisplayName(itemName)
                );
        if (byTier == null || byTier.isEmpty()) {
            return "";
        }

        List<Integer> sourceTiers = new ArrayList<>();
        for (CraftingExtraRequirement requirement : byTier.values()) {
            int sourceTier = requirementItemTier(requirement.itemId());
            if (sourceTier > 0 && !sourceTiers.contains(sourceTier)) {
                sourceTiers.add(sourceTier);
            }
        }
        Collections.sort(sourceTiers);

        StringBuilder value = new StringBuilder();
        for (int index = 0; index < sourceTiers.size(); index++) {
            if (index > 0) {
                value.append('/');
            }
            value.append('T').append(sourceTiers.get(index));
        }
        return value.toString();
    }

    private static int requirementItemTier(String itemId) {
        if (isBlank(itemId) || itemId.length() < 3
                || (itemId.charAt(0) != 'T' && itemId.charAt(0) != 't')) {
            return 0;
        }

        int underscore = itemId.indexOf('_');
        if (underscore <= 1) {
            return 0;
        }

        try {
            return Integer.parseInt(itemId.substring(1, underscore));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static String additionalRequirementItemId(
            String itemName,
            int tier
    ) {
        CraftingExtraRequirement requirement =
                additionalCraftingRequirement(itemName, tier);
        return requirement == null ? null : requirement.itemId();
    }

    public static int additionalRequirementQuantity(
            String itemName,
            int tier
    ) {
        CraftingExtraRequirement requirement =
                additionalCraftingRequirement(itemName, tier);
        return requirement == null ? 0 : Math.max(1, requirement.count());
    }

    public static boolean additionalRequirementReceivesReturns(
            String itemName,
            int tier
    ) {
        CraftingExtraRequirement requirement =
                additionalCraftingRequirement(itemName, tier);
        return requirement != null && requirement.returnable();
    }

    private static CraftingExtraRequirement additionalCraftingRequirement(
            String itemName,
            int craftedTier
    ) {
        if (isBlank(itemName) || craftedTier < 4 || craftedTier > 8) {
            return null;
        }

        Map<Integer, CraftingExtraRequirement> byCraftedTier =
                craftingAdditionalRequirements().get(
                        normalizeDisplayName(itemName)
                );

        if (byCraftedTier == null || byCraftedTier.isEmpty()) {
            return null;
        }

        // Strict lookup: the map key is the CRAFTED weapon tier. The item ID
        // stored in the row carries its independent source tier (T3/T5/T7).
        // Never fall back to another crafted tier, because that can silently
        // bind the wrong market item and quantity.
        return byCraftedTier.get(craftedTier);
    }

    private static CraftingExtraRequirement firstAdditionalCraftingRequirement(
            String itemName
    ) {
        if (isBlank(itemName)) {
            return null;
        }

        Map<Integer, CraftingExtraRequirement> byTier =
                craftingAdditionalRequirements().get(
                        normalizeDisplayName(itemName)
                );

        if (byTier == null || byTier.isEmpty()) {
            return null;
        }

        CraftingExtraRequirement tier4 = byTier.get(4);
        return tier4 != null ? tier4 : byTier.values().iterator().next();
    }

    private static Map<String, Map<Integer, CraftingExtraRequirement>>
    craftingAdditionalRequirements() {
        Map<String, Map<Integer, CraftingExtraRequirement>> current =
                craftingAdditionalRequirements;

        if (current != null) {
            return current;
        }

        synchronized (LocalMarketPriceService.class) {
            if (craftingAdditionalRequirements == null) {
                craftingAdditionalRequirements =
                        loadAdditionalCraftingRequirements();
            }
            return craftingAdditionalRequirements;
        }
    }

    private static Map<String, Map<Integer, CraftingExtraRequirement>>
    loadAdditionalCraftingRequirements() {
        Map<String, Map<Integer, CraftingExtraRequirement>> values =
                new HashMap<>();

        try (InputStream input = LocalMarketPriceService.class
                .getResourceAsStream(
                        CRAFTING_ADDITIONAL_REQUIREMENTS_RESOURCE
                )) {
            if (input == null) {
                return Map.of();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    String[] parts = line.split("\\t", -1);
                    if (parts.length < 6) {
                        continue;
                    }

                    String itemName = normalizeDisplayName(parts[0]);
                    String itemId = parts[2].trim();
                    String displayName = parts[3].trim();

                    try {
                        int tier = Integer.parseInt(parts[1].trim());
                        int count = Integer.parseInt(parts[4].trim());
                        boolean returnable = Boolean.parseBoolean(
                                parts[5].trim()
                        );

                        if (!isBlank(itemName) && !isBlank(itemId)) {
                            values.computeIfAbsent(
                                    itemName,
                                    ignored -> new HashMap<>()
                            ).put(
                                    tier,
                                    new CraftingExtraRequirement(
                                            itemId,
                                            displayName,
                                            Math.max(1, count),
                                            returnable
                                    )
                            );
                        }
                    } catch (NumberFormatException ignored) {
                        // Ignore malformed generated rows.
                    }
                }
            }
        } catch (IOException ignored) {
            return Map.of();
        }

        Map<String, Map<Integer, CraftingExtraRequirement>> immutable =
                new HashMap<>();
        for (Map.Entry<String, Map<Integer, CraftingExtraRequirement>> entry
                : values.entrySet()) {
            immutable.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        return Map.copyOf(immutable);
    }

    private static CraftingExtraRequirement craftingExtraRequirement(
            String itemName,
            int craftedTier
    ) {
        if (isBlank(itemName) || craftedTier < 4 || craftedTier > 8) {
            return null;
        }

        Map<Integer, CraftingExtraRequirement> byCraftedTier =
                craftingExtraRequirements().get(normalizeDisplayName(itemName));

        if (byCraftedTier == null || byCraftedTier.isEmpty()) {
            return null;
        }

        // Artifact/remnant/head tier must match the crafted item tier exactly.
        // Do not fall back to a different tier if a generated row is missing.
        CraftingExtraRequirement exact = byCraftedTier.get(craftedTier);
        if (exact == null) {
            return null;
        }

        int sourceTier = requirementItemTier(exact.itemId());
        return sourceTier == 0 || sourceTier == craftedTier ? exact : null;
    }

    private static CraftingExtraRequirement firstCraftingExtraRequirement(
            String itemName
    ) {
        if (isBlank(itemName)) {
            return null;
        }

        Map<Integer, CraftingExtraRequirement> byTier =
                craftingExtraRequirements().get(normalizeDisplayName(itemName));

        if (byTier == null || byTier.isEmpty()) {
            return null;
        }

        CraftingExtraRequirement tier4 = byTier.get(4);
        return tier4 != null ? tier4 : byTier.values().iterator().next();
    }

    private static Map<String, Map<Integer, CraftingExtraRequirement>>
    craftingExtraRequirements() {
        Map<String, Map<Integer, CraftingExtraRequirement>> current =
                craftingExtraRequirements;

        if (current != null) {
            return current;
        }

        synchronized (LocalMarketPriceService.class) {
            if (craftingExtraRequirements == null) {
                craftingExtraRequirements = loadCraftingExtraRequirements();
            }

            return craftingExtraRequirements;
        }
    }

    private static Map<String, Map<Integer, CraftingExtraRequirement>>
    loadCraftingExtraRequirements() {
        Map<String, Map<Integer, CraftingExtraRequirement>> values =
                new HashMap<>();

        try (InputStream input = LocalMarketPriceService.class
                .getResourceAsStream(CRAFTING_EXTRA_REQUIREMENTS_RESOURCE)) {
            if (input == null) {
                return Map.of();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    String[] parts = line.split("\\t", -1);

                    if (parts.length < 6) {
                        continue;
                    }

                    String itemName = normalizeDisplayName(parts[0]);
                    String itemId = parts[2].trim();
                    String displayName = parts[3].trim();

                    try {
                        int tier = Integer.parseInt(parts[1].trim());
                        int count = Integer.parseInt(parts[4].trim());
                        boolean returnable = Boolean.parseBoolean(
                                parts[5].trim()
                        );

                        if (!isBlank(itemName) && !isBlank(itemId)) {
                            values.computeIfAbsent(
                                    itemName,
                                    ignored -> new HashMap<>()
                            ).put(
                                    tier,
                                    new CraftingExtraRequirement(
                                            itemId,
                                            displayName,
                                            Math.max(1, count),
                                            returnable
                                    )
                            );
                        }
                    } catch (NumberFormatException ignored) {
                        // Skip malformed rows but keep the rest of the map.
                    }
                }
            }
        } catch (IOException ignored) {
            return Map.of();
        }

        Map<String, Map<Integer, CraftingExtraRequirement>> immutable =
                new HashMap<>();

        for (Map.Entry<String, Map<Integer, CraftingExtraRequirement>> entry
                : values.entrySet()) {
            immutable.put(
                    entry.getKey(),
                    Collections.unmodifiableMap(new HashMap<>(entry.getValue()))
            );
        }

        return Collections.unmodifiableMap(immutable);
    }

    private static String stripTierPrefix(String displayName) {
        if (isBlank(displayName)) {
            return displayName;
        }

        return displayName.trim().replaceFirst(
                "(?i)^(Adept's|Expert's|Master's|Grandmaster's|Elder's)\\s+",
                ""
        );
    }


    public static boolean hasEffectivePrice(
            TextField apiField,
            TextField manualField
    ) {
        return parsePrice(textOf(manualField)) != null
                || parsePrice(textOf(apiField)) != null;
    }

    public static double effectiveDisplayedPrice(
            TextField apiField,
            TextField manualField
    ) {
        Double manual = parsePrice(textOf(manualField));

        if (manual != null) {
            return manual;
        }

        Double api = parsePrice(textOf(apiField));
        return api == null ? 0.0 : api;
    }

    public static String formatSilver(double value) {
        if (!Double.isFinite(value)) {
            return "\u2014";
        }

        synchronized (SILVER_FORMAT) {
            return SILVER_FORMAT.format(value);
        }
    }

    public static Path databasePath() {
        String override = firstNonBlank(
                System.getProperty("albion.local.sqlite.path"),
                System.getProperty("albion.sqlite.path"),
                System.getenv("ALBIONSERVANT_DB")
        );

        if (!isBlank(override)) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        String localAppData = System.getenv("LOCALAPPDATA");

        if (!isBlank(localAppData)) {
            return Paths.get(
                    localAppData,
                    "AlbionServant",
                    "data",
                    "albionservant.db"
            ).toAbsolutePath().normalize();
        }

        return Paths.get(
                System.getProperty("user.home"),
                ".albionservant",
                "data",
                "albionservant.db"
        ).toAbsolutePath().normalize();
    }

    private static Long findMinSellRaw(
            String itemId,
            String city,
            int qualityLevel,
            boolean resourceLookup
    ) throws SQLException {
        Path database = databasePath();

        if (!Files.isRegularFile(database)) {
            return null;
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new SQLException(
                    "Missing org.xerial sqlite-jdbc driver",
                    exception
            );
        }

        List<String> locations = locationCandidates(city);

        if (locations.isEmpty()) {
            return null;
        }

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database
        )) {
            try (PreparedStatement pragma =
                         connection.prepareStatement(
                                 "PRAGMA busy_timeout = 5000"
                         )) {
                pragma.execute();
            }

            Long orderPrice = queryRawOrderPrice(
                    connection,
                    itemId,
                    locations,
                    qualityLevel,
                    resourceLookup
            );

            if (orderPrice != null && orderPrice > 0L) {
                return orderPrice;
            }

            return queryLatestPrice(
                    connection,
                    itemId,
                    locations,
                    qualityLevel,
                    resourceLookup
            );
        }
    }

    private static DemandQueryResult findDemandAmount(
            long albionId,
            String city,
            int qualityLevel,
            int timescale
    ) throws SQLException {
        Path database = databasePath();

        if (!Files.isRegularFile(database)) {
            return null;
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new SQLException(
                    "Missing org.xerial sqlite-jdbc driver",
                    exception
            );
        }

        List<String> locations = locationCandidates(city);

        if (locations.isEmpty()) {
            return null;
        }

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database
        )) {
            try (PreparedStatement pragma =
                         connection.prepareStatement(
                                 "PRAGMA busy_timeout = 3000"
                         )) {
                pragma.execute();
            }

            DemandQueryResult result = queryDemand(
                    connection,
                    DEFAULT_SERVER,
                    albionId,
                    locations,
                    qualityLevel,
                    timescale,
                    false
            );

            if (result != null) {
                return result;
            }

            if (qualityLevel <= 1) {
                result = queryDemand(
                        connection,
                        DEFAULT_SERVER,
                        albionId,
                        locations,
                        qualityLevel,
                        timescale,
                        true
                );

                if (result != null) {
                    return result;
                }
            }

            result = queryDemand(
                    connection,
                    null,
                    albionId,
                    locations,
                    qualityLevel,
                    timescale,
                    false
            );

            if (result != null) {
                return result;
            }

            if (qualityLevel <= 1) {
                return queryDemand(
                        connection,
                        null,
                        albionId,
                        locations,
                        qualityLevel,
                        timescale,
                        true
                );
            }

            return null;
        }
    }

    private static DemandQueryResult queryDemand(
            Connection connection,
            String server,
            long albionId,
            List<String> locations,
            int qualityLevel,
            int timescale,
            boolean allowNormalQualityFallback
    ) throws SQLException {
        String placeholders = String.join(
                ", ",
                locations.stream().map(value -> "?").toList()
        );

        String serverPredicate = server == null
                ? ""
                : "h.server = ? AND ";
        String qualityPredicate = allowNormalQualityFallback
                ? "h.quality_level IN (0, 1)"
                : "h.quality_level = ?";

        String sql = "SELECT COALESCE(SUM(h.item_amount), 0), "
                + "COUNT(*), "
                + "MAX(h.observed_at) "
                + "FROM market_history h "
                + "WHERE "
                + serverPredicate
                + "h.albion_id = ? "
                + "AND h.location_id IN ("
                + placeholders
                + ") "
                + "AND "
                + qualityPredicate
                + " "
                + "AND h.timescale = ? "
                + "AND h.observed_at = ("
                + "SELECT MAX(x.observed_at) "
                + "FROM market_history x "
                + "WHERE x.server = h.server "
                + "AND x.albion_id = h.albion_id "
                + "AND x.location_id = h.location_id "
                + "AND x.quality_level = h.quality_level "
                + "AND x.timescale = h.timescale"
                + ")";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            int parameterIndex = 1;

            if (server != null) {
                statement.setString(parameterIndex++, server);
            }

            statement.setLong(parameterIndex++, albionId);

            for (String location : locations) {
                statement.setString(parameterIndex++, location);
            }

            if (!allowNormalQualityFallback) {
                statement.setInt(
                        parameterIndex++,
                        Math.max(0, Math.min(5, qualityLevel))
                );
            }

            statement.setInt(parameterIndex, Math.max(0, Math.min(2, timescale)));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                long rowCount = resultSet.getLong(2);

                if (rowCount <= 0L) {
                    return null;
                }

                long itemAmount = resultSet.getLong(1);
                long latestObservedAt = resultSet.getLong(3);

                return new DemandQueryResult(
                        itemAmount,
                        rowCount,
                        resultSet.wasNull() ? null : latestObservedAt
                );
            }
        }
    }

    private static Long queryLatestPrice(
            Connection connection,
            String itemId,
            List<String> locations,
            int qualityLevel,
            boolean resourceLookup
    ) throws SQLException {
        String placeholders = String.join(
                ", ",
                locations.stream().map(value -> "?").toList()
        );

        String qualityPredicate = resourceLookup
                ? "quality_level IN (0, 1)"
                : "quality_level = ?";

        Long strictServer = executePriceQuery(
                connection,
                "SELECT MIN(min_sell_price) "
                        + "FROM market_prices_latest "
                        + "WHERE server = ? "
                        + "AND UPPER(item_id) = UPPER(?) "
                        + "AND enchantment_level = ? "
                        + "AND " + qualityPredicate + " "
                        + "AND location_id IN (" + placeholders + ") "
                        + "AND min_sell_price IS NOT NULL "
                        + "AND min_sell_price > 0",
                DEFAULT_SERVER,
                itemId,
                locations,
                qualityLevel,
                !resourceLookup,
                enchantmentLevelFromItemId(itemId)
        );

        if (strictServer != null && strictServer > 0L) {
            return strictServer;
        }

        return executePriceQuery(
                connection,
                "SELECT MIN(min_sell_price) "
                        + "FROM market_prices_latest "
                        + "WHERE UPPER(item_id) = UPPER(?) "
                        + "AND enchantment_level = ? "
                        + "AND " + qualityPredicate + " "
                        + "AND location_id IN (" + placeholders + ") "
                        + "AND min_sell_price IS NOT NULL "
                        + "AND min_sell_price > 0",
                null,
                itemId,
                locations,
                qualityLevel,
                !resourceLookup,
                enchantmentLevelFromItemId(itemId)
        );
    }

    private static Long queryRawOrderPrice(
            Connection connection,
            String itemId,
            List<String> locations,
            int qualityLevel,
            boolean resourceLookup
    ) throws SQLException {
        String placeholders = String.join(
                ", ",
                locations.stream().map(value -> "?").toList()
        );

        String qualityPredicate = resourceLookup
                ? "quality_level IN (0, 1)"
                : "quality_level = ?";

        /*
         * Do not filter by expires here. The rows are written from the live
         * local order-book capture, and AODP/SQLite timestamp variants can make
         * valid live orders fail datetime(...) parsing. The cheapest currently
         * captured sell offer is what the crafting UI needs.
         */
        Long strictServer = executePriceQuery(
                connection,
                "SELECT MIN(unit_price_silver) "
                        + "FROM market_orders "
                        + "WHERE server = ? "
                        + "AND UPPER(item_id) = UPPER(?) "
                        + "AND enchantment_level = ? "
                        + "AND " + qualityPredicate + " "
                        + "AND location_id IN (" + placeholders + ") "
                        + "AND LOWER(auction_type) = 'offer' "
                        + "AND unit_price_silver IS NOT NULL "
                        + "AND unit_price_silver > 0",
                DEFAULT_SERVER,
                itemId,
                locations,
                qualityLevel,
                !resourceLookup,
                enchantmentLevelFromItemId(itemId)
        );

        if (strictServer != null && strictServer > 0L) {
            return strictServer;
        }

        return executePriceQuery(
                connection,
                "SELECT MIN(unit_price_silver) "
                        + "FROM market_orders "
                        + "WHERE UPPER(item_id) = UPPER(?) "
                        + "AND enchantment_level = ? "
                        + "AND " + qualityPredicate + " "
                        + "AND location_id IN (" + placeholders + ") "
                        + "AND LOWER(auction_type) = 'offer' "
                        + "AND unit_price_silver IS NOT NULL "
                        + "AND unit_price_silver > 0",
                null,
                itemId,
                locations,
                qualityLevel,
                !resourceLookup,
                enchantmentLevelFromItemId(itemId)
        );
    }

    private static Long executePriceQuery(
            Connection connection,
            String sql,
            String server,
            String itemId,
            List<String> locations,
            int qualityLevel,
            boolean bindQuality,
            int enchantmentLevel
    ) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int parameterIndex = 1;

            if (server != null) {
                statement.setString(parameterIndex++, server);
            }

            statement.setString(parameterIndex++, itemId);
            statement.setInt(
                    parameterIndex++,
                    Math.max(0, Math.min(4, enchantmentLevel))
            );

            if (bindQuality) {
                statement.setInt(
                        parameterIndex++,
                        Math.max(1, Math.min(5, qualityLevel))
                );
            }

            for (String location : locations) {
                statement.setString(parameterIndex++, location);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                long value = resultSet.getLong(1);

                if (resultSet.wasNull() || value <= 0L) {
                    return null;
                }

                return value;
            }
        }
    }


    private static int enchantmentLevelFromItemId(String itemId) {
        if (isBlank(itemId)) {
            return 0;
        }

        String normalized = itemId.trim().toUpperCase(Locale.ROOT);

        int suffixIndex = normalized.lastIndexOf('@');
        if (suffixIndex >= 0 && suffixIndex + 1 < normalized.length()) {
            int parsed = leadingInt(normalized.substring(suffixIndex + 1));
            if (parsed >= 0) {
                return Math.max(0, Math.min(4, parsed));
            }
        }

        int levelIndex = normalized.lastIndexOf("_LEVEL");
        if (levelIndex >= 0) {
            int parsed = leadingInt(normalized.substring(levelIndex + 6));
            if (parsed >= 0) {
                return Math.max(0, Math.min(4, parsed));
            }
        }

        return 0;
    }

    private static int leadingInt(String value) {
        if (value == null || value.isBlank()) {
            return -1;
        }

        int end = 0;
        while (end < value.length()
                && Character.isDigit(value.charAt(end))) {
            end++;
        }

        if (end <= 0) {
            return -1;
        }

        try {
            return Integer.parseInt(value.substring(0, end));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static List<String> locationCandidates(String city) {
        if (isBlank(city)) {
            return List.of();
        }

        String trimmed = city.trim();
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        Set<String> values = new LinkedHashSet<>();

        /*
         * A selected Royal city searches both its city marketplace and
         * corresponding portal marketplace. SQL MIN(...) therefore returns
         * the cheapest matching sell order from either location.
         *
         * Caerleon, Black Market and Brecilien intentionally remain single
         * locations because they have no matching Royal portal marketplace.
         */
        switch (normalized) {
            case "THETFORD" -> {
                values.add("0007");
                values.add("0301");
                values.add("Thetford Portal");
            }

            case "LYMHURST" -> {
                values.add("1002");
                values.add("1301");
                values.add("Lymhurst Portal");
            }

            case "BRIDGEWATCH" -> {
                values.add("2004");
                values.add("2301");
                values.add("Bridgewatch Portal");
            }

            case "MARTLOCK" -> {
                values.add("3008");
                values.add("3301");
                values.add("Martlock Portal");
            }

            case "FORT STERLING", "FORTSTERLING" -> {
                values.add("4002");
                values.add("4301");
                values.add("Fort Sterling Portal");
            }

            default -> {
                String numericId = MARKET_LOCATION_IDS.get(normalized);

                if (numericId != null) {
                    values.add(numericId);
                }
            }
        }

        /*
         * Preserve the original configured market ID as well. This keeps
         * Caerleon, Black Market, Brecilien and future entries working.
         */
        String configuredMarketId = MARKET_LOCATION_IDS.get(normalized);

        if (configuredMarketId != null) {
            values.add(configuredMarketId);
        }

        /*
         * Some captures or older databases can store zero-padded IDs without
         * the leading zero. Accept both forms, for example 0301 and 301.
         */
        List<String> numericValues = new ArrayList<>(values);

        for (String value : numericValues) {
            try {
                values.add(String.valueOf(Integer.parseInt(value)));
            } catch (NumberFormatException ignored) {
                // Textual location aliases are intentionally kept unchanged.
            }
        }

        values.add(trimmed);
        values.add(trimmed + " Market");

        return new ArrayList<>(values);
    }

    private static String displayNameItemId(
            String displayName,
            int enchantment
    ) {
        if (isBlank(displayName)) {
            return null;
        }

        String baseId = craftingItemNameIds().get(normalizeDisplayName(displayName));

        if (isBlank(baseId)) {
            return null;
        }

        return addEnchantmentSuffix(baseId, enchantment);
    }

    private static String addEnchantmentSuffix(
            String itemId,
            int enchantment
    ) {
        if (isBlank(itemId)) {
            return itemId;
        }

        String cleanBase = itemId.trim();
        int suffixIndex = cleanBase.indexOf('@');

        if (suffixIndex >= 0) {
            cleanBase = cleanBase.substring(0, suffixIndex);
        }

        if (enchantment <= 0) {
            return cleanBase;
        }

        return cleanBase + "@" + Math.max(1, Math.min(4, enchantment));
    }

    private static Long albionIdForItemId(String itemId) {
        if (isBlank(itemId)) {
            return null;
        }

        return itemAlbionIds().get(normalizeItemId(itemId));
    }

    private static Map<String, Long> itemAlbionIds() {
        Map<String, Long> current = itemAlbionIds;

        if (current != null) {
            return current;
        }

        synchronized (LocalMarketPriceService.class) {
            if (itemAlbionIds == null) {
                itemAlbionIds = loadItemAlbionIds();
            }

            return itemAlbionIds;
        }
    }

    private static Map<String, String> craftingItemNameIds() {
        Map<String, String> current = craftingItemNameIds;

        if (current != null) {
            return current;
        }

        synchronized (LocalMarketPriceService.class) {
            if (craftingItemNameIds == null) {
                craftingItemNameIds = loadCraftingItemNameIds();
            }

            return craftingItemNameIds;
        }
    }

    private static Map<String, Long> loadItemAlbionIds() {
        Map<String, Long> values = new HashMap<>();

        try (InputStream input = LocalMarketPriceService.class
                .getResourceAsStream(ITEM_ALBION_IDS_RESOURCE)) {
            if (input == null) {
                return Map.of();
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    String[] parts = trimmed.split("\t");

                    if (parts.length < 2) {
                        continue;
                    }

                    try {
                        values.put(
                                normalizeItemId(parts[0]),
                                Long.parseLong(parts[1].trim())
                        );
                    } catch (NumberFormatException ignored) {
                        // Skip malformed rows but keep the rest of the map.
                    }
                }
            }
        } catch (IOException ignored) {
            return Map.of();
        }

        return Collections.unmodifiableMap(values);
    }

    private static Map<String, String> loadCraftingItemNameIds() {
        Map<String, String> values = new HashMap<>();

        loadNameIdResource(DISPLAY_ITEM_NAME_IDS_RESOURCE, values);
        loadNameIdResource(CRAFTING_ITEM_NAME_IDS_RESOURCE, values);

        return Collections.unmodifiableMap(values);
    }

    private static void loadNameIdResource(
            String resourceName,
            Map<String, String> values
    ) {
        try (InputStream input = LocalMarketPriceService.class
                .getResourceAsStream(resourceName)) {
            if (input == null) {
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    String[] parts = trimmed.split("\t");

                    if (parts.length < 2) {
                        continue;
                    }

                    values.putIfAbsent(
                            normalizeDisplayName(parts[0]),
                            parts[1].trim()
                    );
                }
            }
        } catch (IOException ignored) {
            // The older crafting-name map remains as a fallback if present.
        }
    }

    private static String normalizeItemId(String itemId) {
        return itemId == null
                ? ""
                : itemId.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeDisplayName(String displayName) {
        return displayName == null
                ? ""
                : displayName
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private static int demandTimescale(String demandType) {
        if (isBlank(demandType)) {
            return 0;
        }

        String normalized = demandType.trim().toLowerCase(Locale.ROOT);

        if (normalized.contains("4w") || normalized.contains("week")) {
            return 2;
        }

        if (normalized.contains("7d") || normalized.contains("day")) {
            return 1;
        }

        return 0;
    }

    private static String demandTypeLabel(int timescale) {
        return switch (timescale) {
            case 1 -> "7d";
            case 2 -> "4w";
            default -> "24h";
        };
    }

    private static String formatLong(long value) {
        synchronized (SILVER_FORMAT) {
            return SILVER_FORMAT.format(value);
        }
    }

    private static String formatObservedAt(Long epochMillis) {
        if (epochMillis == null || epochMillis <= 0L) {
            return "unknown";
        }

        return Instant.ofEpochMilli(epochMillis).toString();
    }

    private static String formatRawPrice(long rawPrice) {
        double silver = rawPrice / (double) PRICE_SCALE;
        return formatSilver(silver);
    }

    private static Double parsePrice(String value) {
        if (isBlank(value)) {
            return null;
        }

        String cleaned = value
                .trim()
                .replace("\u2014", "")
                .replace("...", "")
                .replace("silver", "")
                .replace(" ", "")
                .replace("_", "")
                .replace(",", "");

        if (cleaned.isEmpty()) {
            return null;
        }

        try {
            double parsed = Double.parseDouble(cleaned);

            if (!Double.isFinite(parsed) || parsed < 0.0) {
                return null;
            }

            return parsed;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String textOf(TextField field) {
        return field == null ? null : field.getText();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }

        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record DemandQueryResult(
            long itemAmount,
            long rowCount,
            Long latestObservedAt
    ) {}
}
