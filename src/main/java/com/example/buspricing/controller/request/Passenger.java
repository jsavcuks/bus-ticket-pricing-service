package com.example.buspricing.controller.request;

import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {
    public enum Type { ADULT, CHILD }

    @NotNull(message = "Passenger type is required")
    private Type type;

    @Min(value = 0, message = "Luggage count must be greater than or equal to 0")
    @Max(value = 100, message = "Luggage count must be less than or equal to 100")
    private int luggageCount;
}
