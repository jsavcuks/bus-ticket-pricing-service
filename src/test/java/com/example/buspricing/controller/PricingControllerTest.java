package com.example.buspricing.controller;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.request.Passenger;
import com.example.buspricing.controller.response.DraftPriceResponse;
import com.example.buspricing.controller.response.ItemPrice;
import com.example.buspricing.exception.ValidationErrorException;
import com.example.buspricing.service.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PricingController.class)
class PricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PricingService pricingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void post_draft_returns_itemized_prices_and_total() throws Exception {
        DraftPriceResponse mocked = DraftPriceResponse.builder()
                .items(List.of(
                        ItemPrice.builder().description("Adult").price(new BigDecimal("12.10")).build(),
                        ItemPrice.builder().description("Two bags").price(new BigDecimal("7.26")).build(),
                        ItemPrice.builder().description("Child").price(new BigDecimal("6.05")).build(),
                        ItemPrice.builder().description("One bag").price(new BigDecimal("3.63")).build()
                ))
                .total(new BigDecimal("29.04"))
                .build();

        Mockito.when(pricingService.calculateDraftPrice(Mockito.any(DraftPriceRequest.class)))
                .thenReturn(mocked);

        String body = objectMapper.writeValueAsString(DraftPriceRequest.builder()
                .route("Vilnius, Lithuania")
                .date(LocalDate.of(2025, 1, 1))
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(2).build(),
                        Passenger.builder().type(Passenger.Type.CHILD).luggageCount(1).build()
                ))
                .build());

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.items[0].description", is("Adult")))
                .andExpect(jsonPath("$.items[0].price", is(12.10)))
                .andExpect(jsonPath("$.items[1].description", is("Two bags")))
                .andExpect(jsonPath("$.items[1].price", is(7.26)))
                .andExpect(jsonPath("$.items[2].description", is("Child")))
                .andExpect(jsonPath("$.items[2].price", is(6.05)))
                .andExpect(jsonPath("$.items[3].description", is("One bag")))
                .andExpect(jsonPath("$.items[3].price", is(3.63)))
                .andExpect(jsonPath("$.total", is(29.04)));
    }

    @Test
    void post_draft_returns_404_when_route_not_found() throws Exception {
        Mockito.when(pricingService.calculateDraftPrice(Mockito.any(DraftPriceRequest.class)))
                .thenThrow(new ValidationErrorException(
                        "route",
                        "Unknown route: Nowhere",
                        "Nowhere",
                        HttpStatus.NOT_FOUND
                ));

        String body = objectMapper.writeValueAsString(DraftPriceRequest.builder()
                .route("Nowhere")
                .date(LocalDate.of(2025, 1, 1))
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(0).build()
                ))
                .build());

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.path").value("/api/pricing/draft"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors[0].field").value("route"))
                .andExpect(jsonPath("$.errors[0].message").value("Unknown route: Nowhere"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").value("Nowhere"));

        Mockito.verify(pricingService, Mockito.times(1))
                .calculateDraftPrice(Mockito.any(DraftPriceRequest.class));
        Mockito.verifyNoMoreInteractions(pricingService);
    }

    // ... existing code ...

    @Test
    void post_draft_returns_400_when_luggage_count_negative() throws Exception {
        String body = objectMapper.writeValueAsString(DraftPriceRequest.builder()
                .route("Vilnius, Lithuania")
                .date(LocalDate.of(2025, 1, 1))
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(-1).build()
                ))
                .build());

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_draft_returns_400_when_luggage_count_unreasonably_high() throws Exception {
        String body = objectMapper.writeValueAsString(DraftPriceRequest.builder()
                .route("Vilnius, Lithuania")
                .date(LocalDate.of(2025, 1, 1))
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(101).build()
                ))
                .build());

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_draft_returns_400_when_route_blank() throws Exception {
        String body = objectMapper.writeValueAsString(DraftPriceRequest.builder()
                .route(" ")
                .date(LocalDate.of(2025, 1, 1))
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(0).build()
                ))
                .build());

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ... existing code ...
    @Test
    void post_draft_returns_400_when_date_missing() throws Exception {
        // date is null
        String body = objectMapper.writeValueAsString(DraftPriceRequest.builder()
                .route("Vilnius, Lithuania")
                .date(null)
                .passengers(List.of(
                        Passenger.builder().type(Passenger.Type.ADULT).luggageCount(0).build()
                ))
                .build());

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_draft_returns_400_when_passengers_empty() throws Exception {
        String body = objectMapper.writeValueAsString(DraftPriceRequest.builder()
                .route("Vilnius, Lithuania")
                .date(LocalDate.of(2025, 1, 1))
                .passengers(List.of())
                .build());

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

}
