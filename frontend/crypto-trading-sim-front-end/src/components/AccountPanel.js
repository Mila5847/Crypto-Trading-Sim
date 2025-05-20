import React, { useEffect, useState } from "react";
import { getAccount } from "../api";

export default function AccountPanel({ refresh }) {
  const [account, setAccount] = useState({ balance: 0, holdings: [] });

  useEffect(() => {
    getAccount().then(setAccount);
  }, [refresh]);

  return (
    <section>
      <h2>Account</h2>
      <p>Balance: ${(+account.balance).toFixed(2)}</p>
      <h3>Holdings</h3>
      <table>
        <thead><tr><th>Symbol</th><th>Quantity</th><th>Value</th></tr></thead>
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
    </section>
  );
}
