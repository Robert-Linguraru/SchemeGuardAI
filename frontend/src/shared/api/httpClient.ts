import { getAccessToken } from "../storage/authStorage";

interface ApiErrorResponse {
    message?: string;
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

    const response = await fetch(url, {
        ...requestOptions,
        body,
        headers: requestHeaders
    });
    const data = await parseResponse(response);

    if (!response.ok) {
        const errorData = data as ApiErrorResponse | null;
        throw new Error(errorData?.message || "Request failed");
    }

    return data as T;
};