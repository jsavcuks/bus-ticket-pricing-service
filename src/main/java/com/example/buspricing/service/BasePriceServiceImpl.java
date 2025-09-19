package com.example.buspricing.service;

import com.example.buspricing.domain.BusTerminal;
import com.example.buspricing.repository.BusTerminalRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BasePriceServiceImpl implements BasePriceService {

    private final BusTerminalRepository repository;

    public BasePriceServiceImpl(BusTerminalRepository repository) {
        this.repository = repository;
    }

    @Override
    public BigDecimal getBasePrice(String route) {
        return repository.findById(route)
                .map(BusTerminal::getBasePrice)
                .orElseThrow(() -> new RouteNotFoundException("Unknown route: " + route));
    }

    public static class RouteNotFoundException extends RuntimeException {
        public RouteNotFoundException(String message) {
            super(message);
        }
    }
}

