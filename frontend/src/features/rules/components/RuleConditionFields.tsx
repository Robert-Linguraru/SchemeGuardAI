import type { RuleCondition } from "../types/rule";

interface RuleConditionFieldsProps {
    conditions: RuleCondition[];
    onChange: (conditions: RuleCondition[]) => void;
}

const fieldOptions = [
    "cardType",
    "cardCategory",
    "channel",
    "merchantDataComplete",
    "clearingDelayMaxDays",
    "threeDsUsed",
    "cvvPresent"
];

const operatorOptions = ["EQUALS", "NOT_EQUALS", "GREATER_THAN", "LESS_THAN"];

export function RuleConditionFields({ conditions, onChange }: RuleConditionFieldsProps) {
    const updateCondition = (index: number, changes: Partial<RuleCondition>) => {
        onChange(conditions.map((condition, conditionIndex) =>
            conditionIndex === index ? { ...condition, ...changes } : condition
        ));
    };

    const removeCondition = (index: number) => {
        onChange(conditions.filter((_, conditionIndex) => conditionIndex !== index));
    };

    return (
        <fieldset className="conditions-fieldset">
            <legend>Conditions</legend>
            <p className="field-help">Every condition is combined in the backend as an item in <code>conditions.all</code>.</p>
            <div className="condition-list">
                {conditions.map((condition, index) => (
                    <div className="condition-row" key={`${index}-${condition.field}`}>
                        <label>
                            Field
                            <select
                                value={condition.field}
                                onChange={(event) => updateCondition(index, { field: event.target.value })}
                            >
                                <option value="">Select a field</option>
                                {fieldOptions.map((field) => <option key={field} value={field}>{field}</option>)}
                            </select>
                        </label>
                        <label>
                            Operator
                            <select
                                value={condition.operator}
                                onChange={(event) => updateCondition(index, { operator: event.target.value })}
                            >
                                <option value="">Select an operator</option>
                                {operatorOptions.map((operator) => <option key={operator} value={operator}>{operator}</option>)}
                            </select>
                        </label>
                        <label>
                            Value
                            <input
                                value={String(condition.value)}
                                placeholder="Value"
                                onChange={(event) => updateCondition(index, { value: event.target.value })}
                            />
                        </label>
                        <button
                            type="button"
                            className="remove-condition"
                            aria-label={`Remove condition ${index + 1}`}
                            onClick={() => removeCondition(index)}
                        >
                            Remove
                        </button>
                    </div>
                ))}
            </div>
            <button
                type="button"
                className="secondary-button"
                onClick={() => onChange([...conditions, { field: "", operator: "", value: "" }])}
            >
                Add condition
            </button>
        </fieldset>
    );
}
