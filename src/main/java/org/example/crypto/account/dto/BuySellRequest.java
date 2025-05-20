package org.example.crypto.account.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record BuySellRequest(
        @NotBlank String symbol,
        @DecimalMin("0.00000001") BigDecimal quantity
) {}