package org.example.crypto.account;
import org.example.crypto.account.dto.*;
import org.example.crypto.price.KrakenPriceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AccountController {

    private final AccountService service;
    private final KrakenPriceService priceService;

    public AccountController(AccountService service, KrakenPriceService priceService) {
        this.service = service;
        this.priceService = priceService;
    }

    /** Returns a map of {symbol -> lastPrice}. */
    @GetMapping("/prices")
    public Map<String, BigDecimal> prices() {
        return priceService.getCurrentPrices();
    }

    @GetMapping("/account")
    public AccountDTO account() { return service.getAccount(); }

    @GetMapping("/transactions")
    public List<TransactionDTO> transactions() { return service.getTransactions(); }

    @PostMapping("/buy")
    public AccountDTO buy(@Valid @RequestBody BuySellRequest req) { return service.buy(req); }

    @PostMapping("/sell")
    public AccountDTO sell(@Valid @RequestBody BuySellRequest req) { return service.sell(req); }

    @PostMapping("/reset")
    public AccountDTO reset() { return service.reset(); }
}