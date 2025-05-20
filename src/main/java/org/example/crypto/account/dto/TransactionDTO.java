package org.example.crypto.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
        long id,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        String type,
        LocalDateTime timestamp,
        BigDecimal pnl
) {}