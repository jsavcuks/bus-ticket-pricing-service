package com.example.buspricing.service;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.request.Passenger;
import com.example.buspricing.controller.response.DraftPriceResponse;
import com.example.buspricing.controller.response.ItemPrice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceImplTest {

    @Mock
    private BasePriceService basePriceService;

    @Mock
    private TaxRateService taxRateService;

    @InjectMocks
    private PricingServiceImpl pricingService;

    @Test
    void acceptance_case_adult_with_two_bags_child_with_one_bag_vat_21() {
        String route = "Vilnius, Lithuania";
        LocalDate date = LocalDate.of(2025, 1, 1);

        when(basePriceService.getBasePrice(route)).thenReturn(new BigDecimal("10.00"));
        when(taxRateService.getTaxRates(date)).thenReturn(List.of(
                new TaxRate("VAT", new BigDecimal("21"))
        ));

        DraftPriceRequest request = DraftPriceRequest.builder()
                .route(route)
                .date(date)
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(2).build(),
                        Passenger.builder().type(Passenger.Type.CHILD).luggageCount(1).build()
                ))
                .build();

        DraftPriceResponse response = pricingService.calculateDraftPrice(request);

        List<ItemPrice> items = response.getItems();
        assertEquals(4, items.size());

        assertEquals("Adult", items.get(0).getDescription());
        assertEquals(new BigDecimal("12.10"), items.get(0).getPrice());

        assertEquals("Two bags", items.get(1).getDescription());
        assertEquals(new BigDecimal("7.26"), items.get(1).getPrice());

        assertEquals("Child", items.get(2).getDescription());
        assertEquals(new BigDecimal("6.05"), items.get(2).getPrice());

        assertEquals("One bag", items.get(3).getDescription());
        assertEquals(new BigDecimal("3.63"), items.get(3).getPrice());

        assertEquals(new BigDecimal("29.04"), response.getTotal());
    }

    @Test
    void multiple_taxes_are_summed() {
        String route = "Any";
        LocalDate date = LocalDate.now();

        when(basePriceService.getBasePrice(route)).thenReturn(new BigDecimal("10.00"));
        when(taxRateService.getTaxRates(date)).thenReturn(List.of(
                new TaxRate("VAT", new BigDecimal("21")),
                new TaxRate("City", new BigDecimal("2"))
        ));

        DraftPriceRequest request = DraftPriceRequest.builder()
                .route(route)
                .date(date)
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(0).build()
                ))
                .build();

        DraftPriceResponse response = pricingService.calculateDraftPrice(request);

        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("12.30"), response.getItems().get(0).getPrice());
        assertEquals(new BigDecimal("12.30"), response.getTotal());
    }

    @Test
    void empty_passenger_list_returns_zero_total_and_no_items() {
        String route = "Any";
        LocalDate date = LocalDate.now();

        when(basePriceService.getBasePrice(route)).thenReturn(new BigDecimal("10.00"));
        when(taxRateService.getTaxRates(date)).thenReturn(List.of(
                new TaxRate("VAT", new BigDecimal("21"))
        ));

        DraftPriceRequest request = DraftPriceRequest.builder()
                .route(route)
                .date(date)
                .passengers(List.of())
                .build();

        DraftPriceResponse response = pricingService.calculateDraftPrice(request);

        assertEquals(0, response.getItems().size());
        assertEquals(new BigDecimal("0.00"), response.getTotal());
    }
}
