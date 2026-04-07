package com.example.priceflow.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyUtilsTest {

    @Test
    void shouldParsePriceWithCents() {
        BigDecimal result = MoneyUtils.parsePrice("1990", "75");
        assertEquals(new BigDecimal("1990.75"), result);
    }

    @Test
    void shouldParsePriceWithoutCents() {
        BigDecimal result = MoneyUtils.parsePrice("999", null);
        assertEquals(new BigDecimal("999.00"), result);
    }

    @Test
    void shouldRemoveThousandsSeparator() {
        BigDecimal result = MoneyUtils.parsePrice("12.345", "99");
        assertEquals(new BigDecimal("12345.99"), result);
    }
}
