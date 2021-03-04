package com.creditville.notifications.utils;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Chuks on 03/04/2021.
 */
@Service
public class CurrencyUtil {
    public String getFormattedCurrency(BigDecimal currency) {
        return NumberFormat
                .getCurrencyInstance(new Locale("en", "NG"))
                .format(currency);
    }
}
