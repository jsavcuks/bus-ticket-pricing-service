package com.example.buspricing.service;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.request.Passenger;
import com.example.buspricing.controller.response.DraftPriceResponse;
import com.example.buspricing.model.TaxRate;
import com.example.buspricing.util.PriceDescriptionFormatter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class PricingServiceImplTest {

    private PriceDescriptionFormatter stubFormatter() {
        PriceDescriptionFormatter formatter = mock(PriceDescriptionFormatter.class);
        when(formatter.passengerDescription(anyInt(), any())).thenReturn("Passenger desc");
        when(formatter.passengerPriceDescription(any(), any(), any(), any())).thenReturn("Passenger price desc");
        when(formatter.luggageDescription(anyInt(), anyInt())).thenReturn("Luggage desc");
        when(formatter.luggagePriceDescription(anyInt(), any(), any(), any())).thenReturn("Luggage price desc");
        when(formatter.totalDescription(any())).thenReturn("Total price desc");
        return formatter;
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), "Expected: " + expected + " but was: " + actual);
    }

    @Test
    void returns_correct_total_for_adult_without_luggage() {
        BasePriceService basePriceService = mock(BasePriceService.class);
        TaxRateService taxRateService = mock(TaxRateService.class);
        PriceDescriptionFormatter descriptionFormatter = stubFormatter();

        LocalDate date = LocalDate.of(2025, 9, 20);
        DraftPriceRequest request = DraftPriceRequest.builder()
                .route("R1")
                .passengers(List.of(Passenger.builder().type(Passenger.Type.ADULT).luggageCount(0).build()))
                .build();

        when(basePriceService.getBasePrice("R1")).thenReturn(new BigDecimal("100.00"));
        when(taxRateService.getTaxRates()).thenReturn(List.of(new TaxRate("VAT", new BigDecimal("10"))));

        PricingServiceImpl service = new PricingServiceImpl(basePriceService, taxRateService, descriptionFormatter);

        DraftPriceResponse response = service.calculateDraftPrice(request);

        assertEquals(1, response.getItems().size());
        assertBigDecimalEquals(new BigDecimal("110.00"), response.getTotalPrice());
        assertEquals("Total price desc", response.getTotalPriceDescription());
        assertBigDecimalEquals(new BigDecimal("110.00"), response.getItems().get(0).getPrice());

        verify(basePriceService).getBasePrice("R1");
        verify(taxRateService).getTaxRates();
    }

    @Test
    void returns_correct_total_for_child_with_luggage() {
        BasePriceService basePriceService = mock(BasePriceService.class);
        TaxRateService taxRateService = mock(TaxRateService.class);
        PriceDescriptionFormatter descriptionFormatter = stubFormatter();

        LocalDate date = LocalDate.of(2025, 9, 21);
        DraftPriceRequest request = DraftPriceRequest.builder()
                .route("R2")
                .passengers(List.of(Passenger.builder().type(Passenger.Type.CHILD).luggageCount(2).build()))
                .build();

        when(basePriceService.getBasePrice("R2")).thenReturn(new BigDecimal("100.00"));
        when(taxRateService.getTaxRates()).thenReturn(List.of(new TaxRate("VAT", new BigDecimal("20"))));

        PricingServiceImpl service = new PricingServiceImpl(basePriceService, taxRateService, descriptionFormatter);

        DraftPriceResponse response = service.calculateDraftPrice(request);

        // Child: 100 * 0.50 * 1.20 = 60.00
        // Luggage: (100 * 0.30) * 2 * 1.20 = 72.00
        // Total: 132.00
        assertEquals(2, response.getItems().size());
        assertBigDecimalEquals(new BigDecimal("132.00"), response.getTotalPrice());
        assertBigDecimalEquals(new BigDecimal("60.00"), response.getItems().get(0).getPrice());
        assertBigDecimalEquals(new BigDecimal("72.00"), response.getItems().get(1).getPrice());
    }

    @Test
    void returns_zero_total_and_no_items_when_no_passengers() {
        BasePriceService basePriceService = mock(BasePriceService.class);
        TaxRateService taxRateService = mock(TaxRateService.class);
        PriceDescriptionFormatter descriptionFormatter = stubFormatter();

        LocalDate date = LocalDate.of(2025, 9, 23);
        DraftPriceRequest request = DraftPriceRequest.builder()
                .route("R4")
                .passengers(List.of())
                .build();

        when(basePriceService.getBasePrice("R4")).thenReturn(new BigDecimal("77.77"));
        when(taxRateService.getTaxRates()).thenReturn(List.of(new TaxRate("Zero", BigDecimal.ZERO)));

        PricingServiceImpl service = new PricingServiceImpl(basePriceService, taxRateService, descriptionFormatter);

        DraftPriceResponse response = service.calculateDraftPrice(request);

        assertEquals(0, response.getItems().size());
        assertBigDecimalEquals(new BigDecimal("0.00"), response.getTotalPrice());
        assertEquals("Total price desc", response.getTotalPriceDescription());
    }

    @Test
    void calculates_correct_totals_for_multiple_passengers() {
        BasePriceService basePriceService = mock(BasePriceService.class);
        TaxRateService taxRateService = mock(TaxRateService.class);
        PriceDescriptionFormatter descriptionFormatter = stubFormatter();

        LocalDate date = LocalDate.of(2025, 9, 24);
        DraftPriceRequest request = DraftPriceRequest.builder()
                .route("R5")
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(1).build(),
                        Passenger.builder().type(Passenger.Type.CHILD).luggageCount(2).build()
                ))
                .build();

        when(basePriceService.getBasePrice("R5")).thenReturn(new BigDecimal("80.00"));
        when(taxRateService.getTaxRates()).thenReturn(List.of(
                new TaxRate("VAT", new BigDecimal("12")),
                new TaxRate("City", new BigDecimal("3"))
        ));

        PricingServiceImpl service = new PricingServiceImpl(basePriceService, taxRateService, descriptionFormatter);

        DraftPriceResponse response = service.calculateDraftPrice(request);

        // Taxes: 12% + 3% = 15% -> multiplier 1.15
        // Adult: 80 * 1.15 = 92.00
        // Adult luggage: (80 * 0.30) * 1 * 1.15 = 27.60
        // Child: (80 * 0.50) * 1.15 = 46.00
        // Child luggage: (80 * 0.30) * 2 * 1.15 = 55.20
        // Total: 92.00 + 27.60 + 46.00 + 55.20 = 220.80
        assertEquals(4, response.getItems().size());
        assertEquals(new BigDecimal("220.80"), response.getTotalPrice());

        assertEquals(new BigDecimal("92.00"), response.getItems().get(0).getPrice());  // adult
        assertEquals(new BigDecimal("27.60"), response.getItems().get(1).getPrice());  // adult luggage
        assertEquals(new BigDecimal("46.00"), response.getItems().get(2).getPrice());  // child
        assertEquals(new BigDecimal("55.20"), response.getItems().get(3).getPrice());  // child luggage

        verify(basePriceService).getBasePrice("R5");
        verify(taxRateService).getTaxRates();
    }
}