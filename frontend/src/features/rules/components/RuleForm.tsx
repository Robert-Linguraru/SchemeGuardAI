import { useState, type FormEvent } from "react";
import { uploadRule } from "../api/rulesApi";
import { RuleConditionFields } from "./RuleConditionFields";
import type { RuleCondition, RuleUploadRequest, RuleUploadResponse } from "../types/rule";

interface RuleFormProps {
    onSuccess: (response: RuleUploadResponse) => void;
}

interface RuleFormValues {
    schemeId: string;
    ruleCode: string;
    ruleName: string;
    region: string;
    priority: string;
    qualificationCategory: string;
    interchangeRate: string;
    feeType: string;
    effectiveFrom: string;
    effectiveTo: string;
    version: string;
    active: boolean;
}

const initialValues: RuleFormValues = {
    schemeId: "",
    ruleCode: "",
    ruleName: "",
    region: "EU",
    priority: "0",
    qualificationCategory: "",
    interchangeRate: "0",
    feeType: "PERCENTAGE",
    effectiveFrom: "",
    effectiveTo: "",
    version: "1",
    active: true
};

const initialConditions: RuleCondition[] = [{ field: "", operator: "", value: "" }];
const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const booleanFields = new Set(["merchantDataComplete", "threeDsUsed", "cvvPresent"]);
const numericFields = new Set(["clearingDelayMaxDays"]);

const valueForRequest = (condition: RuleCondition): unknown => {
    if (booleanFields.has(condition.field)) {
        return String(condition.value).toLowerCase() === "true";
    }
    if (numericFields.has(condition.field)) {
        return Number(condition.value);
    }
    return condition.value;
};

export function RuleForm({ onSuccess }: RuleFormProps) {
    const [values, setValues] = useState(initialValues);
    const [conditions, setConditions] = useState(initialConditions);
    const [errors, setErrors] = useState<string[]>([]);
    const [requestError, setRequestError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const updateValue = <Key extends keyof RuleFormValues>(key: Key, value: RuleFormValues[Key]) => {
        setValues((currentValues) => ({ ...currentValues, [key]: value }));
    };

    const reset = () => {
        setValues(initialValues);
        setConditions(initialConditions);
        setErrors([]);
        setRequestError(null);
    };

    const validate = (): string[] => {
        const validationErrors: string[] = [];
        const priority = Number(values.priority);
        const interchangeRate = Number(values.interchangeRate);
        const version = Number(values.version);

        if (!uuidPattern.test(values.schemeId.trim())) validationErrors.push("Card scheme ID must be a valid UUID.");
        if (!values.ruleCode.trim()) validationErrors.push("Rule code is required.");
        if (!values.ruleName.trim()) validationErrors.push("Rule name is required.");
        if (!["EU", "US", "UK"].includes(values.region)) validationErrors.push("Region must be EU, US or UK.");
        if (!Number.isFinite(priority) || priority < 0) validationErrors.push("Priority must be a non-negative number.");
        if (conditions.length === 0) validationErrors.push("Add at least one condition.");
        conditions.forEach((condition, index) => {
            if (!condition.field.trim() || !condition.operator.trim() || String(condition.value).trim() === "") {
                validationErrors.push(`Condition ${index + 1} needs a field, operator and value.`);
            }
            if ((numericFields.has(condition.field) && !Number.isFinite(Number(condition.value)))) {
                validationErrors.push(`Condition ${index + 1} must use a numeric value.`);
            }
            if (booleanFields.has(condition.field) && !["true", "false"].includes(String(condition.value).toLowerCase())) {
                validationErrors.push(`Condition ${index + 1} must use true or false.`);
            }
        });
        if (!values.qualificationCategory.trim()) validationErrors.push("Qualification category is required.");
        if (!Number.isFinite(interchangeRate) || interchangeRate < 0) validationErrors.push("Interchange rate must be non-negative.");
        if (!values.feeType.trim()) validationErrors.push("Fee type is required.");
        if (!values.effectiveFrom) validationErrors.push("Effective-from date is required.");
        if (values.effectiveTo && values.effectiveFrom && values.effectiveTo < values.effectiveFrom) validationErrors.push("Effective-to cannot be earlier than effective-from.");
        if (!Number.isInteger(version) || version < 1) validationErrors.push("Version must be a positive integer.");
        return validationErrors;
    };

    const submit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setRequestError(null);
        const validationErrors = validate();
        setErrors(validationErrors);
        if (validationErrors.length > 0) return;

        const payload: RuleUploadRequest = {
            scheme_id: values.schemeId.trim(),
            ruleCode: values.ruleCode.trim(),
            ruleName: values.ruleName.trim(),
            region: values.region,
            priority: Number(values.priority),
            conditions: { all: conditions.map((condition) => ({ ...condition, value: valueForRequest(condition) })) },
            result: {
                qualificationCategory: values.qualificationCategory.trim(),
                interchangeRate: Number(values.interchangeRate),
                feeType: values.feeType.trim()
            },
            effectiveFrom: values.effectiveFrom,
            effectiveTo: values.effectiveTo || null,
            version: Number(values.version),
            active: values.active
        };

        setIsSubmitting(true);
        try {
            const response = await uploadRule(payload);
            onSuccess(response);
        } catch (error) {
            setRequestError(error instanceof Error ? error.message : "Could not create the rule.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form className="rule-form" onSubmit={submit}>
            <section className="form-section">
                <div className="form-section-heading"><p className="eyebrow">IDENTITY</p><h3>Rule details</h3></div>
                <label>Card scheme ID
                    <input value={values.schemeId} onChange={(event) => updateValue("schemeId", event.target.value)} placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" />
                    <span className="field-help">Temporary input: use a valid UUID from the card_schemes table. This will become a scheme dropdown after a card-scheme lookup endpoint is available.</span>
                </label>
                <div className="form-grid">
                    <label>Rule code<input value={values.ruleCode} onChange={(event) => updateValue("ruleCode", event.target.value)} /></label>
                    <label>Rule name<input value={values.ruleName} onChange={(event) => updateValue("ruleName", event.target.value)} /></label>
                    <label>Region<select value={values.region} onChange={(event) => updateValue("region", event.target.value)}><option>EU</option><option>US</option><option>UK</option></select></label>
                    <label>Priority<input type="number" min="0" value={values.priority} onChange={(event) => updateValue("priority", event.target.value)} /></label>
                </div>
            </section>
            <RuleConditionFields conditions={conditions} onChange={setConditions} />
            <section className="form-section">
                <div className="form-section-heading"><p className="eyebrow">OUTCOME</p><h3>Qualification result</h3></div>
                <div className="form-grid">
                    <label>Qualification category<input value={values.qualificationCategory} onChange={(event) => updateValue("qualificationCategory", event.target.value)} /></label>
                    <label>Interchange rate<input type="number" min="0" step="0.0001" value={values.interchangeRate} onChange={(event) => updateValue("interchangeRate", event.target.value)} /></label>
                    <label>Fee type<input value={values.feeType} onChange={(event) => updateValue("feeType", event.target.value)} /></label>
                    <label>Version<input type="number" min="1" step="1" value={values.version} onChange={(event) => updateValue("version", event.target.value)} /></label>
                    <label>Effective from<input type="date" value={values.effectiveFrom} onChange={(event) => updateValue("effectiveFrom", event.target.value)} /></label>
                    <label>Effective to <span className="optional">(optional)</span><input type="date" value={values.effectiveTo} onChange={(event) => updateValue("effectiveTo", event.target.value)} /></label>
                </div>
                <label className="checkbox-label"><input type="checkbox" checked={values.active} onChange={(event) => updateValue("active", event.target.checked)} /> Active</label>
            </section>
            {errors.length > 0 && <div className="form-error" role="alert"><strong>Check the form</strong><ul>{errors.map((error) => <li key={error}>{error}</li>)}</ul></div>}
            {requestError && <div className="form-error" role="alert"><strong>Could not create rule</strong><span>{requestError}</span></div>}
            <div className="form-actions"><button type="submit" disabled={isSubmitting}>{isSubmitting ? "Creating rule..." : "Create rule"}</button><button type="button" className="secondary-button" onClick={reset} disabled={isSubmitting}>Reset</button></div>
        </form>
    );
}
