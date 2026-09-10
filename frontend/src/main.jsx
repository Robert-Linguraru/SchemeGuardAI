import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import { login, register } from "./services/authApi";
import { getMockTransactions } from "./services/transactionsApi";
import "./style.css";

function App() {
    const [mode, setMode] = useState("login");
    const [form, setForm] = useState({
        email: "",
        password: "",
        fullName: ""
    });

    const [user, setUser] = useState(null);
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [transactions, setTransactions] = useState([]);
    const [transactionsLoading, setTransactionsLoading] = useState(false);
    const [transactionsError, setTransactionsError] = useState("");

    useEffect(() => {
        if (!user) {
            return;
        }

        const token = localStorage.getItem("accessToken");

        if (!token) {
            return;
        }

        setTransactionsLoading(true);
        setTransactionsError("");

        getMockTransactions(token)
            .then((data) => {
                setTransactions(data);
            })
            .catch((error) => {
                setTransactionsError(error.message);
            })
            .finally(() => {
                setTransactionsLoading(false);
            });
    }, [user]);

    const updateField = (event) => {
        setForm((previousForm) => ({
            ...previousForm,
            [event.target.name]: event.target.value
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        setMessage("");
        setLoading(true);

        try {
            const response = mode === "login"
                ? await login({
                    email: form.email,
                    password: form.password
                })
                : await register({
                    email: form.email,
                    password: form.password,
                    fullName: form.fullName
                });

            localStorage.setItem(
                "accessToken",
                response.accessToken
            );

            setUser(response);
            setMessage("");
        } catch (error) {
            setMessage(error.message);
        } finally {
            setLoading(false);
        }
    };

    const logout = () => {
        localStorage.removeItem("accessToken");
        setUser(null);
        setTransactions([]);
        setTransactionsError("");
        setMessage("");
    };

    const statistics = useMemo(() => {
        const processed = transactions.filter(
            (transaction) => transaction.status === "PROCESSED"
        ).length;

        const qualified = transactions.filter(
            (transaction) =>
                transaction.qualificationStatus === "QUALIFIED"
        ).length;

        const totalFees = transactions.reduce(
            (total, transaction) =>
                total + Number(transaction.estimatedFee || 0),
            0
        );

        return {
            total: transactions.length,
            processed,
            qualified,
            totalFees
        };
    }, [transactions]);

    const getBadgeClass = (value) => {
        return value.toLowerCase().replaceAll("_", "-");
    };

    if (user) {
        return (
            <main className="dashboard">
                <div className="brand-decoration" aria-hidden="true">
                    <span className="orb orb-blue orb-one"></span>
                    <span className="orb orb-coral orb-two"></span>
                    <span className="orb orb-white orb-three"></span>
                    <span className="orb orb-blue orb-four"></span>
                </div>

                <header className="dashboard-header">
                    <div className="header-actions">
                        <div className="welcome">
                            <h1>SchemeGuard AI</h1>
                            <p>Welcome, {user.fullName}</p>
                        </div>

                        <button
                            className="logout-button"
                            onClick={logout}
                        >
                            Logout
                        </button>
                    </div>
                </header>

                <section className="content">
                    <div className="page-heading">
                        <div>
                            <p className="eyebrow">PAYMENTS WORKSPACE</p>
                            <h2>Transaction overview</h2>
                            <p>
                                Monitor interchange qualification and fees.
                            </p>
                        </div>

                        <span className="role-badge">
                            {user.status} · MERCHANT
                        </span>
                    </div>

                    <section className="stats-grid">
                        <article className="stat-card">
                            <span className="stat-label">
                                Total transactions
                            </span>
                            <strong>{statistics.total}</strong>
                            <span className="stat-description">
                                Mock records available
                            </span>
                        </article>

                        <article className="stat-card">
                            <span className="stat-label">
                                Processed
                            </span>
                            <strong>{statistics.processed}</strong>
                            <span className="stat-description">
                                Successfully processed
                            </span>
                        </article>

                        <article className="stat-card">
                            <span className="stat-label">
                                Qualified
                            </span>
                            <strong>{statistics.qualified}</strong>
                            <span className="stat-description">
                                Passed qualification
                            </span>
                        </article>

                        <article className="stat-card">
                            <span className="stat-label">
                                Estimated fees
                            </span>
                            <strong>
                                {statistics.totalFees.toFixed(2)} EUR
                            </strong>
                            <span className="stat-description">
                                Combined estimated amount
                            </span>
                        </article>
                    </section>

                    <section className="transactions-card">
                        <div className="section-header">
                            <div>
                                <p className="eyebrow">TRANSACTIONS</p>
                                <h2>Mock transaction list</h2>
                            </div>

                            <span className="record-count">
                                {transactions.length} records
                            </span>
                        </div>

                        {transactionsLoading && (
                            <div className="empty-state">
                                Loading transactions...
                            </div>
                        )}

                        {transactionsError && (
                            <div className="error-box">
                                <strong>Unable to load transactions</strong>
                                <span>{transactionsError}</span>
                            </div>
                        )}

                        {!transactionsLoading &&
                            !transactionsError &&
                            transactions.length === 0 && (
                                <div className="empty-state">
                                    No transactions available.
                                </div>
                            )}

                        {!transactionsLoading &&
                            !transactionsError &&
                            transactions.length > 0 && (
                                <div className="table-container">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Transaction</th>
                                                <th>Merchant</th>
                                                <th>Scheme</th>
                                                <th>Amount</th>
                                                <th>Status</th>
                                                <th>Qualification</th>
                                                <th>Fee</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            {transactions.map(
                                                (transaction) => (
                                                    <tr
                                                        key={transaction.id}
                                                    >
                                                        <td>
                                                            <strong>
                                                                {
                                                                    transaction.externalId
                                                                }
                                                            </strong>
                                                        </td>

                                                        <td>
                                                            {
                                                                transaction.merchantName
                                                            }
                                                        </td>

                                                        <td>
                                                            <span className="scheme">
                                                                {
                                                                    transaction.scheme
                                                                }
                                                            </span>
                                                        </td>

                                                        <td>
                                                            {
                                                                transaction.amount
                                                            }{" "}
                                                            {
                                                                transaction.currencyCode
                                                            }
                                                        </td>

                                                        <td>
                                                            <span
                                                                className={`badge ${getBadgeClass(
                                                                    transaction.status
                                                                )}`}
                                                            >
                                                                {
                                                                    transaction.status
                                                                }
                                                            </span>
                                                        </td>

                                                        <td>
                                                            <span
                                                                className={`badge ${getBadgeClass(
                                                                    transaction.qualificationStatus
                                                                )}`}
                                                            >
                                                                {
                                                                    transaction.qualificationStatus
                                                                }
                                                            </span>
                                                        </td>

                                                        <td>
                                                            {
                                                                transaction.estimatedFee
                                                            }{" "}
                                                            {
                                                                transaction.currencyCode
                                                            }
                                                        </td>
                                                    </tr>
                                                )
                                            )}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                    </section>
                </section>
            </main>
        );
    }

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
                    <span className="brand-name">endava</span>
                </div>

                <h1>SchemeGuard AI</h1>

                <p className="subtitle">
                    Interchange qualification workspace
                </p>

                <div className="tabs">
                    <button
                        className={
                            mode === "login" ? "active" : ""
                        }
                        onClick={() => {
                            setMode("login");
                            setMessage("");
                        }}
                    >
                        Login
                    </button>

                    <button
                        className={
                            mode === "register" ? "active" : ""
                        }
                        onClick={() => {
                            setMode("register");
                            setMessage("");
                        }}
                    >
                        Create account
                    </button>
                </div>

                <form onSubmit={handleSubmit}>
                    {mode === "register" && (
                        <label>
                            Full name
                            <input
                                name="fullName"
                                value={form.fullName}
                                onChange={updateField}
                                required
                            />
                        </label>
                    )}

                    <label>
                        Email
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={updateField}
                            required
                        />
                    </label>

                    <label>
                        Password
                        <input
                            type="password"
                            name="password"
                            value={form.password}
                            onChange={updateField}
                            minLength="8"
                            required
                        />
                    </label>

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Please wait..."
                            : mode === "login"
                                ? "Login"
                                : "Create account"}
                    </button>
                </form>

                {message && (
                    <div className="form-error">
                        {message}
                    </div>
                )}
            </section>
        </main>
    );
}

createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);