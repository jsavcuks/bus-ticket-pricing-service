package com.example.buspricing.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftPriceResponse {
    private List<ItemPrice> items;
    private BigDecimal totalPrice;
    private String totalPriceDescription;
}
