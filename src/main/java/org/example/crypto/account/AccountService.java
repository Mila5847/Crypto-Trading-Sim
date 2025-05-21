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

    // Returns the current account balance and all holdings
    public AccountDTO getAccount() {
        BigDecimal balance = jdbc.queryForObject("SELECT balance FROM account WHERE id=?", BigDecimal.class, ACCOUNT_ID);
        List<HoldingDTO> holdings = jdbc.query(
                "SELECT symbol, quantity FROM holdings", (rs, rowNum) -> mapHolding(rs));
        return new AccountDTO(balance, holdings);
    }

    // Returns all past transactions sorted by most recent
    public List<TransactionDTO> getTransactions() {
        return jdbc.query("SELECT * FROM transactions ORDER BY timestamp DESC", (rs, n) -> mapTx(rs));
    }

    // Handles buying of cryptocurrency
    @Transactional
    public AccountDTO buy(BuySellRequest req) {
        validateRequest(req); // check for nulls, empty symbol, or invalid quantity

        BigDecimal price = requiredPrice(req.symbol()); // get current price
        BigDecimal cost = price.multiply(req.quantity()); // total cost of purchase

        // Check if balance is sufficient
        BigDecimal balance = jdbc.queryForObject("SELECT balance FROM account WHERE id=?", BigDecimal.class, ACCOUNT_ID);
        if (balance.compareTo(cost) < 0) {
            throw new IllegalArgumentException("Insufficient balance to complete purchase.");
        }

        try {
            // Deduct the cost from the account balance
            jdbc.update("UPDATE account SET balance = balance - ? WHERE id=?", cost, ACCOUNT_ID);

            // Add to holdings or insert new holding if it doesn't exist
            int updated = jdbc.update("UPDATE holdings SET quantity = quantity + ? WHERE symbol = ?", req.quantity(), req.symbol());
            if (updated == 0) {
                jdbc.update("INSERT INTO holdings(symbol, quantity) VALUES (?,?)", req.symbol(), req.quantity());
            }

            // Record the transaction
            jdbc.update("INSERT INTO transactions(symbol, quantity, price, type, timestamp) VALUES (?,?,?,?,?)",
                    req.symbol(), req.quantity(), price, "BUY", LocalDateTime.now());

            return getAccount(); // Return updated account info
        } catch (Exception ex) {
            throw new RuntimeException("Failed to complete BUY operation: " + ex.getMessage(), ex);
        }
    }

    // Handles selling of cryptocurrency
    @Transactional
    public AccountDTO sell(BuySellRequest req) {
        validateRequest(req); // validate input

        BigDecimal price = requiredPrice(req.symbol()); // get current price

        // Get current holding quantity
        BigDecimal heldQty = jdbc.queryForObject("SELECT quantity FROM holdings WHERE symbol=?",
                BigDecimal.class, req.symbol());
        if (heldQty == null || heldQty.compareTo(req.quantity()) < 0) {
            throw new IllegalArgumentException("Not enough holdings to sell.");
        }

        try {
            // Calculate average buy price for profit/loss calculation
            BigDecimal totalQty = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(quantity), 0) FROM transactions WHERE symbol=? AND type='BUY'",
                    BigDecimal.class, req.symbol());

            BigDecimal totalCost = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(quantity * price), 0) FROM transactions WHERE symbol=? AND type='BUY'",
                    BigDecimal.class, req.symbol());

            BigDecimal avgBuyPrice = (totalQty.compareTo(BigDecimal.ZERO) > 0)
                    ? totalCost.divide(totalQty, 8, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;

            // Profit or loss = (SellPrice - AvgBuyPrice) * quantity sold
            BigDecimal profitLoss = price.subtract(avgBuyPrice).multiply(req.quantity());

            // Update holdings and balance
            jdbc.update("UPDATE holdings SET quantity = quantity - ? WHERE symbol = ?", req.quantity(), req.symbol());
            jdbc.update("UPDATE account SET balance = balance + ? WHERE id=?",
                    price.multiply(req.quantity()), ACCOUNT_ID);

            // Record transaction with profit/loss
            jdbc.update("INSERT INTO transactions(symbol, quantity, price, type, pl, timestamp) VALUES (?,?,?,?,?,?)",
                    req.symbol(), req.quantity(), price, "SELL", profitLoss, LocalDateTime.now());

            // Clean up any zero holdings
            jdbc.update("DELETE FROM holdings WHERE quantity = 0");

            return getAccount();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to complete SELL operation: " + ex.getMessage(), ex);
        }
    }

    // Resets the account to the initial state
    @Transactional
    public AccountDTO reset() {
        jdbc.update("UPDATE account SET balance=? WHERE id=?", START_BALANCE, ACCOUNT_ID);
        jdbc.update("DELETE FROM holdings");
        jdbc.update("DELETE FROM transactions");
        return getAccount();
    }

    // Basic validation for Buy/Sell requests
    private void validateRequest(BuySellRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Trade request cannot be null.");
        }
        if (req.symbol() == null || req.symbol().isBlank()) {
            throw new IllegalArgumentException("Symbol is required.");
        }
        if (req.quantity() == null || req.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
    }

    // Helper method to fetch price or throw if unavailable
    private BigDecimal requiredPrice(String symbol) {
        BigDecimal price = priceService.getPrice(symbol);
        if (price == null) throw new IllegalArgumentException("Price unavailable for " + symbol);
        return price;
    }

    // Maps a row from the holdings table to HoldingDTO, including current price
    private HoldingDTO mapHolding(ResultSet rs) throws SQLException {
        String symbol = rs.getString("symbol");
        BigDecimal qty = rs.getBigDecimal("quantity");
        BigDecimal price = priceService.getPrice(symbol);
        return new HoldingDTO(symbol, qty, price);
    }

    // Maps a row from the transactions table to TransactionDTO
    private TransactionDTO mapTx(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String symbol = rs.getString("symbol");
        BigDecimal qty = rs.getBigDecimal("quantity");
        BigDecimal price = rs.getBigDecimal("price");
        String type = rs.getString("type");
        LocalDateTime ts = rs.getTimestamp("timestamp").toLocalDateTime();
        BigDecimal pnl = rs.getBigDecimal("pl");
        return new TransactionDTO(id, symbol, qty, price, type, ts, pnl);
    }
}