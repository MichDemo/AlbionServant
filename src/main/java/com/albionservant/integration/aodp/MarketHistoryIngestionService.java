package com.albionservant.integration.aodp;

import com.albionservant.integration.aodp.dto.AodpMarketHistoriesUpload;
import com.albionservant.integration.aodp.dto.AodpMarketHistory;
import com.albionservant.integration.config.LocalIntegrationProperties;
import com.albionservant.market.model.MarketHistoryDocument;
import com.albionservant.market.sqlite.SqliteMarketStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MarketHistoryIngestionService {

    private final SqliteMarketStore store;
    private final LocalIntegrationProperties properties;

    public MarketHistoryIngestionService(SqliteMarketStore store, LocalIntegrationProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    @Transactional
    public void ingest(AodpMarketHistoriesUpload upload) {
        Instant observedAt = Instant.now();
        String server = properties.getServerName();

        for (AodpMarketHistory history : upload.marketHistories()) {
            MarketHistoryDocument document = new MarketHistoryDocument();
            document.setId(String.join(":",
                    server,
                    String.valueOf(upload.albionId()),
                    safe(upload.locationId()),
                    String.valueOf(upload.qualityLevel()),
                    String.valueOf(upload.timescale()),
                    String.valueOf(history.timestamp())
            ));
            document.setServer(server);
            document.setAlbionId(upload.albionId());
            document.setLocationId(upload.locationId());
            document.setQualityLevel(upload.qualityLevel());
            document.setTimescale(upload.timescale());
            document.setItemAmount(history.itemAmount());
            document.setSilverAmount(history.silverAmount());
            document.setTimestamp(history.timestamp());
            document.setAveragePrice(history.itemAmount() > 0
                    ? history.silverAmount() / (double) history.itemAmount()
                    : null);
            document.setObservedAt(observedAt);
            store.upsertMarketHistory(document);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(":", "_");
    }
}
