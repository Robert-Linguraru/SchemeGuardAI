import { request } from "../../../shared/api/httpClient";
import type { Transaction } from "../../../shared/types/transaction";

export const getMockTransactions = (
    signal?: AbortSignal
): Promise<Transaction[]> =>
    request<Transaction[]>("/api/transactions/mock", {
        method: "GET",
        signal
    });