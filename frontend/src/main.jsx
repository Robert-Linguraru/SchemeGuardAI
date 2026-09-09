import React from "react";
import { createRoot } from "react-dom/client";
import "./style.css";

function App() {
  return (
    <main>
      <h1>SchemeGuard AI Payments</h1>
      <p>Interchange qualification and scheme compliance workspace.</p>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);