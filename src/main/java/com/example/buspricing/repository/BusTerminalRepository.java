package com.example.buspricing.repository;

import com.example.buspricing.domain.BusTerminal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusTerminalRepository extends JpaRepository<BusTerminal, String> {
}

