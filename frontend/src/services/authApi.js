const apiRequest = async (url, options = {}) => {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    const data = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(data?.message || "Request failed");
    }

    return data;
};

export const register = (payload) =>
    apiRequest("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(payload)
    });

export const login = (payload) =>
    apiRequest("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(payload)
    });

export const getMe = (token) =>
    apiRequest("/api/auth/me", {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`
        }
    });