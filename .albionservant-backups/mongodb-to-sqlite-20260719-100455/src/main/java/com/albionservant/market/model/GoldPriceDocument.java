package com.albionservant.market.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("gold_prices")
public class GoldPriceDocument {

    @Id
    private String id;
    private String server;
    private long price;
    private long timestamp;
    private Instant observedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
}
