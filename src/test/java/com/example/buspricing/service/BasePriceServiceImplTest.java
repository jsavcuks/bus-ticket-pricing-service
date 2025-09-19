package com.example.buspricing.service;

import com.example.buspricing.domain.BusTerminal;
import com.example.buspricing.repository.BusTerminalRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasePriceServiceImplTest {

    @Test
    void returns_base_price_when_terminal_exists() {
        BusTerminalRepository repo = mock(BusTerminalRepository.class);
        when(repo.findById("Vilnius, Lithuania"))
                .thenReturn(Optional.of(BusTerminal.builder()
                        .terminalName("Vilnius, Lithuania")
                        .basePrice(new BigDecimal("10.00"))
                        .build()));

        BasePriceServiceImpl service = new BasePriceServiceImpl(repo);

        assertEquals(new BigDecimal("10.00"), service.getBasePrice("Vilnius, Lithuania"));
        verify(repo).findById("Vilnius, Lithuania");
    }

    @Test
    void throws_when_terminal_missing() {
        BusTerminalRepository repo = mock(BusTerminalRepository.class);
        when(repo.findById("Unknown")).thenReturn(Optional.empty());

        BasePriceServiceImpl service = new BasePriceServiceImpl(repo);

        BasePriceServiceImpl.RouteNotFoundException ex =
                assertThrows(BasePriceServiceImpl.RouteNotFoundException.class,
                        () -> service.getBasePrice("Unknown"));
        assertTrue(ex.getMessage().contains("Unknown route"));
    }
}
