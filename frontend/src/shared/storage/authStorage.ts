const ACCESS_TOKEN_KEY = "accessToken";

export const getAccessToken = (): string | null =>
    localStorage.getItem(ACCESS_TOKEN_KEY);

export const setAccessToken = (token: string): void => {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
};

export const clearAccessToken = (): void => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
};

export const hasAccessToken = (): boolean => getAccessToken() !== null;