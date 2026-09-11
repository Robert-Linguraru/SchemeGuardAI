import { getAccessToken } from "../storage/authStorage";
import { ApiError, type FieldErrors } from "./ApiError";

interface ApiErrorResponse {
    message?: string;
    errorCode?: string;
    code?: string;
    fieldErrors?: FieldErrors;
}

export interface HttpRequestOptions extends RequestInit {
    authenticated?: boolean;
}

const parseResponse = async (response: Response): Promise<unknown> => {
    const contentType = response.headers.get("content-type") ?? "";

    if (!contentType.includes("application/json")) {
        return null;
    }

    return response.json().catch(() => null);
};

export const request = async <T>(
    url: string,
    options: HttpRequestOptions = {}
): Promise<T> => {
    const { authenticated = true, headers, body, ...requestOptions } = options;
    const token = authenticated ? getAccessToken() : null;
    const requestHeaders = new Headers(headers);

    if (body !== undefined && !requestHeaders.has("Content-Type")) {
        requestHeaders.set("Content-Type", "application/json");
    }

    if (token) {
        requestHeaders.set("Authorization", `Bearer ${token}`);
    }

    let response: Response;
    try {
        response = await fetch(url, {
            ...requestOptions,
            body,
            headers: requestHeaders
        });
    } catch (error) {
        throw new ApiError(
            error instanceof Error ? error.message : "Network request failed"
        );
    }
    const data = await parseResponse(response);

    if (!response.ok) {
        const errorData = data as ApiErrorResponse | null;
        throw new ApiError(
            errorData?.message || "Request failed",
            response.status,
            errorData?.errorCode || errorData?.code,
            errorData?.fieldErrors
        );
    }

    return data as T;
};