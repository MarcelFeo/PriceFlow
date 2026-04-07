package com.example.priceflow.infrastructure.scraper;

import java.io.IOException;
import java.math.BigDecimal;

public interface PriceScraper {

    boolean supports(String url);
    BigDecimal scrape(String url) throws IOException;

}
