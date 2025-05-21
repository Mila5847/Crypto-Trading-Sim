import React, { useEffect, useState } from "react";
import { getAccount } from "../api";

export default function AccountPanel({ refresh }) {
  const [account, setAccount] = useState({ balance: 0, holdings: [] });
  const [error, setError] = useState("");

  useEffect(() => {
    getAccount()
      .then(setAccount)
      .catch(err => {
        const msg =
          err?.response?.data?.message ||
          err?.message ||
          "Failed to load account information";
        setError(msg);
      });
  }, [refresh]);

  return (
    <section>
      <h2>Account</h2>

      {/* Show error if exists */}
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

      {/* Show data only if no error */}
      {!error && (
        <>
          <p>Balance: ${(+account.balance).toFixed(2)}</p>
          <h3>Holdings</h3>
          <table>
            <thead>
              <tr><th>Symbol</th><th>Quantity</th><th>Value</th></tr>
            </thead>
            <tbody>
              {account.holdings.map(h => (
                <tr key={h.symbol}>
                  <td>{h.symbol}</td>
                  <td>{h.quantity}</td>
                  <td>{(h.quantity * h.currentPrice).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </section>
  );
}
