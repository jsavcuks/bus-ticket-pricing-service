package com.example.buspricing.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "BUS_TERMINALS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusTerminal {

    @Id
    @Column(name = "TERMINAL_NAME", nullable = false)
    private String terminalName;

    @Column(name = "BASE_PRICE", nullable = false)
    private BigDecimal basePrice;
}

