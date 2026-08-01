package com.albionservant.integration.market;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

// ALBIONSERVANT_RESOURCE_PRICE_FALLBACK_PATCH
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
                apiField.setText("\u2014");
                return;
            }

            apiField.setText("...");
            apiField.setTooltip(new Tooltip(
                    itemId
                            + " | "
                            + city
                            + " | Q"
                            + qualityLevel
                            + " | local SQLite"
            ));

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
                        apiField.setText("\u2014");

                        String detail = isBlank(finalError)
                                ? "No local Q"
                                + qualityLevel
                                + " price for "
                                + itemId
                                + " in "
                                + city
                                : "SQLite error: " + finalError;

                        apiField.setTooltip(new Tooltip(detail));
                        return;
                    }

                    apiField.setText(formatRawPrice(finalRawPrice));
                    apiField.setTooltip(new Tooltip(
                            itemId
                                    + " | "
                                    + city
                                    + " | Q"
                                    + qualityLevel
                                    + " | raw="
                                    + finalRawPrice
                    ));
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

        refresh.run();
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
        if (isBlank(baseItemId) || enchantment <= 0) {
            return baseItemId;
        }

        int suffixIndex = baseItemId.indexOf('@');

        String cleanBase = suffixIndex >= 0
                ? baseItemId.substring(0, suffixIndex)
                : baseItemId;

        return cleanBase + "@" + enchantment;
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

    public static String artifactItemId(
            String artifactTypeName,
            int tier
    ) {
        if ("AVALON_ENERGY".equalsIgnoreCase(artifactTypeName)) {
            return "QUESTITEM_TOKEN_AVALON";
        }

        return null;
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
                                 "PRAGMA busy_timeout = 3000"
                         )) {
                pragma.execute();
            }

            Long latestPrice = queryLatestPrice(
                    connection,
                    itemId,
                    locations,
                    qualityLevel,
                    resourceLookup
            );

            if (latestPrice != null && latestPrice > 0L) {
                return latestPrice;
            }

            /*
             * Fallback to raw orders. This covers a short delay between
             * receiving market orders and updating market_prices_latest.
             */
            return queryRawOrderPrice(
                    connection,
                    itemId,
                    locations,
                    qualityLevel,
                    resourceLookup
            );
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

        String sql = "SELECT MIN(min_sell_price) "
                + "FROM market_prices_latest "
                + "WHERE UPPER(item_id) = UPPER(?) "
                + "AND " + qualityPredicate + " "
                + "AND location_id IN (" + placeholders + ") "
                + "AND min_sell_price IS NOT NULL "
                + "AND min_sell_price > 0";

        return executePriceQuery(
                connection,
                sql,
                itemId,
                locations,
                qualityLevel,
                !resourceLookup
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

        String sql = "SELECT MIN(unit_price_silver) "
                + "FROM market_orders "
                + "WHERE UPPER(item_id) = UPPER(?) "
                + "AND " + qualityPredicate + " "
                + "AND location_id IN (" + placeholders + ") "
                + "AND LOWER(auction_type) = 'offer' "
                + "AND unit_price_silver IS NOT NULL "
                + "AND unit_price_silver > 0";

        return executePriceQuery(
                connection,
                sql,
                itemId,
                locations,
                qualityLevel,
                !resourceLookup
        );
    }

    private static Long executePriceQuery(
            Connection connection,
            String sql,
            String itemId,
            List<String> locations,
            int qualityLevel,
            boolean bindQuality
    ) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            int parameterIndex = 1;
            statement.setString(parameterIndex++, itemId);

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

    private static List<String> locationCandidates(String city) {
        if (isBlank(city)) {
            return List.of();
        }

        String trimmed = city.trim();
        String normalized = trimmed.toUpperCase(Locale.ROOT);
        Set<String> values = new LinkedHashSet<>();

        String numericId = MARKET_LOCATION_IDS.get(normalized);

        if (numericId != null) {
            values.add(numericId);

            /*
             * Some local captures may store Thetford as 7 instead of 0007.
             * Keep both representations without changing other city IDs.
             */
            try {
                values.add(String.valueOf(Integer.parseInt(numericId)));
            } catch (NumberFormatException ignored) {
                // Non-numeric location IDs are kept unchanged.
            }
        }

        values.add(trimmed);
        values.add(trimmed + " Market");

        return new ArrayList<>(values);
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
}
