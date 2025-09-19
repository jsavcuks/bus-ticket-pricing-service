package com.example.buspricing.service;

import com.example.buspricing.controller.request.DraftPriceRequest;
import com.example.buspricing.controller.response.DraftPriceResponse;

public interface PricingService {
    DraftPriceResponse calculateDraftPrice(DraftPriceRequest request);
}
