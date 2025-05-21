import React, { useState } from "react";
import PricesTable from "./components/PricesTable";
import AccountPanel from "./components/AccountPanel";
import TradeForm from "./components/TradeForm";
import HistoryTable from "./components/HistoryTable";

export default function App() {
  const [symbols, setSymbols] = useState([]);
  const [selectedSymbol, setSelectedSymbol] = useState("");

  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const refresh = () => setRefreshTrigger(prev => prev + 1);

  const handleSymbolsLoaded = (list) => {
    setSymbols(list);
    setSelectedSymbol((prev) => prev || list[0]);
  };

  return (
    <div style={{ padding: "1rem", fontFamily: "Arial, sans-serif" }}>
      <h1>Crypto Trading Simulator</h1>

      {/* Prices (left) + Account (right) */}
      <div style={{ display: "flex", gap: "2rem", marginBottom: "2rem" }}>
        <div style={{ flex: 1 }}>
          <PricesTable onSymbolsLoaded={handleSymbolsLoaded} />
        </div>
        <div style={{ flex: 1 }}>
          <AccountPanel refresh={refreshTrigger} />
        </div>
      </div>

      {/* Trade Form (left) + History (right) */}
      <div style={{ display: "flex", gap: "2rem" }}>
        <div style={{ flex: 1 }}>
          <TradeForm
            symbols={symbols}
            selected={selectedSymbol}
            onSelectChange={setSelectedSymbol}
            onRefresh={refresh}
          />
        </div>
        <div style={{ flex: 1 }}>
          <HistoryTable refresh={refreshTrigger} />
        </div>
      </div>
    </div>
  );
}
