package com.example.buspricing.service;

import java.math.BigDecimal;

public interface BasePriceService {

    BigDecimal getBasePrice(String route);

}
