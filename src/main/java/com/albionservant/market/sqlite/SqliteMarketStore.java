package com.albionservant.market.sqlite;

import com.albionservant.market.model.GoldPriceDocument;
import com.albionservant.market.model.MarketHistoryDocument;
import com.albionservant.market.model.MarketOrderDocument;
import com.albionservant.market.service.MarketPriceView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class SqliteMarketStore {

    private final JdbcTemplate jdbc;

    public SqliteMarketStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertRawEventIfAbsent(
            String id,
            String subject,
            String payloadJson,
            Instant receivedAt
    ) {
        jdbc.update("""
                INSERT OR IGNORE INTO market_raw_events
                    (id, subject, payload_json, received_at, status, attempts)
                VALUES (?, ?, ?, ?, 'PENDING', 0)
                """, id, subject, payloadJson, epoch(receivedAt));
    }

    public String findRawEventStatus(String id) {
        List<String> values = jdbc.query(
                "SELECT status FROM market_raw_events WHERE id = ?",
                (rs, rowNum) -> rs.getString(1),
                id
        );
        return values.isEmpty() ? null : values.get(0);
    }

    public void markRawProcessing(String id) {
        jdbc.update("""
                UPDATE market_raw_events
                SET status = 'PROCESSING', attempts = attempts + 1, last_error = NULL
                WHERE id = ?
                """, id);
    }

    public void markRawProcessed(String id, Instant processedAt) {
        jdbc.update("""
                UPDATE market_raw_events
                SET status = 'PROCESSED', processed_at = ?, last_error = NULL
                WHERE id = ?
                """, epoch(processedAt), id);
    }

    public void markRawRetryableFailure(String id, String error) {
        jdbc.update("""
                UPDATE market_raw_events
                SET status = 'FAILED_RETRY', last_error = ?
                WHERE id = ?
                """, error, id);
    }

    public void markRawRejected(String id, Instant processedAt, String error) {
        jdbc.update("""
                UPDATE market_raw_events
                SET status = 'REJECTED', processed_at = ?, last_error = ?
                WHERE id = ?
                """, epoch(processedAt), error, id);
    }

    public void upsertMarketOrder(MarketOrderDocument document) {
        jdbc.update("""
                INSERT INTO market_orders (
                    id, server, order_id, item_id, item_group_type_id, location_id,
                    quality_level, enchantment_level, unit_price_silver, amount,
                    auction_type, expires, observed_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(server, order_id) DO UPDATE SET
                    id = excluded.id,
                    item_id = excluded.item_id,
                    item_group_type_id = excluded.item_group_type_id,
                    location_id = excluded.location_id,
                    quality_level = excluded.quality_level,
                    enchantment_level = excluded.enchantment_level,
                    unit_price_silver = excluded.unit_price_silver,
                    amount = excluded.amount,
                    auction_type = excluded.auction_type,
                    expires = excluded.expires,
                    observed_at = excluded.observed_at
                """,
                document.getId(),
                document.getServer(),
                document.getOrderId(),
                document.getItemId(),
                document.getItemGroupTypeId(),
                document.getLocationId(),
                document.getQualityLevel(),
                document.getEnchantmentLevel(),
                document.getUnitPriceSilver(),
                document.getAmount(),
                document.getAuctionType(),
                document.getExpires(),
                epoch(document.getObservedAt())
        );
    }

    public void upsertMarketPrice(
            String id,
            String server,
            String itemId,
            String locationId,
            int qualityLevel,
            int enchantmentLevel,
            Long minSellPrice,
            Long maxBuyPrice,
            Instant observedAt
    ) {
        Long observed = epoch(observedAt);
        jdbc.update("""
                INSERT INTO market_prices_latest (
                    id, server, item_id, location_id, quality_level, enchantment_level,
                    min_sell_price, max_buy_price, sell_updated_at, buy_updated_at, observed_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    min_sell_price = COALESCE(excluded.min_sell_price, market_prices_latest.min_sell_price),
                    max_buy_price = COALESCE(excluded.max_buy_price, market_prices_latest.max_buy_price),
                    sell_updated_at = CASE
                        WHEN excluded.min_sell_price IS NULL THEN market_prices_latest.sell_updated_at
                        ELSE excluded.sell_updated_at
                    END,
                    buy_updated_at = CASE
                        WHEN excluded.max_buy_price IS NULL THEN market_prices_latest.buy_updated_at
                        ELSE excluded.buy_updated_at
                    END,
                    observed_at = excluded.observed_at
                """,
                id, server, itemId, locationId, qualityLevel, enchantmentLevel,
                minSellPrice, maxBuyPrice,
                minSellPrice == null ? null : observed,
                maxBuyPrice == null ? null : observed,
                observed
        );
    }

    public void upsertMarketHistory(MarketHistoryDocument document) {
        jdbc.update("""
                INSERT INTO market_history (
                    id, server, albion_id, location_id, quality_level, timescale,
                    item_amount, silver_amount, timestamp, average_price, observed_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    item_amount = excluded.item_amount,
                    silver_amount = excluded.silver_amount,
                    average_price = excluded.average_price,
                    observed_at = excluded.observed_at
                """,
                document.getId(), document.getServer(), document.getAlbionId(),
                document.getLocationId(), document.getQualityLevel(), document.getTimescale(),
                document.getItemAmount(), document.getSilverAmount(), document.getTimestamp(),
                document.getAveragePrice(), epoch(document.getObservedAt())
        );
    }

    public void upsertGoldPrice(GoldPriceDocument document) {
        jdbc.update("""
                INSERT INTO gold_prices (id, server, price, timestamp, observed_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(server, timestamp) DO UPDATE SET
                    id = excluded.id,
                    price = excluded.price,
                    observed_at = excluded.observed_at
                """,
                document.getId(), document.getServer(), document.getPrice(),
                document.getTimestamp(), epoch(document.getObservedAt())
        );
    }

    public Optional<MarketPriceView> findLatestPrice(
            String server,
            String itemId,
            String locationId,
            int qualityLevel,
            int enchantmentLevel
    ) {
        List<MarketPriceView> results = jdbc.query("""
                SELECT server, item_id, location_id, quality_level, enchantment_level,
                       min_sell_price, max_buy_price, observed_at
                FROM market_prices_latest
                WHERE server = ? AND item_id = ? AND location_id = ?
                  AND quality_level = ? AND enchantment_level = ?
                LIMIT 1
                """, this::mapPrice, server, itemId, locationId, qualityLevel, enchantmentLevel);
        return results.stream().findFirst();
    }

    public List<MarketPriceView> findLatestPrices(String server, String itemId) {
        return jdbc.query("""
                SELECT server, item_id, location_id, quality_level, enchantment_level,
                       min_sell_price, max_buy_price, observed_at
                FROM market_prices_latest
                WHERE server = ? AND item_id = ?
                ORDER BY location_id, quality_level, enchantment_level
                """, this::mapPrice, server, itemId);
    }

    private MarketPriceView mapPrice(ResultSet rs, int rowNum) throws SQLException {
        return new MarketPriceView(
                rs.getString("server"),
                rs.getString("item_id"),
                rs.getString("location_id"),
                rs.getInt("quality_level"),
                rs.getInt("enchantment_level"),
                nullableLong(rs, "min_sell_price"),
                nullableLong(rs, "max_buy_price"),
                instant(rs.getLong("observed_at"))
        );
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private Long epoch(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    private Instant instant(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }
}
