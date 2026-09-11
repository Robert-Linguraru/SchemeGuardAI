import { ApiError } from "../../../shared/api/ApiError";

export const getRuleErrorMessage = (error: unknown): string => {
    if (!(error instanceof ApiError)) {
        return "The server is unavailable. Check your connection and try again.";
    }

    if (error.status === 400) return "Some rule fields are invalid. Please review the form.";
    if (error.status === 401) return "Your session has expired. Please log in again.";
    if (error.status === 403) return "You do not have permission to perform this action.";
    if (error.status === 404) {
        return error.errorCode === "CARD_SCHEME_NOT_FOUND"
            ? "The selected card scheme could not be found."
            : "This rule could not be found.";
    }
    if (error.status === 409) return "A rule with this code already exists.";
    if (error.status === 500) return "The rule could not be processed. Please try again.";

    return "The server is unavailable. Check your connection and try again.";
};