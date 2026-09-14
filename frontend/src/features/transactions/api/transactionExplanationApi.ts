import { request } from "../../../shared/api/httpClient";

export interface TransactionExplanation {
    transactionId: string;
    externalId: string;
    qualificationStatus: string;
    originalExplanation: string | null;
    llmExplanation: string;
    estimatedFee: number | string | null;
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