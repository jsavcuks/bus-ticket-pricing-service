package com.example.buspricing.controller;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.request.Passenger;
import com.example.buspricing.controller.response.DraftPriceResponse;
import com.example.buspricing.service.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PricingController.class)
public class PricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PricingService pricingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testDraft_ValidRequest() throws Exception {
        DraftPriceRequest request = DraftPriceRequest.builder()
                .route("Route A")
                .passengers(Collections.singletonList(
                        Passenger.builder()
                                .type(Passenger.Type.ADULT)
                                .luggageCount(0)
                                .build()
                ))
                .build();

        DraftPriceResponse response = DraftPriceResponse.builder().build();

        Mockito.when(pricingService.calculateDraftPrice(any(DraftPriceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void testDraft_InvalidRequest_MissingFields() throws Exception {
        DraftPriceRequest request = DraftPriceRequest.builder().build();

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDraft_InvalidRequest_PassengersEmpty() throws Exception {
        DraftPriceRequest request = DraftPriceRequest.builder()
                .route("Route A")
                .passengers(Collections.emptyList())
                .build();

        mockMvc.perform(post("/api/pricing/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}