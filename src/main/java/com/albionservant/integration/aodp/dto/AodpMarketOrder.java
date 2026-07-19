package com.albionservant.integration.aodp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AodpMarketOrder(
        @JsonProperty("Id") long id,
        @JsonProperty("ItemTypeId") String itemId,
        @JsonProperty("ItemGroupTypeId") String itemGroupTypeId,
        @JsonProperty("LocationId") String locationId,
        @JsonProperty("QualityLevel") int qualityLevel,
        @JsonProperty("EnchantmentLevel") int enchantmentLevel,
        @JsonProperty("UnitPriceSilver") long unitPriceSilver,
        @JsonProperty("Amount") long amount,
        @JsonProperty("AuctionType") String auctionType,
        @JsonProperty("Expires") String expires
) {
}
