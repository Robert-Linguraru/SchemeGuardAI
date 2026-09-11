import { useEffect, useState } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { AccountMenu } from "../../../shared/components/AccountMenu";
import { ErrorMessage } from "../../../shared/components/ErrorMessage";
import { LoadingState } from "../../../shared/components/LoadingState";
import type { Transaction } from "../../../shared/types/transaction";
import { getMockTransactions } from "../api/transactionsApi";
import { TransactionStats } from "../components/TransactionStats";
import { TransactionTable } from "../components/TransactionTable";

interface DashboardPageProps {
    onCreateRule: () => void;
    onEditProfile: () => void;
}

export function DashboardPage({ onCreateRule, onEditProfile }: DashboardPageProps) {
    const { user } = useAuth();
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const controller = new AbortController();
        let isMounted = true;
        setIsLoading(true);
        setError(null);

        getMockTransactions(controller.signal)
            .then((loadedTransactions) => {
                if (isMounted) {
                    setTransactions(loadedTransactions);
                }
            })
            .catch((requestError: unknown) => {
                if (requestError instanceof DOMException && requestError.name === "AbortError") return;
                if (isMounted) {
                    setError(requestError instanceof Error ? requestError.message : "Could not load transactions");
                }
            })
            .finally(() => {
                if (isMounted) {
                    setIsLoading(false);
                }
            });

        return () => {
            isMounted = false;
            controller.abort();
        };
    }, [user]);

    return (
        <main className="dashboard">
            <div className="brand-decoration" aria-hidden="true">
                <span className="orb orb-blue orb-one"></span><span className="orb orb-coral orb-two"></span><span className="orb orb-white orb-three"></span><span className="orb orb-blue orb-four"></span>
            </div>
            <header className="dashboard-header">
                <div className="welcome"><h1>SchemeGuard AI</h1><p>Welcome, {user?.fullName}</p></div>
                <AccountMenu onEditProfile={onEditProfile} />
            </header>
            <section className="content">
                <div className="page-heading">
                    <div><p className="eyebrow">PAYMENTS WORKSPACE</p><h2>Transaction overview</h2><p>Monitor interchange qualification and fees.</p></div>
                    <div className="heading-actions"><span className="role-badge">{user?.status} · MERCHANT</span><button className="create-rule-button" onClick={onCreateRule}>Create rule</button></div>
                </div>
                <TransactionStats transactions={transactions} />
                <section className="transactions-card">
                    <div className="section-header"><div><p className="eyebrow">TRANSACTIONS</p><h2>Mock transaction list</h2></div><span className="record-count">{transactions.length} records</span></div>
                    {isLoading && <LoadingState message="Loading transactions..." />}
                    {error && <ErrorMessage title="Unable to load transactions" message={error} />}
                    {!isLoading && !error && transactions.length === 0 && <LoadingState message="No transactions available." />}
                    {!isLoading && !error && transactions.length > 0 && <TransactionTable transactions={transactions} />}
                </section>
            </section>
        </main>
    );
}
