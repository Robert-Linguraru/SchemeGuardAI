import { request } from "../../../shared/api/httpClient";

export interface TransactionExplanation {
    transactionId: string;
    externalId: string;
    qualificationStatus: string;
    qualificationCategory: string | null;
    originalExplanation: string | null;
    llmExplanation: string;
    interchangeRate: number | string | null;
    interchangeFee: number | string | null;
    currencyCode: string;
}

export const getTransactionExplanation = (
    transactionId: string
): Promise<TransactionExplanation> => {
    return request<TransactionExplanation>(
        `/api/transactions/${transactionId}/explanation`,
        {
            method: "GET"
        }
    );
};