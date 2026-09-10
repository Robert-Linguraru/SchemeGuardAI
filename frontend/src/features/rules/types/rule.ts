export interface RuleCondition {
    field: string;
    operator: string;
    value: unknown;
}

export interface RuleConditions {
    all: RuleCondition[];
}

export interface RuleResult {
    qualificationCategory: string;
    interchangeRate: number;
    feeType: string;
}

export interface RuleUploadRequest {
    scheme_id: string;
    ruleCode: string;
    ruleName: string;
    region: string;
    priority: number;
    conditions: RuleConditions;
    result: RuleResult;
    effectiveFrom: string;
    effectiveTo: string | null;
    version: number;
    active: boolean;
}

export interface RuleUploadResponse {
    id: string;
    ruleCode: string;
    ruleName: string;
    qualificationCategory: string;
    interchangeRate: number;
}
