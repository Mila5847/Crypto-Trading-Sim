package org.example.crypto.unit;

import org.example.crypto.account.AccountService;
import org.example.crypto.account.dto.AccountDTO;
import org.example.crypto.account.dto.BuySellRequest;
import org.example.crypto.price.KrakenPriceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private JdbcTemplate jdbc;
    private KrakenPriceService priceService;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        priceService = mock(KrakenPriceService.class);
        accountService = new AccountService(jdbc, priceService);
    }

    @Test
    void testBuySuccess() {
        BuySellRequest request = new BuySellRequest("ETH/USD", new BigDecimal("1.5"));
        BigDecimal currentPrice = new BigDecimal("2000.00");
        BigDecimal balance = new BigDecimal("10000");

        when(priceService.getPrice("ETH/USD")).thenReturn(currentPrice);
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), anyLong())).thenReturn(balance);
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);

        when(jdbc.query(anyString(), (ResultSetExtractor<Object>) any())).thenReturn(List.of());
        AccountDTO result = accountService.buy(request);

        assertNotNull(result);
        verify(jdbc).update(
                eq("INSERT INTO transactions(symbol, quantity, price, type, timestamp) VALUES (?,?,?,?,?)"),
                eq("ETH/USD"),
                eq(new BigDecimal("1.5")),
                eq(currentPrice),
                eq("BUY"),
                any()
        );
    }

    @Test
    void testBuyFailsWithInsufficientBalance() {
        BuySellRequest request = new BuySellRequest("BTC/USD", new BigDecimal("10"));
        BigDecimal currentPrice = new BigDecimal("1000");
        BigDecimal balance = new BigDecimal("500"); // not enough

        when(priceService.getPrice("BTC/USD")).thenReturn(currentPrice);
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), anyLong())).thenReturn(balance);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            accountService.buy(request);
        });

        assertEquals("Insufficient balance to complete purchase.", ex.getMessage());
    }

    @Test
    void testSellFailsWithNotEnoughHoldings() {
        BuySellRequest request = new BuySellRequest("ETH/USD", new BigDecimal("3"));
        BigDecimal currentPrice = new BigDecimal("1500");

        when(priceService.getPrice("ETH/USD")).thenReturn(currentPrice);
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), eq("ETH/USD"))).thenReturn(new BigDecimal("1"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            accountService.sell(request);
        });

        assertEquals("Not enough holdings to sell.", ex.getMessage());
    }

    @Test
    void testReset() {
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);
        when(jdbc.update("DELETE FROM holdings")).thenReturn(1);
        when(jdbc.update("DELETE FROM transactions")).thenReturn(1);
        when(jdbc.queryForObject(anyString(), eq(BigDecimal.class), anyLong())).thenReturn(new BigDecimal("10000"));
        when(jdbc.query(anyString(), (ResultSetExtractor<Object>) any())).thenReturn(List.of());

        AccountDTO result = accountService.reset();

        assertEquals(new BigDecimal("10000"), result.balance());
    }

    @Test
    void testValidateRequestThrowsForInvalidQuantity() {
        BuySellRequest request = new BuySellRequest("ETH/USD", new BigDecimal("-5"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            accountService.buy(request);
        });
        assertEquals("Quantity must be a positive number.", ex.getMessage());
    }
}
