import { request } from "../../../shared/api/httpClient";
import type {
    AuthResponse,
    LoginRequest,
    RegisterRequest
} from "../../../shared/types/auth";

export const login = (payload: LoginRequest): Promise<AuthResponse> =>
    request<AuthResponse>("/api/auth/login", {
        method: "POST",
        authenticated: false,
        body: JSON.stringify(payload)
    });

export const register = (payload: RegisterRequest): Promise<AuthResponse> =>
    request<AuthResponse>("/api/auth/register", {
        method: "POST",
        authenticated: false,
        body: JSON.stringify(payload)
    });

export const getMe = (): Promise<AuthResponse> =>
    request<AuthResponse>("/api/auth/me", { method: "GET" });