package com.example.buspricing.integration;


import com.example.buspricing.controller.request.BusTerminalRequest;
import com.example.buspricing.domain.BusTerminal;
import com.example.buspricing.repository.BusTerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BusTerminalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BusTerminalRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        repository.deleteAll(); // clearing the database before each test
    }

    @Test
    void createBusTerminal_success() throws Exception {
        BusTerminalRequest request = new BusTerminalRequest();
        request.setTerminalName("Central");
        request.setBasePrice(BigDecimal.valueOf(10.5));

        mockMvc.perform(post("/api/bus-terminals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.terminalName").value("Central"))
                .andExpect(jsonPath("$.basePrice").value(BigDecimal.valueOf(10.5)));
    }

    @Test
    void createBusTerminal_conflict() throws Exception {
        BusTerminal existing = BusTerminal.builder()
                .terminalName("Central")
                .basePrice(BigDecimal.valueOf(5.0))
                .build();
        repository.save(existing);

        BusTerminalRequest request = new BusTerminalRequest();
        request.setTerminalName("Central");
        request.setBasePrice(BigDecimal.valueOf(10.5));

        mockMvc.perform(post("/api/bus-terminals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}