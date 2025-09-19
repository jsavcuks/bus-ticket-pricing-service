package com.example.buspricing.controller.request;

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

    @NotNull
    private Type type;

    @Min(0)
    private int luggageCount;
}
