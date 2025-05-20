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
    setSelectedSymbol(list[0]);
  };

  return (
    <>
      <h1>Crypto Trading Simulator</h1>
      <PricesTable onSymbolsLoaded={handleSymbolsLoaded} />
      <AccountPanel refresh={refreshTrigger} />
      <TradeForm
        symbols={symbols}
        selected={selectedSymbol}
        onSelectChange={setSelectedSymbol}
        onRefresh={refresh}
      />
      <HistoryTable refresh={refreshTrigger} />
    </>
  );
}
