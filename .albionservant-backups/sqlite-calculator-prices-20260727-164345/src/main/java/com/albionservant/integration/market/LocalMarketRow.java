package com.albionservant.integration.market;

import java.math.BigDecimal;

public record LocalMarketRow(
        String itemTypeId,
        String locationId,
        int qualityLevel,
        int enchantmentLevel,
        String auctionType,
        long unitPriceRaw,
        long amount,
        String updatedAt
) {
    public BigDecimal unitPriceSilver() {
        return BigDecimal.valueOf(unitPriceRaw).movePointLeft(4).stripTrailingZeros();
    }
}
