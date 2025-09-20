package com.example.buspricing.controller;

import com.example.buspricing.controller.request.BusTerminalRequest;
import com.example.buspricing.domain.BusTerminal;
import com.example.buspricing.repository.BusTerminalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BusTerminalController.class)
public class BusTerminalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusTerminalRepository repository;

    @Test
    public void createBusTerminal_ShouldReturn201_WhenValidRequest() throws Exception {
        BusTerminalRequest request = new BusTerminalRequest("Central Terminal", BigDecimal.valueOf(50.00));
        BusTerminal savedTerminal = BusTerminal.builder()
                .terminalName("Central Terminal")
                .basePrice(BigDecimal.valueOf(50.00))
                .build();

        Mockito.when(repository.existsById(eq("Central Terminal"))).thenReturn(false);
        Mockito.when(repository.save(any(BusTerminal.class))).thenReturn(savedTerminal);

        mockMvc.perform(post("/api/bus-terminals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.terminalName").value("Central Terminal"))
                .andExpect(jsonPath("$.basePrice").value(50.00));
    }

    @Test
    void createBusTerminal_ShouldReturn409_WhenTerminalAlreadyExists() throws Exception {
        BusTerminalRequest request = BusTerminalRequest.builder()
                .terminalName("Central")
                .basePrice(new java.math.BigDecimal("12.50"))
                .build();

        Mockito.when(repository.existsById("Central")).thenReturn(true);

        mockMvc.perform(post("/api/bus-terminals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }


    @Test
    public void createBusTerminal_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        BusTerminalRequest request = new BusTerminalRequest("", BigDecimal.valueOf(-5.00));

        mockMvc.perform(post("/api/bus-terminals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'terminalName')].message").value("Terminal name must not be blank"))
                .andExpect(jsonPath("$.errors[?(@.field == 'basePrice')].message").value("Base price must be greater than or equal to 0.00"));
    }
}