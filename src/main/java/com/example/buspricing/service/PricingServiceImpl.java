package com.example.buspricing.service;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.request.Passenger;
import com.example.buspricing.controller.response.DraftPriceResponse;
import com.example.buspricing.controller.response.ItemPrice;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingServiceImpl implements PricingService {

    private static final BigDecimal CHILD_DISCOUNT = new BigDecimal("0.50"); // 50%
    private static final BigDecimal LUGGAGE_RATE = new BigDecimal("0.30");   // 30%
    private static final RoundingMode ROUND = RoundingMode.HALF_UP;

    private final BasePriceService basePriceService;
    private final TaxRateService taxRateService;

    public PricingServiceImpl(BasePriceService basePriceService, TaxRateService taxRateService) {
        this.basePriceService = basePriceService;
        this.taxRateService = taxRateService;
    }

    @Override
    public DraftPriceResponse calculateDraftPrice(DraftPriceRequest request) {
        BigDecimal base = basePriceService.getBasePrice(request.getRoute());

        BigDecimal taxPercentSum = taxRateService.getTaxRates(request.getDate()).stream()
                .map(TaxRate::ratePercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxMultiplier = BigDecimal.ONE.add(taxPercentSum.divide(new BigDecimal("100")));

        List<ItemPrice> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int passengerCount = 1;
        for (Passenger p : request.getPassengers()) {
            BigDecimal passengerPreTax;
            String passengerDesc;
            String priceDesc;
            if (p.getType() == Passenger.Type.ADULT) {
                passengerPreTax = base;
                passengerDesc = String.format("Passenger %d (%s)", passengerCount, "Adult");
                priceDesc = String.format("Adult (%.2f EUR + %s%%)",
                        base,
                        taxPercentSum.stripTrailingZeros().toPlainString());
            } else {
                passengerPreTax = base.multiply(CHILD_DISCOUNT);
                passengerDesc = String.format("Passenger %d (%s)", passengerCount, "Child");
                priceDesc = String.format("Child (%.2f EUR x %s%% + %s%%)",
                        base,
                        CHILD_DISCOUNT.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString(),
                        taxPercentSum);
            }
            BigDecimal passengerWithTax = passengerPreTax.multiply(taxMultiplier).setScale(2, ROUND);
            priceDesc = priceDesc + " = " + passengerWithTax.toPlainString() + " EUR";
            items.add(ItemPrice.builder()
                    .description(passengerDesc)
                    .price(passengerWithTax)
                    .priceDescription(priceDesc)
                    .build());
            total = total.add(passengerWithTax);

            // Luggage
            if (p.getLuggageCount() > 0) {
                BigDecimal luggageUnit = base.multiply(LUGGAGE_RATE);
                BigDecimal luggagePreTax = luggageUnit.multiply(BigDecimal.valueOf(p.getLuggageCount()));
                BigDecimal luggageWithTax = luggagePreTax.multiply(taxMultiplier).setScale(2, ROUND);
                items.add(ItemPrice.builder()
                        .description(String.format("Luggage for passenger %d (%s)",
                                passengerCount,
                                luggageDescription(p.getLuggageCount())))
                        .price(luggageWithTax)
                        .priceDescription(String.format("%s (%d x %.2f EUR x %s%% + %s%%) = %.2f EUR",
                                luggageDescription(p.getLuggageCount()),
                                p.getLuggageCount(),
                                base,
                                LUGGAGE_RATE.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString(),
                                taxPercentSum,
                                luggageWithTax
                                ))
                        .build());
                total = total.add(luggageWithTax);
            }
            passengerCount++;
        }

        return DraftPriceResponse.builder()
                .items(items)
                .total(total.setScale(2, ROUND))
                .totalDescription(String.format("%.2f EUR", total))
                .build();
    }

    private String luggageDescription(int count) {
        if (count == 1) return "One bag";
        if (count == 2) return "Two bags";
        return count + " bags";
    }
}
