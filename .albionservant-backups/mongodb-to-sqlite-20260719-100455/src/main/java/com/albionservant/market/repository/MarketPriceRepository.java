package com.albionservant.market.repository;

import com.albionservant.market.model.MarketPriceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MarketPriceRepository extends MongoRepository<MarketPriceDocument, String> {
    List<MarketPriceDocument> findByServerAndItemId(String server, String itemId);
}
