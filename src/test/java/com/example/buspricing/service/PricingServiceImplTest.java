package com.example.buspricing.service;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.request.Passenger;
import com.example.buspricing.controller.response.DraftPriceResponse;
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

    @Test
    void one_child_with_luggage_is_priced_correctly() {
        String route = "Any";
        LocalDate date = LocalDate.now();

        when(basePriceService.getBasePrice(route)).thenReturn(new BigDecimal("20.00"));
        when(taxRateService.getTaxRates(date)).thenReturn(List.of(
                new TaxRate("VAT", new BigDecimal("10"))
        ));

        DraftPriceRequest request = DraftPriceRequest.builder()
                .route(route)
                .date(date)
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.CHILD).luggageCount(2).build()
                ))
                .build();

        DraftPriceResponse response = pricingService.calculateDraftPrice(request);

        assertEquals(2, response.getItems().size());
        assertEquals(new BigDecimal("11.00"), response.getItems().get(0).getPrice()); // Child price
        assertEquals(new BigDecimal("13.20"), response.getItems().get(1).getPrice()); // Luggage price
        assertEquals(new BigDecimal("24.20"), response.getTotal());
    }

    @Test
    void multiple_passengers_are_priced_correctly() {
        String route = "Any";
        LocalDate date = LocalDate.now();

        when(basePriceService.getBasePrice(route)).thenReturn(new BigDecimal("15.00"));
        when(taxRateService.getTaxRates(date)).thenReturn(List.of(
                new TaxRate("VAT", new BigDecimal("20"))
        ));

        DraftPriceRequest request = DraftPriceRequest.builder()
                .route(route)
                .date(date)
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(0).build(),
                        Passenger.builder().type(Passenger.Type.CHILD).luggageCount(1).build()
                ))
                .build();

        DraftPriceResponse response = pricingService.calculateDraftPrice(request);

        assertEquals(3, response.getItems().size());
        assertEquals(new BigDecimal("18.00"), response.getItems().get(0).getPrice()); // Adult price
        assertEquals(new BigDecimal("9.00"), response.getItems().get(1).getPrice());  // Child price
        assertEquals(new BigDecimal("5.40"), response.getItems().get(2).getPrice()); // Luggage price
        assertEquals(new BigDecimal("32.40"), response.getTotal());
    }

    @Test
    void luggage_with_multiple_bags_is_priced_correctly() {
        String route = "Any";
        LocalDate date = LocalDate.now();

        when(basePriceService.getBasePrice(route)).thenReturn(new BigDecimal("30.00"));
        when(taxRateService.getTaxRates(date)).thenReturn(List.of(
                new TaxRate("VAT", new BigDecimal("5"))
        ));

        DraftPriceRequest request = DraftPriceRequest.builder()
                .route(route)
                .date(date)
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(3).build()
                ))
                .build();

        DraftPriceResponse response = pricingService.calculateDraftPrice(request);

        assertEquals(2, response.getItems().size());
        assertEquals(new BigDecimal("31.50"), response.getItems().get(0).getPrice()); // Adult price
        assertEquals(new BigDecimal("28.35"), response.getItems().get(1).getPrice()); // Luggage price
        assertEquals(new BigDecimal("59.85"), response.getTotal());
    }
}
