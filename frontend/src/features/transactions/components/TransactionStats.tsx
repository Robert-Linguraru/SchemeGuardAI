import type { Transaction } from "../../../shared/types/transaction";

interface TransactionStatsProps {
    transactions: Transaction[];
}

export function TransactionStats({ transactions }: TransactionStatsProps) {
    const processed = transactions.filter((transaction) => transaction.status === "PROCESSED").length;
    const qualified = transactions.filter((transaction) => transaction.qualificationStatus === "QUALIFIED").length;
    const totalFees = transactions.reduce((total, transaction) => total + Number(transaction.estimatedFee || 0), 0);
    const stats = [
        ["Total transactions", transactions.length, "Mock records available"],
        ["Processed", processed, "Successfully processed"],
        ["Qualified", qualified, "Passed qualification"],
        ["Estimated fees", `${totalFees.toFixed(2)} EUR`, "Combined estimated amount"]
    ];

    return (
        <section className="stats-grid">
            {stats.map(([label, value, description]) => (
                <article className="stat-card" key={label}>
                    <span className="stat-label">{label}</span>
                    <strong>{value}</strong>
                    <span className="stat-description">{description}</span>
                </article>
            ))}
        </section>
    );
}