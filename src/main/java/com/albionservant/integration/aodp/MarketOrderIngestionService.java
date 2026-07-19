package com.albionservant.integration.aodp;

import com.albionservant.integration.aodp.dto.AodpMarketOrder;
import com.albionservant.integration.aodp.dto.AodpMarketUpload;
import com.albionservant.integration.config.LocalIntegrationProperties;
import com.albionservant.market.event.MarketPricesUpdatedEvent;
import com.albionservant.market.model.MarketOrderDocument;
import com.albionservant.market.model.MarketPriceDocument;
import com.albionservant.market.sqlite.SqliteMarketStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class MarketOrderIngestionService {

    private final SqliteMarketStore store;
    private final LocalIntegrationProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    public MarketOrderIngestionService(
            SqliteMarketStore store,
            LocalIntegrationProperties properties,
            ApplicationEventPublisher eventPublisher
    ) {
        this.store = store;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void ingest(AodpMarketUpload upload) {
        Instant observedAt = Instant.now();
        String server = properties.getServerName();
        Map<PriceKey, PriceAccumulator> prices = new HashMap<>();

        for (AodpMarketOrder order : upload.orders()) {
            if (order.itemId() == null || order.itemId().isBlank() || order.locationId() == null) {
                continue;
            }

            MarketOrderDocument document = new MarketOrderDocument();
            document.setId(server + ":" + order.id());
            document.setServer(server);
            document.setOrderId(order.id());
            document.setItemId(order.itemId());
            document.setItemGroupTypeId(order.itemGroupTypeId());
            document.setLocationId(order.locationId());
            document.setQualityLevel(order.qualityLevel());
            document.setEnchantmentLevel(order.enchantmentLevel());
            document.setUnitPriceSilver(order.unitPriceSilver());
            document.setAmount(order.amount());
            document.setAuctionType(order.auctionType());
            document.setExpires(order.expires());
            document.setObservedAt(observedAt);
            store.upsertMarketOrder(document);

            if (order.unitPriceSilver() <= 0 || order.amount() <= 0) {
                continue;
            }

            PriceKey key = new PriceKey(
                    server, order.itemId(), order.locationId(),
                    order.qualityLevel(), order.enchantmentLevel()
            );
            PriceAccumulator accumulator = prices.computeIfAbsent(key, ignored -> new PriceAccumulator());

            if (isSell(order.auctionType())) {
                accumulator.minSell = accumulator.minSell == null
                        ? order.unitPriceSilver()
                        : Math.min(accumulator.minSell, order.unitPriceSilver());
            } else if (isBuy(order.auctionType())) {
                accumulator.maxBuy = accumulator.maxBuy == null
                        ? order.unitPriceSilver()
                        : Math.max(accumulator.maxBuy, order.unitPriceSilver());
            }
        }

        Set<String> updatedKeys = new HashSet<>();
        for (Map.Entry<PriceKey, PriceAccumulator> entry : prices.entrySet()) {
            PriceKey key = entry.getKey();
            PriceAccumulator value = entry.getValue();
            String id = MarketPriceDocument.makeId(
                    key.server, key.itemId, key.locationId,
                    key.qualityLevel, key.enchantmentLevel
            );

            store.upsertMarketPrice(
                    id, key.server, key.itemId, key.locationId,
                    key.qualityLevel, key.enchantmentLevel,
                    value.minSell, value.maxBuy, observedAt
            );
            updatedKeys.add(id);
        }

        if (!updatedKeys.isEmpty()) {
            eventPublisher.publishEvent(new MarketPricesUpdatedEvent(Set.copyOf(updatedKeys)));
        }
    }

    private boolean isSell(String auctionType) {
        String type = normalize(auctionType);
        return type.contains("offer") || type.contains("sell");
    }

    private boolean isBuy(String auctionType) {
        String type = normalize(auctionType);
        return type.contains("request") || type.contains("buy");
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private record PriceKey(String server, String itemId, String locationId, int qualityLevel, int enchantmentLevel) {}

    private static class PriceAccumulator {
        private Long minSell;
        private Long maxBuy;
    }
}
