export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest extends LoginRequest {
    fullName: string;
}

export interface AuthResponse {
    accessToken: string | null;
    id: string;
    email: string;
    fullName: string;
    status: string;
}

export type AuthenticatedUser = Omit<AuthResponse, "accessToken">;