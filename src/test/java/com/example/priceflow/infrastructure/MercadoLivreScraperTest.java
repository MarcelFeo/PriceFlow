package com.example.priceflow.infrastructure;

import com.example.priceflow.infrastructure.scraper.MercadoLivreScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MercadoLivreScraperTest {

    @Test
    void shouldExtractPriceFromHtml() throws Exception {

        String fakeHtml = """
            <html>
                <span class="andes-money-amount__fraction">1.234</span>
                <span class="andes-money-amount__cents">56</span>
            </html>
        """;

        Document fakeDocument = org.jsoup.Jsoup.parse(fakeHtml);
        Connection mockConnection = mock(Connection.class);

        try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {

            jsoupMock.when(() -> Jsoup.connect(anyString()))
                    .thenReturn(mockConnection);

            when(mockConnection.get()).thenReturn(fakeDocument);

            MercadoLivreScraper scraper = new MercadoLivreScraper();

            BigDecimal price = scraper.scrape("https://mercadolivre.com/product");

            assertEquals(new BigDecimal("1234.56"), price);
        }
    }
}