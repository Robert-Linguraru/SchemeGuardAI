import { request } from "../../../shared/api/httpClient";
import type { RuleUploadRequest, RuleUploadResponse } from "../types/rule";

export const uploadRule = (
    rule: RuleUploadRequest
): Promise<RuleUploadResponse> =>
    request<RuleUploadResponse>("/api/rule/upload", {
        method: "POST",
        body: JSON.stringify(rule)
    });
