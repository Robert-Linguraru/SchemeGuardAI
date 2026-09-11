import {
    createContext,
    useContext,
    useEffect,
    useState,
    type ReactNode
} from "react";
import { getMe, login as loginRequest, register as registerRequest, updateProfile as updateProfileRequest } from "../features/auth/api/authApi";
import {
    clearAccessToken,
    getAccessToken,
    setAccessToken
} from "../shared/storage/authStorage";
import type {
    AuthenticatedUser,
    LoginRequest,
    RegisterRequest
} from "../shared/types/auth";

interface AuthContextValue {
    user: AuthenticatedUser | null;
    isLoading: boolean;
    error: string | null;
    login: (payload: LoginRequest) => Promise<void>;
    register: (payload: RegisterRequest) => Promise<void>;
    updateProfile: (payload: { fullName: string; email: string; password?: string }) => Promise<void>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const toUser = (response: Awaited<ReturnType<typeof getMe>>): AuthenticatedUser => {
    const { accessToken: _accessToken, ...user } = response;
    return user;
};

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthenticatedUser | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;
        const token = getAccessToken();

        if (!token) {
            setIsLoading(false);
            return () => {
                isMounted = false;
            };
        }

        getMe()
            .then((response) => {
                if (isMounted) {
                    setUser(toUser(response));
                }
            })
            .catch(() => {
                clearAccessToken();
                if (isMounted) {
                    setUser(null);
                }
            })
            .finally(() => {
                if (isMounted) {
                    setIsLoading(false);
                }
            });

        return () => {
            isMounted = false;
        };
    }, []);

    const authenticate = async (
        action: () => ReturnType<typeof loginRequest>
    ): Promise<void> => {
        setError(null);
        try {
            const response = await action();
            if (!response.accessToken) {
                throw new Error("Authentication response did not include a token");
            }
            setAccessToken(response.accessToken);
            setUser(toUser(response));
        } catch (authenticationError) {
            const message = authenticationError instanceof Error
                ? authenticationError.message
                : "Authentication failed";
            setError(message);
        }
    };

    const login = (payload: LoginRequest) => authenticate(() => loginRequest(payload));
    const register = (payload: RegisterRequest) => authenticate(() => registerRequest(payload));
    const updateProfile = async (payload: { fullName: string; email: string; password?: string }) => {
        setError(null);
        try {
            const response = await updateProfileRequest(payload);
            setUser(toUser(response));
        } catch (profileError) {
            const message = profileError instanceof Error
                ? profileError.message
                : "Could not update profile";
            setError(message);
            throw profileError;
        }
    };
    const logout = () => {
        clearAccessToken();
        setUser(null);
        setError(null);
    };

    return (
        <AuthContext.Provider value={{ user, isLoading, error, login, register, updateProfile, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}
