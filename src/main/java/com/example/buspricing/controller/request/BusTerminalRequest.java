package com.example.buspricing.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusTerminalRequest {
    @NotBlank
    private String terminalName;

    @DecimalMin(value = "0.00")
    private BigDecimal basePrice;
}
