package com.example.buspricing.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftPriceRequest {
    @NotBlank(message = "Route must not be blank")
    private String route;

    @NotEmpty(message = "Passengers list must not be empty")
    @Valid
    private List<Passenger> passengers;
}