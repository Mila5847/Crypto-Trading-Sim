# 💱 Crypto Trading Simulator

This is a full-stack web application that simulates cryptocurrency trading using **real-time Kraken API data**. It allows users to view live crypto prices, manage a virtual account balance, simulate trades, and view transaction history — all without actual market involvement.

---

## 📁 Project Structure

```
Crypto-Trading-Sim/
├── backend/
│   └── src/main/java/org/example/crypto/
│       ├── account/               # Account and trade management logic
│       ├── price/                 # Kraken WebSocket API integration
│       ├── common/                # DTOs and shared utilities
│       ├── config/                # App configuration
│       └── TradingSimApplication.java
├── frontend/
│   └── crypto-trading-sim-front-end/  # React-based front-end interface
```

---

## 🚀 Features

### ✅ Real-Time Price Feed
- Powered by Kraken WebSocket API (v2).
- The app dynamically fetches the top 20 cryptocurrencies by market cap using the CoinGecko API.

### ✅ Simulated Trading
- Buy or sell crypto assets at real-time prices.
- Balance is virtual (starting at $10,000).
- All transactions are stored in H2 (in-memory database).

### ✅ Account Management
- View current balance and crypto holdings.
- Transaction history with timestamps and trade details.
- Option to reset balance.

---

## ⚙️ Backend (Java + Spring Boot)

### Key Components

| Component | Description |
|----------|-------------|
| `KrakenPriceService` | Subscribes to Kraken's WebSocket API for live prices and caches them. |
| `AccountService` | Handles balance operations, trading logic, and database interaction. |
| `TradeController` | Exposes API endpoints for buy/sell actions. |
| `PriceController` | Streams live prices to the frontend. |

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/account` | Returns current account balance and holdings. |
| `POST` | `/trade/buy` | Simulates a crypto buy transaction. |
| `POST` | `/trade/sell` | Simulates a crypto sell transaction. |
| `GET` | `/prices` | Returns cached prices for 20 crypto pairs. |
| `GET` | `/prices/stream` | Server-Sent Events (SSE) stream for live price updates. |

---

## 🎨 Frontend (React)

- Built using functional components and hooks.
- Key components:
  - `PricesTable`: Displays live crypto prices.
  - `TradeForm`: Form to buy/sell assets.
  - `AccountPanel`: Shows current balance and holdings.
  - `HistoryTable`: Displays recent trades.
- Connects to the backend using fetch and SSE for real-time updates.

---

## 🧠 Design Decisions

- **Spring Boot + H2 DB**: Lightweight and perfect for prototyping with no setup overhead.
- **WebSocket + SSE**: Combines backend WebSocket for data fetching and SSE for frontend live updates.
- **Single Account Simulation**: Simplifies the scope by assuming a single virtual user.
- **React Frontend**: Ensures responsiveness and modularity with hooks and component reuse.

---

## 🛠️ Setup Instructions

### Backend

```bash
cd Crypto-Trading-Sim
./mvnw spring-boot:run
```

H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: _(leave blank)_

### Frontend

```bash
cd frontend/crypto-trading-sim-front-end
npm install
npm start
```

App: [http://localhost:3000](http://localhost:3000)

---

## 📌 Requirements

- Java 17+
- Node.js 16+
- Maven

## 🖼️ Screenshots
Iinitial application screen
![image](https://github.com/user-attachments/assets/8727dd12-74fc-4262-ab4b-9e79cea1c565)

User interface for buying and selling cryptocurrencies & showing the updated account balance after a transaction
![image](https://github.com/user-attachments/assets/33bffc32-3d16-4f47-908d-bb26a22696f0)









