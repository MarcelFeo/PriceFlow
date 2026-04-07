package com.example.priceflow.service;

import com.example.priceflow.infrastructure.scraper.PriceScraper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ScraperService {

    private final List<PriceScraper> scrapers;

    public ScraperService(List<PriceScraper> scrapers) {
        this.scrapers = scrapers;
    }

    public BigDecimal getPriceFromUrl(String url) throws Exception {
        PriceScraper scraper = scrapers.stream()
                .filter(s -> s.supports(url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Loja não suportada"));

        return scraper.scrape(url);
    }
}
