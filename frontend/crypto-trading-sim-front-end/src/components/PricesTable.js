import React, { useEffect, useState } from "react";
import { getPrices } from "../api";

export default function PricesTable({ onSymbolsLoaded }) {
  const [prices, setPrices] = useState({});
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const data = await getPrices();
        setPrices(data);
        onSymbolsLoaded(Object.keys(data));
        setError(""); // clear any old error if successful
      } catch (err) {
        const msg =
          err?.response?.data?.message ||
          err?.message ||
          "Failed to load prices";
        setError(msg);
      }
    };

    load(); // initial load
    const interval = setInterval(load, 2000); // repeat every 2s
    return () => clearInterval(interval); // cleanup
  }, [onSymbolsLoaded]);

  return (
    <section>
      <h2>Top 20 Prices (real-time)</h2>

      {/* Error message */}
      {error && (
        <p style={{
          backgroundColor: "#f8d7da",
          color: "#721c24",
          border: "1px solid #f5c6cb",
          padding: "10px",
          borderRadius: "4px",
          marginBottom: "10px"
        }}>
            {error}
        </p>
      )}

      {/* Show prices only if no error */}
      {!error && (
        <table>
          <thead>
            <tr><th>Symbol</th><th>Price (USD)</th></tr>
          </thead>
          <tbody>
            {Object.entries(prices).map(([sym, price]) => (
              <tr key={sym}>
                <td>{sym}</td>
                <td>{(+price).toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
