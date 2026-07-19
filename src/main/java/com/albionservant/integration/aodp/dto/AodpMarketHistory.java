package com.albionservant.integration.aodp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AodpMarketHistory(
        @JsonProperty("ItemAmount") long itemAmount,
        @JsonProperty("SilverAmount") long silverAmount,
        @JsonProperty("Timestamp") long timestamp
) {
}
