package com.albionservant.market.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("market_history")
public class MarketHistoryDocument {

    @Id
    private String id;
    private String server;
    private long albionId;
    private String locationId;
    private int qualityLevel;
    private int timescale;
    private long itemAmount;
    private long silverAmount;
    private long timestamp;
    private Double averagePrice;
    private Instant observedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }
    public long getAlbionId() { return albionId; }
    public void setAlbionId(long albionId) { this.albionId = albionId; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public int getQualityLevel() { return qualityLevel; }
    public void setQualityLevel(int qualityLevel) { this.qualityLevel = qualityLevel; }
    public int getTimescale() { return timescale; }
    public void setTimescale(int timescale) { this.timescale = timescale; }
    public long getItemAmount() { return itemAmount; }
    public void setItemAmount(long itemAmount) { this.itemAmount = itemAmount; }
    public long getSilverAmount() { return silverAmount; }
    public void setSilverAmount(long silverAmount) { this.silverAmount = silverAmount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public Double getAveragePrice() { return averagePrice; }
    public void setAveragePrice(Double averagePrice) { this.averagePrice = averagePrice; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
}
