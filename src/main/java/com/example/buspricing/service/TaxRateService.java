package com.example.buspricing.service;

import java.time.LocalDate;
import java.util.List;

public interface TaxRateService {
    List<TaxRate> getTaxRates(LocalDate purchaseDate);

}
