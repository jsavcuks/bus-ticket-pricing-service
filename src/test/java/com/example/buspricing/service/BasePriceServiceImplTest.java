package com.example.buspricing.service;

import com.example.buspricing.domain.BusTerminal;
import com.example.buspricing.exception.ValidationErrorException;
import com.example.buspricing.repository.BusTerminalRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    void throws_exception_when_terminal_does_not_exist() {
        BusTerminalRepository repo = mock(BusTerminalRepository.class);
        when(repo.findById("NonExistentRoute")).thenReturn(Optional.empty());

        BasePriceServiceImpl service = new BasePriceServiceImpl(repo);

        ValidationErrorException exception = assertThrows(ValidationErrorException.class,
                () -> service.getBasePrice("NonExistentRoute"));

        assertEquals("route", exception.getField());
        assertEquals("route not found", exception.getMessage());
        assertEquals("NonExistentRoute", exception.getRejectedValue());
        assertEquals(NOT_FOUND, exception.getHttpStatus());
        verify(repo).findById("NonExistentRoute");
    }

    @Test
    void throws_exception_when_route_is_null() {
        BusTerminalRepository repo = mock(BusTerminalRepository.class);

        BasePriceServiceImpl service = new BasePriceServiceImpl(repo);

        ValidationErrorException exception = assertThrows(ValidationErrorException.class,
                () -> service.getBasePrice(null));

        assertEquals("route", exception.getField());
        assertEquals("route not found", exception.getMessage());
        assertNull(exception.getRejectedValue());
        assertEquals(NOT_FOUND, exception.getHttpStatus());
    }
}
