export const getMockTransactions = async (token) => {
    const response = await fetch("/api/transactions/mock", {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`
        }
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(
            data?.message || "Could not load transactions"
        );
    }

    return data;
};