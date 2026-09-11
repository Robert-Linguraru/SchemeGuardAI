export type FieldErrors = Record<string, string | string[]>;

export class ApiError extends Error {
    readonly status: number | null;
    readonly errorCode?: string;
    readonly fieldErrors?: FieldErrors;

    constructor(
        message: string,
        status: number | null = null,
        errorCode?: string,
        fieldErrors?: FieldErrors
    ) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors;
    }
}