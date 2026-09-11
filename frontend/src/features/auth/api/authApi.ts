import { request } from "../../../shared/api/httpClient";
import type {
    AuthResponse,
    LoginRequest,
    RegisterRequest
} from "../../../shared/types/auth";

export interface UpdateProfileRequest {
    email: string;
    fullName: string;
    password?: string;
}

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

export const updateProfile = (payload: UpdateProfileRequest): Promise<AuthResponse> =>
    request<AuthResponse>("/api/auth/profile", {
        method: "PUT",
        body: JSON.stringify(payload)
    });
