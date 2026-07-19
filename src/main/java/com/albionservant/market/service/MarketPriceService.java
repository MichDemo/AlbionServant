package com.albionservant.market.service;

import java.util.List;
import java.util.Optional;

public interface MarketPriceService {

    Optional<MarketPriceView> getLatestPrice(
            String server,
            String itemId,
            String locationId,
            int qualityLevel,
            int enchantmentLevel
    );

    List<MarketPriceView> getLatestPrices(String server, String itemId);
}
