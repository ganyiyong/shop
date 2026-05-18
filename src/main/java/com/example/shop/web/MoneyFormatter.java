package com.example.shop.web;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component("money")
public class MoneyFormatter {
    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("￥#,##0.00"));

    public String format(Number value) {
        return FORMAT.get().format(value == null ? 0D : value.doubleValue());
    }
}
