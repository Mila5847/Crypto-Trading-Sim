import React, { useEffect, useState } from "react";
import { getPrices } from "../api";

export default function PricesTable({ onSymbolsLoaded }) {
  const [prices, setPrices] = useState({});

  useEffect(() => {
    const load = async () => {
      const data = await getPrices();
      setPrices(data);
      onSymbolsLoaded(Object.keys(data));
    };
    load();
    const interval = setInterval(load, 2000);
    return () => clearInterval(interval);
  }, [onSymbolsLoaded]);

  return (
    <section>
      <h2>Top 20 Prices (real-time)</h2>
      <table>
        <thead>
          <tr><th>Symbol</th><th>Price (USD)</th></tr>
        </thead>
        <tbody>
          {Object.entries(prices).map(([sym, price]) => (
            <tr key={sym}><td>{sym}</td><td>{(+price).toFixed(2)}</td></tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
