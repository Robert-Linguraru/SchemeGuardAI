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
    return (
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
                        <th>AI explanation</th>
                    </tr>
                </thead>

                <tbody>
                    {transactions.map((transaction) => (
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
    );
}