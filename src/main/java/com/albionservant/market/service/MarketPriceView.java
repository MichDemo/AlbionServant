package com.albionservant.market.service;

import java.time.Instant;

public record MarketPriceView(
        String server,
        String itemId,
        String locationId,
        int qualityLevel,
        int enchantmentLevel,
        Long minSellPrice,
        Long maxBuyPrice,
        Instant observedAt
) {
}
