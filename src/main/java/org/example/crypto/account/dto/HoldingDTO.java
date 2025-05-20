package org.example.crypto.account.dto;

import java.math.BigDecimal;

public record HoldingDTO(
        String symbol,
        BigDecimal quantity,
        BigDecimal currentPrice
) {}
