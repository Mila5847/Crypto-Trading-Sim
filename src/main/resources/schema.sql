CREATE TABLE IF NOT EXISTS account (
    id      INT PRIMARY KEY,
    balance NUMERIC(18,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS holdings (
    symbol     VARCHAR(10) PRIMARY KEY,
    quantity   NUMERIC(18,8) NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    symbol     VARCHAR(10) NOT NULL,
    quantity   NUMERIC(18,8) NOT NULL,
    price      NUMERIC(18,2) NOT NULL,
    type       VARCHAR(4) NOT NULL,   -- 'BUY' or 'SELL'
    pl         NUMERIC(18,2),         -- NULL for BUY, actual P/L for SELL
    timestamp  TIMESTAMP NOT NULL
);
