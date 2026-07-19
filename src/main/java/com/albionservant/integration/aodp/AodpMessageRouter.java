package com.albionservant.integration.aodp;

import com.albionservant.integration.aodp.dto.AodpGoldPricesUpload;
import com.albionservant.integration.aodp.dto.AodpMarketHistoriesUpload;
import com.albionservant.integration.aodp.dto.AodpMarketUpload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AodpMessageRouter {

    private final ObjectMapper objectMapper;
    private final RawEventService rawEventService;
    private final MarketOrderIngestionService marketOrderService;
    private final MarketHistoryIngestionService marketHistoryService;
    private final GoldPriceIngestionService goldPriceService;

    public AodpMessageRouter(
            ObjectMapper objectMapper,
            RawEventService rawEventService,
            MarketOrderIngestionService marketOrderService,
            MarketHistoryIngestionService marketHistoryService,
            GoldPriceIngestionService goldPriceService
    ) {
        this.objectMapper = objectMapper;
        this.rawEventService = rawEventService;
        this.marketOrderService = marketOrderService;
        this.marketHistoryService = marketHistoryService;
        this.goldPriceService = goldPriceService;
    }

    public void route(String subject, byte[] data) {
        RawEventService.RawEventContext context = rawEventService.begin(subject, data);
        if (context.decision() == RawEventService.Decision.ALREADY_FINISHED) {
            return;
        }

        try {
            switch (subject) {
                case AodpSubjects.MARKET_ORDERS -> marketOrderService.ingest(
                        objectMapper.readValue(data, AodpMarketUpload.class)
                );
                case AodpSubjects.MARKET_HISTORIES -> marketHistoryService.ingest(
                        objectMapper.readValue(data, AodpMarketHistoriesUpload.class)
                );
                case AodpSubjects.GOLD_PRICES -> goldPriceService.ingest(
                        objectMapper.readValue(data, AodpGoldPricesUpload.class)
                );
                default -> throw new IllegalArgumentException("Unsupported AODP subject: " + subject);
            }

            rawEventService.markProcessed(context.id());
        } catch (JsonProcessingException | IllegalArgumentException e) {
            rawEventService.markRejected(context.id(), e);
            throw new PermanentMessageException("AODP payload cannot be processed for subject " + subject, e);
        } catch (RuntimeException e) {
            rawEventService.markRetryableFailure(context.id(), e);
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
