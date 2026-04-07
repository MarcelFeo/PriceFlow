package com.example.priceflow.infrastructure;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AmazonScraperTest {

    @Test
    void shouldExtractPriceFromHtml() throws Exception {

        String fakeHtml = """
            <html>
                <span id="priceblock_ourprice">
                    <span class="a-price-whole">2.999</span>
                </span>
                <span class="a-price-fraction">90</span>
            </html>
        """;

        Document fakeDocument = Jsoup.parse(fakeHtml);
        Connection mockConnection = mock(Connection.class);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {

            jsoupMock.when(() -> Jsoup.connect(anyString()))
                    .thenReturn(mockConnection);

            when(mockConnection.get()).thenReturn(fakeDocument);

            AmazonScraper scraper = new AmazonScraper();

            BigDecimal price = scraper.scrape("https://amazon.com/product");

            assertEquals(new BigDecimal("2999.90"), price);
        }
    }
}