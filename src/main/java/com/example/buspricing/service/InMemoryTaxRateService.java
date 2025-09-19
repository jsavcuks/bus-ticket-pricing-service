package com.example.buspricing.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * In-memory tax rates provider. For now returns VAT 21% for any date.
 * Extend later if needed to vary by date.
 */
@Service
public class InMemoryTaxRateService implements TaxRateService {

    @Override
    public List<TaxRate> getTaxRates(LocalDate purchaseDate) {
        return List.of(new TaxRate("VAT", new BigDecimal("21")));
    }
}

