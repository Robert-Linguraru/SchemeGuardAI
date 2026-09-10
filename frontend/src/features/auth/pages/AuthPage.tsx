import { useState } from "react";
import { LoginForm } from "../components/LoginForm";
import { RegisterForm } from "../components/RegisterForm";

export function AuthPage() {
    const [mode, setMode] = useState<"login" | "register">("login");

    return (
        <main className="page">
            <div className="brand-decoration" aria-hidden="true">
                <span className="orb orb-blue orb-one"></span>
                <span className="orb orb-coral orb-two"></span>
                <span className="orb orb-white orb-three"></span>
                <span className="orb orb-blue orb-four"></span>
            </div>
            <section className="card">
                <div className="brand login-brand">
                    <span className="brand-mark">●</span>
                    <span className="brand-name">SchemeGuard AI</span>
                </div>
                <h1>SchemeGuard AI</h1>
                <p className="subtitle">Interchange qualification workspace</p>
                <div className="tabs">
                    <button className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Login</button>
                    <button className={mode === "register" ? "active" : ""} onClick={() => setMode("register")}>Create account</button>
                </div>
                {mode === "login" ? <LoginForm /> : <RegisterForm />}
            </section>
        </main>
    );
}