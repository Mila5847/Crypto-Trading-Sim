import React, { useState, useEffect } from "react";
import { postTrade, resetAccount } from "../api";

export default function TradeForm({ symbols, selected, onSelectChange, onRefresh }) {
  const [qty, setQty] = useState("");
  const [message, setMessage] = useState("");
  const [status, setStatus] = useState("");

  useEffect(() => {
    setQty("");
  }, [selected]);

  const trade = async (type) => {
    const numericQty = parseFloat(qty);

    // Input validation
    if (isNaN(numericQty) || numericQty <= 0) {
      setMessage("Quantity must be a positive number");
      setStatus("error");
      return;
    }

    try {
      await postTrade(type, { symbol: selected, quantity: numericQty });
      setMessage(`${type.toUpperCase()} successful: ${qty} ${selected}`);
      setStatus("success");
      setQty("");
      onRefresh();
    } catch (err) {
      // Graceful fallback
      const errorMsg =
        err?.response?.data?.message ||  // if backend sends a specific error
        err?.message ||                  // generic JS error
        "An unexpected error occurred";  // last resort
      setMessage(`${type.toUpperCase()} failed: ${errorMsg}`);
      setStatus("error");
    }
  };

  const handleReset = async () => {
    if (window.confirm("Reset account?")) {
      try {
        await resetAccount();
        setMessage("Account reset to starting balance");
        setStatus("success");
        onRefresh();
      } catch (err) {
        const errorMsg =
          err?.response?.data?.message ||
          err?.message ||
          "Failed to reset account";
        setMessage(`${errorMsg}`);
        setStatus("error");
      }
    }
  };

  return (
    <section>
      <h2>Trade</h2>

      {message && (
        <p style={{
          backgroundColor: status === "success" ? "#d4edda" : "#f8d7da",
          color: status === "success" ? "#155724" : "#721c24",
          border: "1px solid",
          borderColor: status === "success" ? "#c3e6cb" : "#f5c6cb",
          padding: "10px",
          borderRadius: "4px",
          marginBottom: "10px"
        }}>
          {message}
        </p>
      )}

      <form onSubmit={e => e.preventDefault()}>
        <select value={selected} onChange={e => onSelectChange(e.target.value)}>
          {symbols.map(s => <option key={s} value={s}>{s}</option>)}
        </select>
        <input
          type="number"
          step="0.00000001"
          value={qty}
          onChange={e => setQty(e.target.value)}
          placeholder="Quantity"
          required
        />
        <button type="button" onClick={() => trade("buy")}>Buy</button>
        <button type="button" onClick={() => trade("sell")}>Sell</button>
      </form>

      <button onClick={handleReset}>
        Reset Account
      </button>
    </section>
  );
}
