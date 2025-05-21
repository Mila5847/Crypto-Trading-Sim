package org.example.crypto.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.crypto.account.AccountController;
import org.example.crypto.account.AccountService;
import org.example.crypto.account.dto.AccountDTO;
import org.example.crypto.account.dto.BuySellRequest;
import org.example.crypto.account.dto.HoldingDTO;
import org.example.crypto.account.dto.TransactionDTO;
import org.example.crypto.price.KrakenPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private KrakenPriceService priceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetPrices() throws Exception {
        when(priceService.getCurrentPrices()).thenReturn(Map.of("ETH/USD", new BigDecimal("2300.00")));

        mockMvc.perform(get("/api/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['ETH/USD']").value("2300.0"));
    }

    @Test
    void testGetAccount() throws Exception {
        AccountDTO mockAccount = new AccountDTO(new BigDecimal("10000.00"), List.of());
        when(accountService.getAccount()).thenReturn(mockAccount);

        mockMvc.perform(get("/api/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("10000.0"));
    }

    @Test
    void testBuy() throws Exception {
        BuySellRequest request = new BuySellRequest("ETH/USD", new BigDecimal("1.5"));
        AccountDTO response = new AccountDTO(new BigDecimal("8000.00"), List.of(
                new HoldingDTO("ETH/USD", new BigDecimal("1.5"), new BigDecimal("2000.00"))
        ));

        when(accountService.buy(any())).thenReturn(response);

        mockMvc.perform(post("/api/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("8000.0"))
                .andExpect(jsonPath("$.holdings[0].symbol").value("ETH/USD"));
    }

    @Test
    void testSell() throws Exception {
        BuySellRequest request = new BuySellRequest("ETH/USD", new BigDecimal("0.5"));
        AccountDTO response = new AccountDTO(new BigDecimal("9500.00"), List.of());

        when(accountService.sell(any())).thenReturn(response);

        mockMvc.perform(post("/api/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("9500.0"));
    }

    @Test
    void testReset() throws Exception {
        AccountDTO resetAccount = new AccountDTO(new BigDecimal("10000.00"), List.of());
        when(accountService.reset()).thenReturn(resetAccount);

        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value("10000.0"));
    }

    @Test
    void testTransactions() throws Exception {
        TransactionDTO tx = new TransactionDTO(
                1L, "BTC/USD", new BigDecimal("1.0"), new BigDecimal("10000.00"),
                "BUY", java.time.LocalDateTime.now(), null);

        when(accountService.getTransactions()).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC/USD"));
    }
}