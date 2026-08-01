package com.albionservant.integration.market;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public final class LocalMarketQueryService {

    private static final int MAX_SCAN = 5000;
    private final Path databasePath;

    public LocalMarketQueryService() {
        this(resolveDatabasePath());
    }

    public LocalMarketQueryService(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath().normalize();
    }

    public Path databasePath() {
        return databasePath;
    }

    public List<LocalMarketRow> findLatest(
            String search,
            String location,
            String auctionType,
            int limit
    ) throws SQLException {
        int safeLimit = Math.max(1, Math.min(limit, 2000));

        try (Connection connection = openConnection()) {
            String table = sourceTable(connection);
            String sql = "SELECT rowid AS __rowid__, * FROM \"" + table
                    + "\" ORDER BY rowid DESC LIMIT " + MAX_SCAN;

            List<LocalMarketRow> rows = new ArrayList<>();

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                Columns columns = Columns.from(resultSet.getMetaData());

                while (resultSet.next() && rows.size() < safeLimit) {
                    LocalMarketRow row = map(resultSet, columns);

                    if (matches(row, search, location, auctionType)) {
                        rows.add(row);
                    }
                }
            }

            return rows;
        }
    }

    public List<String> findLocations() throws SQLException {
        LinkedHashSet<String> values = new LinkedHashSet<>();

        try (Connection connection = openConnection()) {
            String table = sourceTable(connection);
            String sql = "SELECT * FROM \"" + table
                    + "\" ORDER BY rowid DESC LIMIT " + MAX_SCAN;

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                Columns columns = Columns.from(resultSet.getMetaData());

                while (resultSet.next()) {
                    String value = columns.text(
                            resultSet,
                            "location_id", "locationId", "location", "city"
                    );

                    if (value != null && !value.isBlank()) {
                        values.add(value.trim());
                    }
                }
            }
        }

        return new ArrayList<>(values);
    }

    public long countLatestRows() throws SQLException {
        try (Connection connection = openConnection()) {
            String table = sourceTable(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT COUNT(*) FROM \"" + table + "\""
                 )) {

                return resultSet.next() ? resultSet.getLong(1) : 0L;
            }
        }
    }

    private Connection openConnection() throws SQLException {
        if (!Files.isRegularFile(databasePath)) {
            throw new SQLException("SQLite database not found: " + databasePath);
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new SQLException(
                    "Missing SQLite JDBC driver: org.xerial:sqlite-jdbc",
                    exception
            );
        }

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + databasePath
        );

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 3000");
            statement.execute("PRAGMA query_only = ON");
        }

        return connection;
    }

    private String sourceTable(Connection connection) throws SQLException {
        if (tableExists(connection, "market_prices_latest")) {
            return "market_prices_latest";
        }

        if (tableExists(connection, "market_orders")) {
            return "market_orders";
        }

        throw new SQLException(
                "Tables market_prices_latest and market_orders are missing."
        );
    }

    private boolean tableExists(Connection connection, String table)
            throws SQLException {
        String escaped = table.replace("'", "''");

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT 1 FROM sqlite_master WHERE type='table' "
                             + "AND name='" + escaped + "' LIMIT 1"
             )) {

            return resultSet.next();
        }
    }

    private LocalMarketRow map(ResultSet rs, Columns columns)
            throws SQLException {
        return new LocalMarketRow(
                dash(columns.text(
                        rs, "item_type_id", "itemTypeId", "item_id", "itemId", "item"
                )),
                dash(columns.text(
                        rs, "location_id", "locationId", "location", "city"
                )),
                columns.integer(
                        rs, "quality_level", "qualityLevel", "quality"
                ),
                columns.integer(
                        rs, "enchantment_level", "enchantmentLevel", "enchantment"
                ),
                dash(columns.text(
                        rs, "auction_type", "auctionType", "order_type", "orderType", "type"
                )),
                columns.longValue(
                        rs,
                        "unit_price_silver", "unitPriceSilver",
                        "unit_price_raw", "unitPriceRaw",
                        "price", "sell_price_min", "sellPriceMin",
                        "buy_price_max", "buyPriceMax"
                ),
                columns.longValue(rs, "amount", "quantity"),
                dash(columns.text(
                        rs,
                        "updated_at", "updatedAt",
                        "observed_at", "observedAt",
                        "captured_at", "capturedAt",
                        "last_seen_at", "lastSeenAt",
                        "received_at", "receivedAt",
                        "created_at", "createdAt",
                        "expires_at", "expiresAt", "expires"
                ))
        );
    }

    private boolean matches(
            LocalMarketRow row,
            String search,
            String location,
            String auctionType
    ) {
        String query = normalize(search);

        if (!query.isEmpty()) {
            String haystack = (
                    row.itemTypeId() + " "
                            + row.locationId() + " "
                            + row.auctionType()
            ).toLowerCase(Locale.ROOT);

            if (!haystack.contains(query)) {
                return false;
            }
        }

        if (!isAll(location)
                && !row.locationId().equalsIgnoreCase(location.trim())) {
            return false;
        }

        return isAll(auctionType)
                || row.auctionType().equalsIgnoreCase(auctionType.trim());
    }

    private static Path resolveDatabasePath() {
        String configured = firstNonBlank(
                System.getProperty("albion.local.sqlite.path"),
                System.getProperty("albion.sqlite.path"),
                System.getenv("ALBIONSERVANT_DB")
        );

        if (configured != null) {
            return Paths.get(configured);
        }

        String localAppData = System.getenv("LOCALAPPDATA");

        if (localAppData != null && !localAppData.isBlank()) {
            return Paths.get(
                    localAppData,
                    "AlbionServant",
                    "data",
                    "albionservant.db"
            );
        }

        return Paths.get(
                System.getProperty("user.home"),
                ".albionservant",
                "data",
                "albionservant.db"
        );
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isAll(String value) {
        return value == null
                || value.isBlank()
                || "ALL".equalsIgnoreCase(value)
                || "WSZYSTKIE".equalsIgnoreCase(value);
    }

    private static String dash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private static final class Columns {

        private final Map<String, Integer> indexes;

        private Columns(Map<String, Integer> indexes) {
            this.indexes = indexes;
        }

        static Columns from(ResultSetMetaData metadata) throws SQLException {
            Map<String, Integer> indexes = new HashMap<>();

            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                String label = metadata.getColumnLabel(index);

                if (label == null || label.isBlank()) {
                    label = metadata.getColumnName(index);
                }

                indexes.putIfAbsent(normalizeColumn(label), index);
            }

            return new Columns(indexes);
        }

        String text(ResultSet rs, String... names) throws SQLException {
            Integer index = index(names);

            if (index == null) {
                return "";
            }

            Object value = rs.getObject(index);
            return value == null ? "" : String.valueOf(value);
        }

        int integer(ResultSet rs, String... names) throws SQLException {
            long value = longValue(rs, names);

            if (value > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            if (value < Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }

            return (int) value;
        }

        long longValue(ResultSet rs, String... names) throws SQLException {
            Integer index = index(names);

            if (index == null) {
                return 0L;
            }

            Object value = rs.getObject(index);

            if (value instanceof Number number) {
                return number.longValue();
            }

            if (value == null) {
                return 0L;
            }

            try {
                return Long.parseLong(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }

        private Integer index(String... names) {
            for (String name : names) {
                Integer index = indexes.get(normalizeColumn(name));

                if (index != null) {
                    return index;
                }
            }

            return null;
        }

        private static String normalizeColumn(String value) {
            return value == null
                    ? ""
                    : value.replaceAll("[^A-Za-z0-9]", "")
                            .toLowerCase(Locale.ROOT);
        }
    }
}
