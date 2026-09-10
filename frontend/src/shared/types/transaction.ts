export interface Transaction {
    id: string;
    externalId: string;
    merchantName: string;
    scheme: string;
    amount: number | string | null;
    currencyCode: string;
    cardType: string | null;
    cardCategory: string | null;
    channel: string | null;
    issuerCountry: string | null;
    merchantCountry: string | null;
    status: string;
    qualificationStatus: string;
    estimatedFee: number | string | null;
    explanation: string | null;
    authorizedAt: string | null;
}