import React, { useEffect, useState } from "react";
import { getTransactions } from "../api";

export default function HistoryTable({ refresh }) {
  const [txs, setTxs] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    getTransactions()
      .then(setTxs)
      .catch(err => {
        const msg =
          err?.response?.data?.message ||
          err?.message ||
          "Failed to load transactions";
        setError(msg);
      });
  }, [refresh]);

  return (
    <section>
      <h2>Transactions</h2>

      {/* Error Message */}
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

      {!error && (
        <table>
          <thead>
            <tr>
              <th>Time</th>
              <th>Type</th>
              <th>Symbol</th>
              <th>Qty</th>
              <th>Price</th>
              <th>Profit/Loss</th>
            </tr>
          </thead>
          <tbody>
            {txs.map(tx => (
              <tr key={tx.id}>
                <td>{new Date(tx.timestamp).toLocaleString()}</td>
                <td>{tx.type}</td>
                <td>{tx.symbol}</td>
                <td>{parseFloat(tx.quantity).toFixed(8)}</td>
                <td>${parseFloat(tx.price).toFixed(2)}</td>
                <td>
                  {tx.pnl != null
                    ? (
                      <span style={{ color: tx.pnl >= 0 ? "green" : "red" }}>
                        {tx.pnl >= 0 ? "+" : ""}
                        {parseFloat(tx.pnl).toFixed(2)}
                      </span>
                    ) : (
                      "-"
                    )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}