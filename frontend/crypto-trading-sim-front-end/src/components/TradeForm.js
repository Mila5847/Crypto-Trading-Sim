import React, { useState, useEffect } from "react";
import { postTrade, resetAccount } from "../api";

export default function TradeForm({ symbols, selected, onSelectChange, onRefresh }) {
  const [qty, setQty] = useState("");

  useEffect(() => {
    setQty("");
  }, [selected]);

  const trade = async (type) => {
    try {
      await postTrade(type, { symbol: selected, quantity: parseFloat(qty) });
      onRefresh();
      setQty("");
    } catch (err) {
      alert(err.message || "Trade failed");
    }
  };

  return (
    <section>
      <h2>Trade</h2>
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
        <button onClick={() => trade("buy")}>Buy</button>
        <button type="button" onClick={() => trade("sell")}>Sell</button>
      </form>
      <button onClick={() => {
        if (window.confirm("Reset account?")) {
          resetAccount().then(onRefresh);
        }
      }}>
        Reset Account
      </button>
    </section>
  );
}
