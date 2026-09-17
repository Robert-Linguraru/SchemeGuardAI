import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { AccountMenu } from "../../../shared/components/AccountMenu";
import { ErrorMessage } from "../../../shared/components/ErrorMessage";
import { LoadingState } from "../../../shared/components/LoadingState";
import { getAnalytics, type AnalyticsFilters, type AnalyticsMetric, type AnalyticsResponse } from "../api/analyticsApi";

interface AnalysisDashboardPageProps {
    onBack: () => void;
    onEditProfile: () => void;
}

type Filters = AnalyticsFilters;

const emptyFilters: Filters = { from: "", to: "", scheme: "", country: "", merchant: "", qualification: "", status: "" };
const label = (value: string) => value.replaceAll("_", " ");

function BarList({ items, valueLabel, useFees = false }: { items: AnalyticsMetric[]; valueLabel: string; useFees?: boolean }) {
    const values = items.map((item) => useFees ? Number(item.fees) : item.transactions);
    const max = Math.max(...values, 1);
    if (!items.length) return <p className="analysis-empty">No data for the selected filters.</p>;
    return (
        <div className="analysis-bars">
            {items.slice(0, 6).map((item, index) => {
                const value = values[index];
                return <div className="analysis-bar-row" key={item.name}>
                    <span>{label(item.name)}</span>
                    <div className="analysis-bar-track"><i style={{ width: `${(value / max) * 100}%` }} /></div>
                    <strong>{useFees ? `${value.toFixed(2)} EUR` : value}</strong>
                </div>;
            })}
            <small className="analysis-chart-caption">{valueLabel}</small>
        </div>
    );
}

export function AnalysisDashboardPage({ onBack, onEditProfile }: AnalysisDashboardPageProps) {
    const { user } = useAuth();
    const [analytics, setAnalytics] = useState<AnalyticsResponse | null>(null);
    const [filters, setFilters] = useState<Filters>(emptyFilters);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;

        getAnalytics(filters)
            .then((loadedAnalytics) => {
                if (isMounted) setAnalytics(loadedAnalytics);
            })
            .catch((requestError: unknown) => {
                if (isMounted) {
                    setError(requestError instanceof Error ? requestError.message : "Could not load analytics");
                }
            })
            .finally(() => {
                if (isMounted) setIsLoading(false);
            });

        return () => {
            isMounted = false;
        };
    }, [user]);

    const options = useMemo(() => ({
        schemes: analytics?.feesByScheme.map((metric) => metric.name).sort() || [],
        countries: analytics?.transactionsByCountry.map((metric) => metric.name).sort() || [],
        merchants: analytics?.topMerchantsByFees.map((metric) => metric.name).sort() || []
    }), [analytics]);
    const setFilter = (key: keyof Filters, value: string) => setFilters((current) => ({ ...current, [key]: value }));

    const exportCsv = () => {
        const columns = ["Transaction", "Merchant", "Scheme", "Country", "Qualification", "Status", "Fee"];
        if (!analytics) return;
        const rows = analytics.topMerchantsByFees.map((metric) => [metric.name, String(metric.transactions), String(metric.fees)]);
        const csv = [columns, ...rows].map((row) => row.map((value) => `"${value.replaceAll('"', '""')}"`).join(",")).join("\n");
        const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
        const link = document.createElement("a");
        link.href = url;
        link.download = "schemeguard-analysis-summary.csv";
        link.click();
        URL.revokeObjectURL(url);
    };

    return (
        <main className="dashboard analysis-dashboard">
            <div className="brand-decoration" aria-hidden="true"><span className="orb orb-blue orb-one"></span><span className="orb orb-coral orb-two"></span><span className="orb orb-white orb-three"></span><span className="orb orb-blue orb-four"></span></div>
            <header className="dashboard-header">
                <div className="welcome"><h1>SchemeGuard AI</h1><p>Welcome, {user?.fullName}</p></div>
                <div className="dashboard-account-actions"><span className="role-badge">{user?.status} · MERCHANT</span><AccountMenu onEditProfile={onEditProfile} /></div>
            </header>
            <section className="content">
                <div className="page-heading analysis-heading">
                    <div><p className="eyebrow">ANALYSIS DASHBOARD</p><h2>Descriptive statistics</h2><p>Explore qualification results, fees and transaction distribution.</p></div>
                    <div className="page-heading-actions"><button type="button" className="secondary-button page-nav-button" onClick={onBack}>Transaction overview</button><button type="button" className="create-rule-button" onClick={exportCsv} disabled={!analytics?.totalTransactions}>Export CSV</button></div>
                </div>
                <section className="analysis-filters" aria-label="Dashboard filters">
                    <label>From<input type="date" value={filters.from} onChange={(event) => setFilter("from", event.target.value)} /></label>
                    <label>To<input type="date" value={filters.to} onChange={(event) => setFilter("to", event.target.value)} /></label>
                    <label>Scheme<select value={filters.scheme} onChange={(event) => setFilter("scheme", event.target.value)}><option value="">All schemes</option>{options.schemes.map((value) => <option key={value}>{value}</option>)}</select></label>
                    <label>Country<select value={filters.country} onChange={(event) => setFilter("country", event.target.value)}><option value="">All countries</option>{options.countries.map((value) => <option key={value}>{value}</option>)}</select></label>
                    <label>Merchant<select value={filters.merchant} onChange={(event) => setFilter("merchant", event.target.value)}><option value="">All merchants</option>{options.merchants.map((value) => <option key={value}>{value}</option>)}</select></label>
                    <label>Qualification<select value={filters.qualification} onChange={(event) => setFilter("qualification", event.target.value)}><option value="">All results</option><option value="QUALIFIED">Qualified</option><option value="NOT_QUALIFIED">Not qualified</option><option value="PENDING">Pending</option></select></label>
                    <label>Status<select value={filters.status} onChange={(event) => setFilter("status", event.target.value)}><option value="">All statuses</option><option value="NEW">New</option><option value="PROCESSED">Processed</option><option value="FAILED">Failed</option></select></label>
                    <button type="button" className="filter-reset" onClick={() => setFilters(emptyFilters)}>Reset</button>
                </section>
                {isLoading && <LoadingState message="Loading analytics..." />}
                {error && <ErrorMessage title="Unable to load analytics" message={error} />}
                {!isLoading && !error && <>
                    <section className="analysis-kpi-grid">
                        <article><span>Total transactions</span><strong>{analytics?.totalTransactions}</strong><small>{analytics?.processedTransactions} processed</small></article>
                        <article><span>Qualification rate</span><strong>{analytics?.qualificationRate}%</strong><small>{analytics?.qualifiedTransactions} qualified transactions</small></article>
                        <article><span>Estimated fees</span><strong>{Number(analytics?.totalFees || 0).toFixed(2)} EUR</strong><small>Combined filtered amount</small></article>
                        <article><span>Average transaction</span><strong>{Number(analytics?.averageTransactionAmount || 0).toFixed(2)} EUR</strong><small>Total amount: {Number(analytics?.totalAmount || 0).toFixed(2)} EUR</small></article>
                    </section>
                    <section className="analysis-chart-grid">
                        <article className="analysis-card"><div className="analysis-card-heading"><h3>Qualification results</h3><span>{analytics?.totalTransactions} total</span></div><BarList items={analytics?.qualificationResults || []} valueLabel="Transactions" /></article>
                        <article className="analysis-card"><div className="analysis-card-heading"><h3>Transactions by scheme</h3><span>Volume</span></div><BarList items={analytics?.feesByScheme || []} valueLabel="Transactions" /></article>
                        <article className="analysis-card"><div className="analysis-card-heading"><h3>Transactions by country</h3><span>Volume</span></div><BarList items={analytics?.transactionsByCountry || []} valueLabel="Top six countries" /></article>
                        <article className="analysis-card"><div className="analysis-card-heading"><h3>Top merchants by fees</h3><span>Fees (EUR)</span></div><BarList items={analytics?.topMerchantsByFees || []} valueLabel="Estimated fees" useFees /></article>
                    </section>
                </>}
            </section>
        </main>
    );
}
