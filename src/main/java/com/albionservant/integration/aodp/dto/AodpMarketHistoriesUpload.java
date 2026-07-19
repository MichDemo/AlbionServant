package com.albionservant.integration.aodp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AodpMarketHistoriesUpload(
        @JsonProperty("AlbionId") long albionId,
        @JsonProperty("LocationId") String locationId,
        @JsonProperty("QualityLevel") int qualityLevel,
        @JsonProperty("Timescale") int timescale,
        @JsonProperty("MarketHistories") List<AodpMarketHistory> marketHistories
) {
    public AodpMarketHistoriesUpload {
        marketHistories = marketHistories == null ? List.of() : List.copyOf(marketHistories);
    }
}
