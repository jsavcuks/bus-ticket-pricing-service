package com.example.buspricing.controller;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.response.DraftPriceResponse;
import com.example.buspricing.service.PricingService;
import com.example.buspricing.service.BasePriceServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/draft")
    public ResponseEntity<DraftPriceResponse> draft(@Valid @RequestBody DraftPriceRequest request) {
        DraftPriceResponse response = pricingService.calculateDraftPrice(request);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(BasePriceServiceImpl.RouteNotFoundException.class)
    public ResponseEntity<String> handleRouteNotFound(BasePriceServiceImpl.RouteNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}

