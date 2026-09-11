export interface RuleCondition {
    field: string;
    operator: string;
    value: unknown;
}

export interface RuleConditions {
    all: RuleCondition[];
}

export interface RuleFormValues {
    schemeId: string;
    ruleCode: string;
    ruleName: string;
    region: string;
    priority: string;
    conditions: RuleCondition[];
    qualificationCategory: string;
    interchangeRate: string;
    feeType: string;
    effectiveFrom: string;
    effectiveTo: string;
    version: string;
    active: boolean;
}

export interface RuleUploadRequest {
    scheme_id: string;
    rule_code: string;
    rule_name: string;
    region: string;
    priority: number;
    conditions: RuleConditions;
    result: {
        qualification_category: string;
        interchange_rate: number;
        fee_type: string;
    };
    effective_from: string;
    effective_to: string | null;
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

export interface RuleListResponse {
    id: string;
    ruleCode: string;
    ruleName: string;
    schemeCode: string;
    schemeName: string;
    region: string;
    qualificationCategory: string;
    interchangeRate: number;
    priority: number;
    version: number;
    active: boolean;
    effectiveFrom: string;
    effectiveTo: string | null;
}

export interface RuleDetailsResponse extends RuleListResponse {
    conditions: RuleConditions;
    createdAt: string;
}