package com.example.buspricing.controller;

import com.example.buspricing.controller.request.BusTerminalRequest;
import com.example.buspricing.domain.BusTerminal;
import com.example.buspricing.repository.BusTerminalRepository;
import com.example.buspricing.exception.ValidationErrorException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bus-terminals")
public class BusTerminalController {

    private final BusTerminalRepository repository;

    public BusTerminalController(BusTerminalRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BusTerminalRequest request) {
        // Reject if already exists to avoid silent updates
        if (repository.existsById(request.getTerminalName())) {
            throw new ValidationErrorException("terminalName",
                    "terminal already exists",
                    request.getTerminalName(),
                    HttpStatus.CONFLICT);
        }

        BusTerminal entity = BusTerminal.builder()
                .terminalName(request.getTerminalName())
                .basePrice(request.getBasePrice())
                .build();

        BusTerminal saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}

