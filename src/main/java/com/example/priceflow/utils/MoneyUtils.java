package com.example.priceflow.utils;

import java.math.BigDecimal;

public final class MoneyUtils {

    private MoneyUtils() {}

    public static BigDecimal parsePrice(String fraction, String cents) {

        String fractionFormat = fraction.replace(".", "").replace(",", ".");

        if (cents == null || cents.isBlank()) {
            cents = "00";
        }

        String priceFormat = fractionFormat + '.' + cents;

        return new BigDecimal(priceFormat);

    }

}
