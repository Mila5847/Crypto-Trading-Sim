const API = "http://localhost:8080/api";

export const getPrices = () => fetch(`${API}/prices`).then(res => res.json());
export const getAccount = () => fetch(`${API}/account`).then(res => res.json());
export const getTransactions = () => fetch(`${API}/transactions`).then(res => res.json());

export const postTrade = (type, data) =>
  fetch(`${API}/${type}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
  }).then(res => res.ok ? res.json() : res.json().then(err => Promise.reject(err)));

export const resetAccount = () =>
  fetch(`${API}/reset`, { method: "POST" });
