package com.albionservant.integration.aodp;

import com.albionservant.integration.aodp.dto.AodpGoldPricesUpload;
import com.albionservant.integration.config.LocalIntegrationProperties;
import com.albionservant.market.model.GoldPriceDocument;
import com.albionservant.market.sqlite.SqliteMarketStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class GoldPriceIngestionService {

    private final SqliteMarketStore store;
    private final LocalIntegrationProperties properties;

    public GoldPriceIngestionService(SqliteMarketStore store, LocalIntegrationProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    @Transactional
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
            store.upsertGoldPrice(document);
        }
    }
}
