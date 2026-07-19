package com.albionservant.market.model;

import java.time.Instant;

public class MarketPriceDocument {
    private String id;
    private String server;
    private String itemId;
    private String locationId;
    private int qualityLevel;
    private int enchantmentLevel;
    private Long minSellPrice;
    private Long maxBuyPrice;
    private Instant sellUpdatedAt;
    private Instant buyUpdatedAt;
    private Instant observedAt;

    public static String makeId(String server, String itemId, String locationId, int quality, int enchantment) {
        return String.join(":", safe(server), safe(itemId), safe(locationId), String.valueOf(quality), String.valueOf(enchantment));
    }

    private static String safe(String value) { return value == null ? "" : value.replace(":", "_"); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public int getQualityLevel() { return qualityLevel; }
    public void setQualityLevel(int qualityLevel) { this.qualityLevel = qualityLevel; }
    public int getEnchantmentLevel() { return enchantmentLevel; }
    public void setEnchantmentLevel(int enchantmentLevel) { this.enchantmentLevel = enchantmentLevel; }
    public Long getMinSellPrice() { return minSellPrice; }
    public void setMinSellPrice(Long minSellPrice) { this.minSellPrice = minSellPrice; }
    public Long getMaxBuyPrice() { return maxBuyPrice; }
    public void setMaxBuyPrice(Long maxBuyPrice) { this.maxBuyPrice = maxBuyPrice; }
    public Instant getSellUpdatedAt() { return sellUpdatedAt; }
    public void setSellUpdatedAt(Instant sellUpdatedAt) { this.sellUpdatedAt = sellUpdatedAt; }
    public Instant getBuyUpdatedAt() { return buyUpdatedAt; }
    public void setBuyUpdatedAt(Instant buyUpdatedAt) { this.buyUpdatedAt = buyUpdatedAt; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
}
