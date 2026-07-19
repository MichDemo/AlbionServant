package com.albionservant.market.service;

import com.albionservant.integration.config.LocalIntegrationProperties;
import com.albionservant.market.model.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MarketMongoIndexInitializer {

    private final MongoTemplate mongoTemplate;
    private final LocalIntegrationProperties properties;

    public MarketMongoIndexInitializer(MongoTemplate mongoTemplate, LocalIntegrationProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    public void initialize() {
        mongoTemplate.indexOps(RawMarketEventDocument.class)
                .ensureIndex(new Index()
                        .on("receivedAt", Sort.Direction.ASC)
                        .expire(Duration.ofDays(Math.max(1, properties.getRawRetentionDays()))));

        mongoTemplate.indexOps(MarketOrderDocument.class)
                .ensureIndex(new Index()
                        .on("server", Sort.Direction.ASC)
                        .on("orderId", Sort.Direction.ASC)
                        .unique());

        mongoTemplate.indexOps(MarketOrderDocument.class)
                .ensureIndex(new Index()
                        .on("server", Sort.Direction.ASC)
                        .on("itemId", Sort.Direction.ASC)
                        .on("locationId", Sort.Direction.ASC)
                        .on("qualityLevel", Sort.Direction.ASC)
                        .on("enchantmentLevel", Sort.Direction.ASC));

        mongoTemplate.indexOps(MarketPriceDocument.class)
                .ensureIndex(new Index()
                        .on("server", Sort.Direction.ASC)
                        .on("itemId", Sort.Direction.ASC)
                        .on("locationId", Sort.Direction.ASC)
                        .on("qualityLevel", Sort.Direction.ASC)
                        .on("enchantmentLevel", Sort.Direction.ASC)
                        .unique());

        mongoTemplate.indexOps(MarketHistoryDocument.class)
                .ensureIndex(new Index()
                        .on("server", Sort.Direction.ASC)
                        .on("albionId", Sort.Direction.ASC)
                        .on("locationId", Sort.Direction.ASC)
                        .on("qualityLevel", Sort.Direction.ASC)
                        .on("timescale", Sort.Direction.ASC)
                        .on("timestamp", Sort.Direction.ASC)
                        .unique());

        mongoTemplate.indexOps(GoldPriceDocument.class)
                .ensureIndex(new Index()
                        .on("server", Sort.Direction.ASC)
                        .on("timestamp", Sort.Direction.ASC)
                        .unique());
    }
}
