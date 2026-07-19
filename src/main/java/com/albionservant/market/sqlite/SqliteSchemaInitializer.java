package com.albionservant.market.sqlite;

import com.albionservant.integration.config.LocalIntegrationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class SqliteSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SqliteSchemaInitializer.class);

    private final JdbcTemplate jdbc;
    private final LocalIntegrationProperties properties;
    private final Path databasePath;

    public SqliteSchemaInitializer(
            JdbcTemplate jdbc,
            LocalIntegrationProperties properties,
            Path albionServantDatabasePath
    ) {
        this.jdbc = jdbc;
        this.properties = properties;
        this.databasePath = albionServantDatabasePath;
    }

    public void initialize() {
        jdbc.execute("PRAGMA journal_mode=WAL");
        jdbc.execute("PRAGMA synchronous=NORMAL");
        jdbc.execute("PRAGMA foreign_keys=ON");

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS market_raw_events (
                    id TEXT PRIMARY KEY,
                    subject TEXT NOT NULL,
                    payload_json TEXT NOT NULL,
                    received_at INTEGER NOT NULL,
                    processed_at INTEGER,
                    status TEXT NOT NULL,
                    attempts INTEGER NOT NULL DEFAULT 0,
                    last_error TEXT
                )
                """);

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS market_orders (
                    id TEXT PRIMARY KEY,
                    server TEXT NOT NULL,
                    order_id INTEGER NOT NULL,
                    item_id TEXT NOT NULL,
                    item_group_type_id TEXT,
                    location_id TEXT NOT NULL,
                    quality_level INTEGER NOT NULL,
                    enchantment_level INTEGER NOT NULL,
                    unit_price_silver INTEGER NOT NULL,
                    amount INTEGER NOT NULL,
                    auction_type TEXT,
                    expires TEXT,
                    observed_at INTEGER NOT NULL,
                    UNIQUE(server, order_id)
                )
                """);

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS market_prices_latest (
                    id TEXT PRIMARY KEY,
                    server TEXT NOT NULL,
                    item_id TEXT NOT NULL,
                    location_id TEXT NOT NULL,
                    quality_level INTEGER NOT NULL,
                    enchantment_level INTEGER NOT NULL,
                    min_sell_price INTEGER,
                    max_buy_price INTEGER,
                    sell_updated_at INTEGER,
                    buy_updated_at INTEGER,
                    observed_at INTEGER NOT NULL,
                    UNIQUE(server, item_id, location_id, quality_level, enchantment_level)
                )
                """);

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS market_history (
                    id TEXT PRIMARY KEY,
                    server TEXT NOT NULL,
                    albion_id INTEGER NOT NULL,
                    location_id TEXT,
                    quality_level INTEGER NOT NULL,
                    timescale INTEGER NOT NULL,
                    item_amount INTEGER NOT NULL,
                    silver_amount INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL,
                    average_price REAL,
                    observed_at INTEGER NOT NULL,
                    UNIQUE(server, albion_id, location_id, quality_level, timescale, timestamp)
                )
                """);

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS gold_prices (
                    id TEXT PRIMARY KEY,
                    server TEXT NOT NULL,
                    price INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL,
                    observed_at INTEGER NOT NULL,
                    UNIQUE(server, timestamp)
                )
                """);

        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_raw_received_at ON market_raw_events(received_at)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_orders_item ON market_orders(server, item_id, location_id, quality_level, enchantment_level)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_prices_item ON market_prices_latest(server, item_id)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_history_item ON market_history(server, albion_id, location_id, quality_level, timescale, timestamp)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_gold_timestamp ON gold_prices(server, timestamp)");

        long cutoff = Instant.now()
                .minus(Math.max(1, properties.getRawRetentionDays()), ChronoUnit.DAYS)
                .toEpochMilli();
        int deleted = jdbc.update("DELETE FROM market_raw_events WHERE received_at < ?", cutoff);
        jdbc.execute("PRAGMA optimize");

        log.info("SQLite initialized at {}. Deleted {} expired raw events.", databasePath, deleted);
    }
}
