package com.example.buspricing.service;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.request.Passenger;
import com.example.buspricing.controller.response.DraftPriceResponse;
import com.example.buspricing.controller.response.ItemPrice;
import com.example.buspricing.model.TaxRate;
import com.example.buspricing.util.PriceDescriptionFormatter;
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
    private final PriceDescriptionFormatter descriptionFormatter;

    public PricingServiceImpl(BasePriceService basePriceService,
                              TaxRateService taxRateService,
                              PriceDescriptionFormatter descriptionFormatter) {
        this.basePriceService = basePriceService;
        this.taxRateService = taxRateService;
        this.descriptionFormatter = descriptionFormatter;
    }

    @Override
    public DraftPriceResponse calculateDraftPrice(DraftPriceRequest request) {
        BigDecimal base = basePriceService.getBasePrice(request.getRoute());

        BigDecimal taxPercentSum = taxRateService.getTaxRates().stream()
                .map(TaxRate::ratePercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxMultiplier = BigDecimal.ONE.add(taxPercentSum.divide(new BigDecimal("100")));

        List<ItemPrice> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        int passengerIndex = 1;
        for (Passenger passenger : request.getPassengers()) {
            // === Passenger ===
            BigDecimal passengerPreTax = passenger.getType() == Passenger.Type.ADULT
                    ? base
                    : base.multiply(CHILD_DISCOUNT);

            BigDecimal passengerWithTax = passengerPreTax.multiply(taxMultiplier).setScale(2, ROUND);

            ItemPrice passengerItem = ItemPrice.builder()
                    .description(descriptionFormatter.passengerDescription(passengerIndex, passenger))
                    .price(passengerWithTax)
                    .priceDescription(descriptionFormatter.passengerPriceDescription(passenger, base, passengerWithTax, taxPercentSum))
                    .build();

            items.add(passengerItem);
            total = total.add(passengerWithTax);

            // === Luggage ===
            if (passenger.getLuggageCount() > 0) {
                BigDecimal luggageUnit = base.multiply(LUGGAGE_RATE);
                BigDecimal luggagePreTax = luggageUnit.multiply(BigDecimal.valueOf(passenger.getLuggageCount()));
                BigDecimal luggageWithTax = luggagePreTax.multiply(taxMultiplier).setScale(2, ROUND);

                ItemPrice luggageItem = ItemPrice.builder()
                        .description(descriptionFormatter.luggageDescription(passengerIndex, passenger.getLuggageCount()))
                        .price(luggageWithTax)
                        .priceDescription(descriptionFormatter.luggagePriceDescription(passenger.getLuggageCount(), base, luggageWithTax, taxPercentSum))
                        .build();

                items.add(luggageItem);
                total = total.add(luggageWithTax);
            }

            passengerIndex++;
        }

        return DraftPriceResponse.builder()
                .items(items)
                .totalPrice(total.setScale(2, ROUND))
                .totalPriceDescription(descriptionFormatter.totalDescription(total))
                .build();
    }
}