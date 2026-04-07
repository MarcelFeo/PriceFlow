package com.example.priceflow.infrastructure.scraper;

import com.example.priceflow.utils.MoneyUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
public class MercadoLivreScraper implements PriceScraper {

    @Override
    public boolean supports(String url) {
        return url.contains("mercadolivre.com");
    }

    @Override
    public BigDecimal scrape(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        String priceTextFraction = doc.select(".andes-money-amount__fraction").first().text();
        String priceTextCents = doc.select(".andes-money-amount__cents").first().text();

        BigDecimal priceText = MoneyUtils.parsePrice(priceTextFraction, priceTextCents);

        return priceText;
    }

}
