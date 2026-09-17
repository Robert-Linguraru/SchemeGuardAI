import { useMemo, useState } from "react";
import type { Transaction } from "../../../shared/types/transaction";

interface TransactionTableProps {
    transactions: Transaction[];
    onExplain: (transaction: Transaction) => void;
}

const badgeClass = (value: string): string =>
    value.toLowerCase().replaceAll("_", "-");

const formatAmount = (
    value: number | string | null,
    currency: string
): string => `${value ?? "0"} ${currency}`;

export function TransactionTable({
    transactions,
    onExplain
}: TransactionTableProps) {
    const [sortKey, setSortKey] = useState<"authorizedAt" | "amount" | "estimatedFee">("authorizedAt");
    const [sortAscending, setSortAscending] = useState(false);
    const [page, setPage] = useState(1);
    const pageSize = 10;

    const sortedTransactions = useMemo(() => [...transactions].sort((left, right) => {
        let comparison = 0;
        if (sortKey === "authorizedAt") comparison = (left.authorizedAt || "").localeCompare(right.authorizedAt || "");
        if (sortKey === "amount") comparison = Number(left.amount || 0) - Number(right.amount || 0);
        if (sortKey === "estimatedFee") comparison = Number(left.estimatedFee || 0) - Number(right.estimatedFee || 0);
        return sortAscending ? comparison : -comparison;
    }), [sortKey, sortAscending, transactions]);

    const pageCount = Math.max(1, Math.ceil(sortedTransactions.length / pageSize));
    const visibleTransactions = sortedTransactions.slice((page - 1) * pageSize, page * pageSize);
    const changeSort = (key: "authorizedAt" | "amount" | "estimatedFee") => {
        if (sortKey === key) setSortAscending((ascending) => !ascending);
        else { setSortKey(key); setSortAscending(false); }
        setPage(1);
    };

    return (
        <div className="transaction-table-wrapper">
            <div className="table-toolbar">
                <span>Showing {visibleTransactions.length ? (page - 1) * pageSize + 1 : 0}–{Math.min(page * pageSize, sortedTransactions.length)} of {sortedTransactions.length}</span>
                <div className="table-pagination" aria-label="Transaction table pagination">
                    <button type="button" onClick={() => setPage((current) => Math.max(1, current - 1))} disabled={page === 1}>Previous</button>
                    <span>Page {page} of {pageCount}</span>
                    <button type="button" onClick={() => setPage((current) => Math.min(pageCount, current + 1))} disabled={page === pageCount}>Next</button>
                </div>
            </div>
            <div className="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Transaction</th>
                        <th>Merchant</th>
                        <th>Scheme</th>
                        <th><button type="button" className="table-sort-button" onClick={() => changeSort("amount")}>Amount {sortKey === "amount" && (sortAscending ? "↑" : "↓")}</button></th>
                        <th>Status</th>
                        <th>Qualification</th>
                        <th><button type="button" className="table-sort-button" onClick={() => changeSort("estimatedFee")}>Fee {sortKey === "estimatedFee" && (sortAscending ? "↑" : "↓")}</button></th>
                        <th>AI explanation</th>
                    </tr>
                </thead>

                <tbody>
                    {visibleTransactions.map((transaction) => (
                        <tr key={transaction.id}>
                            <td>
                                <strong>{transaction.externalId}</strong>
                            </td>

                            <td>{transaction.merchantName}</td>

                            <td>
                                <span className="scheme">
                                    {transaction.scheme}
                                </span>
                            </td>

                            <td>
                                {formatAmount(
                                    transaction.amount,
                                    transaction.currencyCode
                                )}
                            </td>

                            <td>
                                <span
                                    className={`badge ${badgeClass(
                                        transaction.status
                                    )}`}
                                >
                                    {transaction.status}
                                </span>
                            </td>

                            <td>
                                <span
                                    className={`badge ${badgeClass(
                                        transaction.qualificationStatus
                                    )}`}
                                >
                                    {transaction.qualificationStatus}
                                </span>
                            </td>

                            <td>
                                {formatAmount(
                                    transaction.estimatedFee,
                                    transaction.currencyCode
                                )}
                            </td>

                            <td>
                                <button
                                    type="button"
                                    className="explain-button"
                                    onClick={() =>
                                        onExplain(transaction)
                                    }
                                >
                                    Explain result
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            </div>
        </div>
    );
}
