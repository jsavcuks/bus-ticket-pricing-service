package com.example.buspricing.service;

import java.math.BigDecimal;

public record TaxRate(String name, BigDecimal ratePercent) {
}
