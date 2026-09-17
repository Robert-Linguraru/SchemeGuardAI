import { useEffect, useState } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { AccountMenu } from "../../../shared/components/AccountMenu";
import { ErrorMessage } from "../../../shared/components/ErrorMessage";
import { LoadingState } from "../../../shared/components/LoadingState";
import type { Transaction } from "../../../shared/types/transaction";
import { getMockTransactions } from "../api/transactionsApi";
import {
    getTransactionExplanation,
    type TransactionExplanation
} from "../api/transactionExplanationApi";
import { TransactionStats } from "../components/TransactionStats";
import { TransactionTable } from "../components/TransactionTable";
import CsvTransactionUploadModal from "../../csvUpload/components/csvUploadModal";

interface DashboardPageProps {
    onCreateRule: () => void;
    onEditProfile: () => void;
    onOpenAnalysisDashboard: () => void;
}

export function DashboardPage({
    onCreateRule,
    onEditProfile,
    onOpenAnalysisDashboard
}: DashboardPageProps) {
    const { user } = useAuth();

    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [explanation, setExplanation] =
        useState<TransactionExplanation | null>(null);

    const [explanationError, setExplanationError] =
        useState<string | null>(null);

    const [isExplaining, setIsExplaining] = useState(false);

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
                if (
                    requestError instanceof DOMException &&
                    requestError.name === "AbortError"
                ) {
                    return;
                }

                if (isMounted) {
                    setError(
                        requestError instanceof Error
                            ? requestError.message
                            : "Could not load transactions"
                    );
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

    const explainTransaction = async (
        transaction: Transaction
    ): Promise<void> => {
        setExplanation(null);
        setExplanationError(null);
        setIsExplaining(true);

        try {
            const result = await getTransactionExplanation(
                transaction.id
            );

            setExplanation(result);
        } catch (requestError: unknown) {
            setExplanationError(
                requestError instanceof Error
                    ? requestError.message
                    : "Could not generate explanation"
            );
        } finally {
            setIsExplaining(false);
        }
    };

    return (
        <main className="dashboard">
            <div
                className="brand-decoration"
                aria-hidden="true"
            >
                <span className="orb orb-blue orb-one"></span>
                <span className="orb orb-coral orb-two"></span>
                <span className="orb orb-white orb-three"></span>
                <span className="orb orb-blue orb-four"></span>
            </div>

            <header className="dashboard-header">
                <div className="welcome">
                    <h1>SchemeGuard AI</h1>
                    <p>Welcome, {user?.fullName}</p>
                </div>

                <div className="dashboard-account-actions">
                    <span className="role-badge">
                        {user?.status} · MERCHANT
                    </span>

                    <AccountMenu
                        onEditProfile={onEditProfile}
                    />
                </div>
            </header>

            <section className="content">
                <div className="page-heading">
                    <div>
                        <p className="eyebrow">
                            PAYMENTS WORKSPACE
                        </p>

                        <h2>Transaction overview</h2>

                        <p>
                            Monitor interchange qualification and fees.
                        </p>
                    </div>

                    <div className="page-heading-actions">
                        <button
                            type="button"
                            className="dashboard-button"
                            onClick={onOpenAnalysisDashboard}
                        >
                            Dashboard
                        </button>

                        <CsvTransactionUploadModal />

                        <button
                            className="create-rule-button"
                            onClick={onCreateRule}
                        >
                            View Rules
                        </button>
                    </div>
                </div>

                <TransactionStats
                    transactions={transactions}
                />

                <section className="transactions-card">
                    <div className="section-header">
                        <div>
                            <p className="eyebrow">
                                TRANSACTIONS
                            </p>

                            <h2>Mock transaction list</h2>
                        </div>

                        <span className="record-count">
                            {transactions.length} records
                        </span>
                    </div>

                    {isLoading && (
                        <LoadingState
                            message="Loading transactions..."
                        />
                    )}

                    {error && (
                        <ErrorMessage
                            title="Unable to load transactions"
                            message={error}
                        />
                    )}

                    {!isLoading &&
                        !error &&
                        transactions.length === 0 && (
                            <LoadingState
                                message="No transactions available."
                            />
                        )}

                    {!isLoading &&
                        !error &&
                        transactions.length > 0 && (
                            <TransactionTable
                                transactions={transactions}
                                onExplain={explainTransaction}
                            />
                        )}

                    {isExplaining && (
                        <LoadingState
                            message="Qwen is generating the explanation..."
                        />
                    )}

                    {explanationError && (
                        <div className="error-box">
                            <strong>
                                LLM explanation unavailable
                            </strong>

                            <span>
                                {explanationError}
                            </span>
                        </div>
                    )}

                    {explanation && (
                        <section
                            className="explanation-panel"
                            aria-live="polite"
                        >
                            <h3>
                                Explanation for{" "}
                                {explanation.externalId}
                            </h3>

                            <p>
                                <strong>Status:</strong>{" "}
                                {explanation.qualificationStatus}
                            </p>

                            <p>
                                <strong>Fee:</strong>{" "}
                                {explanation.interchangeFee ?? "—"}{" "}
                                {explanation.currencyCode}
                            </p>

                            <p className="explanation-text">
                                {explanation.llmExplanation}
                            </p>

                            {explanation.originalExplanation && (
                                <small>
                                    <strong>
                                        Original rule result:
                                    </strong>{" "}
                                    {explanation.originalExplanation}
                                </small>
                            )}
                        </section>
                    )}
                </section>
            </section>
        </main>
    );
}
