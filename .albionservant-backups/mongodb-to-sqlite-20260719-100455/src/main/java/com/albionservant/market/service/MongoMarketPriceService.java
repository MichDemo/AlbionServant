package com.albionservant.market.service;

import com.albionservant.market.model.MarketPriceDocument;
import com.albionservant.market.repository.MarketPriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MongoMarketPriceService implements MarketPriceService {

    private final MarketPriceRepository repository;

    public MongoMarketPriceService(MarketPriceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<MarketPriceView> getLatestPrice(
            String server,
            String itemId,
            String locationId,
            int qualityLevel,
            int enchantmentLevel
    ) {
        String id = MarketPriceDocument.makeId(server, itemId, locationId, qualityLevel, enchantmentLevel);
        return repository.findById(id).map(this::toView);
    }

    @Override
    public List<MarketPriceView> getLatestPrices(String server, String itemId) {
        return repository.findByServerAndItemId(server, itemId)
                .stream()
                .map(this::toView)
                .toList();
    }

    private MarketPriceView toView(MarketPriceDocument document) {
        return new MarketPriceView(
                document.getServer(),
                document.getItemId(),
                document.getLocationId(),
                document.getQualityLevel(),
                document.getEnchantmentLevel(),
                document.getMinSellPrice(),
                document.getMaxBuyPrice(),
                document.getObservedAt()
        );
    }
}
