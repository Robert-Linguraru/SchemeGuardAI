import { request } from "../../../shared/api/httpClient";

export interface AnalyticsMetric {
    name: string;
    transactions: number;
    fees: number | string;
}

export interface AnalyticsResponse {
    totalTransactions: number;
    processedTransactions: number;
    qualifiedTransactions: number;
    partiallyQualifiedTransactions: number;
    notQualifiedTransactions: number;
    totalAmount: number | string;
    totalFees: number | string;
    averageTransactionAmount: number | string;
    qualificationRate: number | string;
    qualificationResults: AnalyticsMetric[];
    feesByScheme: AnalyticsMetric[];
    transactionsByCountry: AnalyticsMetric[];
    topMerchantsByFees: AnalyticsMetric[];
}

export interface AnalyticsFilters {
    from: string;
    to: string;
    scheme: string;
    country: string;
    merchant: string;
    qualification: string;
    status: string;
}

export function getAnalytics(filters: AnalyticsFilters): Promise<AnalyticsResponse> {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
        if (value) params.set(key, value);
    });
    const query = params.toString();
    return request<AnalyticsResponse>(`/api/analytics${query ? `?${query}` : ""}`, { method: "GET" });
}
