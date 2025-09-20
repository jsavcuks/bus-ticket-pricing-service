package com.example.buspricing.model;

import java.math.BigDecimal;

public record TaxRate(String name, BigDecimal ratePercent) {
}
