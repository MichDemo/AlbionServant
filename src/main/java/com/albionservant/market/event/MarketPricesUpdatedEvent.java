package com.albionservant.market.event;

import java.util.Set;

public record MarketPricesUpdatedEvent(Set<String> priceKeys) {
}
