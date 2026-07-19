package com.albionservant.market.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("market_orders")
public class MarketOrderDocument {

    @Id
    private String id;
    private String server;
    private long orderId;
    private String itemId;
    private String itemGroupTypeId;
    private String locationId;
    private int qualityLevel;
    private int enchantmentLevel;
    private long unitPriceSilver;
    private long amount;
    private String auctionType;
    private String expires;
    private Instant observedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemGroupTypeId() { return itemGroupTypeId; }
    public void setItemGroupTypeId(String itemGroupTypeId) { this.itemGroupTypeId = itemGroupTypeId; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public int getQualityLevel() { return qualityLevel; }
    public void setQualityLevel(int qualityLevel) { this.qualityLevel = qualityLevel; }
    public int getEnchantmentLevel() { return enchantmentLevel; }
    public void setEnchantmentLevel(int enchantmentLevel) { this.enchantmentLevel = enchantmentLevel; }
    public long getUnitPriceSilver() { return unitPriceSilver; }
    public void setUnitPriceSilver(long unitPriceSilver) { this.unitPriceSilver = unitPriceSilver; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getAuctionType() { return auctionType; }
    public void setAuctionType(String auctionType) { this.auctionType = auctionType; }
    public String getExpires() { return expires; }
    public void setExpires(String expires) { this.expires = expires; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
}
