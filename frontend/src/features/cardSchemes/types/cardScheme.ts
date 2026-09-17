export interface CardScheme {
    id: string;
    code: string;
    name: string;
    active: boolean;
}

export interface CardSchemeListApiResponse {
    cardSchemes: CardScheme[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    search: string | null;
    sort: string;
    includeInactive: boolean;
}

export interface CardSchemeMutationResponse {
    cardScheme: CardScheme;
}
