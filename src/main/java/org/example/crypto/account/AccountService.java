package org.example.crypto.account;

import org.example.crypto.account.dto.AccountDTO;
import org.example.crypto.account.dto.BuySellRequest;
import org.example.crypto.account.dto.HoldingDTO;
import org.example.crypto.account.dto.TransactionDTO;
import org.example.crypto.price.KrakenPriceService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private final JdbcTemplate jdbc;
    private final KrakenPriceService priceService;

    private static final long ACCOUNT_ID = 1L;
    private static final BigDecimal START_BALANCE = new BigDecimal("10000");

    public AccountService(JdbcTemplate jdbc, KrakenPriceService priceService) {
        this.jdbc = jdbc;
        this.priceService = priceService;
    }

    public AccountDTO getAccount() {
        BigDecimal balance = jdbc.queryForObject("SELECT balance FROM account WHERE id=?", BigDecimal.class, ACCOUNT_ID);
        List<HoldingDTO> holdings = jdbc.query(
                "SELECT symbol, quantity FROM holdings", (rs, rowNum) -> mapHolding(rs));
        return new AccountDTO(balance, holdings);
    }

    public List<TransactionDTO> getTransactions() {
        return jdbc.query("SELECT * FROM transactions ORDER BY timestamp DESC", (rs, n) -> mapTx(rs));
    }

    @Transactional
    public AccountDTO buy(BuySellRequest req) {
        BigDecimal price = requiredPrice(req.symbol());
        BigDecimal cost = price.multiply(req.quantity());

        BigDecimal balance = jdbc.queryForObject("SELECT balance FROM account WHERE id=?", BigDecimal.class, ACCOUNT_ID);
        if (balance.compareTo(cost) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        jdbc.update("UPDATE account SET balance = balance - ? WHERE id=?", cost, ACCOUNT_ID);
        int updated = jdbc.update("UPDATE holdings SET quantity = quantity + ? WHERE symbol = ?", req.quantity(), req.symbol());
        if (updated == 0) {
            jdbc.update("INSERT INTO holdings(symbol, quantity) VALUES (?,?)", req.symbol(), req.quantity());
        }
        jdbc.update("INSERT INTO transactions(symbol, quantity, price, type, timestamp) VALUES (?,?,?,?,?)",
                req.symbol(), req.quantity(), price, "BUY", LocalDateTime.now());
        return getAccount();
    }

    @Transactional
    public AccountDTO sell(BuySellRequest req) {
        BigDecimal price = requiredPrice(req.symbol());

        BigDecimal heldQty = jdbc.queryForObject("SELECT quantity FROM holdings WHERE symbol=?",
                BigDecimal.class, req.symbol());
        if (heldQty == null || heldQty.compareTo(req.quantity()) < 0) {
            throw new IllegalArgumentException("Not enough holdings to sell");
        }
        jdbc.update("UPDATE holdings SET quantity = quantity - ? WHERE symbol = ?", req.quantity(), req.symbol());
        jdbc.update("UPDATE account SET balance = balance + ? WHERE id=?",
                price.multiply(req.quantity()), ACCOUNT_ID);
        jdbc.update("INSERT INTO transactions(symbol, quantity, price, type, timestamp) VALUES (?,?,?,?,?)",
                req.symbol(), req.quantity(), price, "SELL", LocalDateTime.now());
        jdbc.update("DELETE FROM holdings WHERE quantity = 0");
        return getAccount();
    }

    @Transactional
    public AccountDTO reset() {
        jdbc.update("UPDATE account SET balance=? WHERE id=?", START_BALANCE, ACCOUNT_ID);
        jdbc.update("DELETE FROM holdings");
        jdbc.update("DELETE FROM transactions");
        return getAccount();
    }

    private BigDecimal requiredPrice(String symbol) {
        BigDecimal price = priceService.getPrice(symbol);
        if (price == null) throw new IllegalArgumentException("Price unavailable for " + symbol);
        return price;
    }

    private HoldingDTO mapHolding(ResultSet rs) throws SQLException {
        String symbol = rs.getString("symbol");
        BigDecimal qty = rs.getBigDecimal("quantity");
        BigDecimal price = priceService.getPrice(symbol);
        return new HoldingDTO(symbol, qty, price);
    }

    private TransactionDTO mapTx(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String symbol = rs.getString("symbol");
        BigDecimal qty = rs.getBigDecimal("quantity");
        BigDecimal price = rs.getBigDecimal("price");
        String type = rs.getString("type");
        LocalDateTime ts = rs.getTimestamp("timestamp").toLocalDateTime();
        BigDecimal current = priceService.getPrice(symbol);
        BigDecimal pnl = (current != null) ? current.subtract(price).multiply(qty) : BigDecimal.ZERO;
        return new TransactionDTO(id, symbol, qty, price, type, ts, pnl);
    }
}