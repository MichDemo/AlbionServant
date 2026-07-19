package com.albionservant.integration.aodp;

import com.albionservant.integration.aodp.dto.AodpGoldPricesUpload;
import com.albionservant.integration.config.LocalIntegrationProperties;
import com.albionservant.market.model.GoldPriceDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class GoldPriceIngestionService {

    private final MongoTemplate mongoTemplate;
    private final LocalIntegrationProperties properties;

    public GoldPriceIngestionService(MongoTemplate mongoTemplate, LocalIntegrationProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    public void ingest(AodpGoldPricesUpload upload) {
        int count = Math.min(upload.prices().size(), upload.timestamps().size());
        String server = properties.getServerName();
        Instant observedAt = Instant.now();

        for (int i = 0; i < count; i++) {
            long timestamp = upload.timestamps().get(i);
            GoldPriceDocument document = new GoldPriceDocument();
            document.setId(server + ":" + timestamp);
            document.setServer(server);
            document.setPrice(upload.prices().get(i));
            document.setTimestamp(timestamp);
            document.setObservedAt(observedAt);
            mongoTemplate.save(document);
        }
    }
}
