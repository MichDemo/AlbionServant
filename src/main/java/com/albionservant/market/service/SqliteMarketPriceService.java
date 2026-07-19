package com.albionservant.market.service;

import com.albionservant.market.sqlite.SqliteMarketStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SqliteMarketPriceService implements MarketPriceService {

    private final SqliteMarketStore store;

    public SqliteMarketPriceService(SqliteMarketStore store) {
        this.store = store;
    }

    @Override
    public Optional<MarketPriceView> getLatestPrice(
            String server,
            String itemId,
            String locationId,
            int qualityLevel,
            int enchantmentLevel
    ) {
        return store.findLatestPrice(server, itemId, locationId, qualityLevel, enchantmentLevel);
    }

    @Override
    public List<MarketPriceView> getLatestPrices(String server, String itemId) {
        return store.findLatestPrices(server, itemId);
    }
}
