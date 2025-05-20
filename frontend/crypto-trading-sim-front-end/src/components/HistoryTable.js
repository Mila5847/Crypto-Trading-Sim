import React, { useEffect, useState } from "react";
import { getTransactions } from "../api";

export default function HistoryTable({ refresh }) {
  const [txs, setTxs] = useState([]);

  useEffect(() => {
    getTransactions().then(setTxs);
  }, [refresh]);

  return (
    <section>
      <h2>Transactions</h2>
      <table>
        <thead>
          <tr><th>Time</th><th>Type</th><th>Symbol</th><th>Qty</th><th>Price</th><th>P/L</th></tr>
        </thead>
        <tbody>
          {txs.map(tx => (
            <tr key={tx.id}>
              <td>{new Date(tx.timestamp).toLocaleString()}</td>
              <td>{tx.type}</td>
              <td>{tx.symbol}</td>
              <td>{tx.quantity}</td>
              <td>{tx.price.toFixed(2)}</td>
              <td>{tx.pnl.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
