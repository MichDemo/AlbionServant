package com.albionservant.integration.aodp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AodpMarketUpload(
        @JsonProperty("Orders") List<AodpMarketOrder> orders
) {
    public AodpMarketUpload {
        orders = orders == null ? List.of() : List.copyOf(orders);
    }
}
