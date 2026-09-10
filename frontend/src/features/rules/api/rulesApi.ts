import { request } from "../../../shared/api/httpClient";
import type {
    RuleCondition,
    RuleFormValues,
    RuleUploadRequest,
    RuleUploadResponse
} from "../types/rule";

const booleanFields = new Set(["merchantDataComplete", "threeDsUsed", "cvvPresent"]);
const numericFields = new Set(["clearingDelayMaxDays"]);

const toBackendCondition = (condition: RuleCondition): RuleCondition => {
    if (booleanFields.has(condition.field)) {
        return { ...condition, value: String(condition.value).toLowerCase() === "true" };
    }
    if (numericFields.has(condition.field)) {
        return { ...condition, value: Number(condition.value) };
    }
    return condition;
};

export const toRuleUploadRequest = (rule: RuleFormValues): RuleUploadRequest => ({
    scheme_id: rule.schemeId.trim(),
    rule_code: rule.ruleCode.trim(),
    rule_name: rule.ruleName.trim(),
    region: rule.region,
    priority: Number(rule.priority),
    conditions: { all: rule.conditions.map(toBackendCondition) },
    result: {
        qualification_category: rule.qualificationCategory.trim(),
        interchange_rate: Number(rule.interchangeRate),
        fee_type: rule.feeType.trim()
    },
    effective_from: rule.effectiveFrom,
    effective_to: rule.effectiveTo || null,
    version: Number(rule.version),
    active: rule.active
});

export const uploadRule = (
    rule: RuleFormValues
): Promise<RuleUploadResponse> =>
    request<RuleUploadResponse>("/api/rule/upload", {
        method: "POST",
        body: JSON.stringify(toRuleUploadRequest(rule))
    });
