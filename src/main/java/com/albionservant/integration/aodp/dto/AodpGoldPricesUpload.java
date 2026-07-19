package com.albionservant.integration.aodp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AodpGoldPricesUpload(
        @JsonProperty("Prices") List<Long> prices,
        @JsonProperty("Timestamps") List<Long> timestamps
) {
    public AodpGoldPricesUpload {
        prices = prices == null ? List.of() : List.copyOf(prices);
        timestamps = timestamps == null ? List.of() : List.copyOf(timestamps);
    }
}
