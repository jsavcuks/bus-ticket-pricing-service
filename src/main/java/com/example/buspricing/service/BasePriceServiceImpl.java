package com.example.buspricing.service;

import com.example.buspricing.domain.BusTerminal;
import com.example.buspricing.exception.ValidationErrorException;
import com.example.buspricing.repository.BusTerminalRepository;
import org.springframework.http.HttpStatus;
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
                .orElseThrow(() -> new ValidationErrorException("route",
                        "route not found",
                        route,
                        HttpStatus.NOT_FOUND));
    }
}

