package org.example.crypto.account.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountDTO(
        BigDecimal balance,
        List<HoldingDTO> holdings
) {}
