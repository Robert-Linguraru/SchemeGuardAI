import { useEffect, useState } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { deleteRule, getRule } from "../api/rulesApi";
import { getRuleErrorMessage } from "../api/ruleErrors";
import type {
    RuleCondition,
    RuleDetailsResponse
} from "../types/rule";
import { AccountMenu } from "../../../shared/components/AccountMenu";

interface RuleDetailsPageProps {
    ruleId: string;
    onBack: () => void;
    onDeleted: () => void;
    onEditProfile: () => void;
}

const labelize = (value: string) =>
    value
        .replace(/([A-Z])/g, " $1")
        .replace(/_/g, " ")
        .replace(/^./, (char) => char.toUpperCase());

const formatValue = (value: unknown) => {
    if (typeof value === "boolean") {
        return value ? "Yes" : "No";
    }

    return String(value);
};

export function RuleDetailsPage({
                                    ruleId,
                                    onBack,
                                    onDeleted,
                                    onEditProfile
                                }: RuleDetailsPageProps) {
    const { user } = useAuth();
    const [rule, setRule] = useState<RuleDetailsResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        getRule(ruleId)
            .then(setRule)
            .catch((error) => {
                setError(getRuleErrorMessage(error));
            });
    }, [ruleId]);

    const handleDelete = async () => {
        if (!window.confirm("Are you sure you want to delete this rule?")) {
            return;
        }

        try {
            await deleteRule(ruleId);
            onDeleted();
        } catch (error) {
            setError(getRuleErrorMessage(error));
        }
    };

    const conditions: RuleCondition[] = rule?.conditions?.all ?? [];

    return (
        <main className="dashboard rules-page">
            <header className="dashboard-header">
                <div className="welcome">
                        <h1>SchemeGuard AI</h1>
                        <p>Welcome, {user?.fullName}</p>
                </div>
                <AccountMenu onEditProfile={onEditProfile} />
            </header>

            <section className="content">
                <button
                    className="page-nav-button"
                    onClick={onBack}
                >
                    Back to rules
                </button>

                {error && (
                    <div className="form-error" role="alert">
                        {error}
                    </div>
                )}

                {!rule && !error && (
                    <div className="rule-loading">
                        Loading rule...
                    </div>
                )}

                {rule && (
                    <article className="rule-detail-card">
                        <div className="rule-detail-header">
                            <div>
                                <p className="eyebrow">
                                    {rule.ruleCode}
                                </p>
                                <h2>{rule.ruleName}</h2>
                                <p className="rule-scheme">
                                    {rule.schemeName} · {rule.schemeCode}
                                </p>
                            </div>

                            <span className={
                                      rule.active
                                          ? "rule-status"
                                          : "rule-status inactive"
                                  }>
                                {rule.active ? "Active" : "Inactive"}
                            </span>
                        </div>

                        <section className="detail-section">
                            <h3>Rule information</h3>

                            <div className="details-grid">
                                <div>
                                    <span>Region</span>
                                    <strong>{rule.region}</strong>
                                </div>

                                <div>
                                    <span>Qualification category</span>
                                    <strong>
                                        {rule.qualificationCategory}
                                    </strong>
                                </div>

                                <div>
                                    <span>Interchange rate</span>
                                    <strong>
                                        {(rule.interchangeRate * 100).toFixed(2)}%
                                    </strong>
                                </div>

                                <div>
                                    <span>Priority</span>
                                    <strong>{rule.priority}</strong>
                                </div>

                                <div>
                                    <span>Version</span>
                                    <strong>v{rule.version}</strong>
                                </div>

                                <div>
                                    <span>Effective period</span>
                                    <strong>
                                        {rule.effectiveFrom}
                                        {rule.effectiveTo
                                            ? ` – ${rule.effectiveTo}`
                                            : " – No end date"}
                                    </strong>
                                </div>

                                <div className="detail-wide">
                                    <span>Rule ID</span>
                                    <strong>{rule.id}</strong>
                                </div>

                                <div className="detail-wide">
                                    <span>Created at</span>
                                    <strong>
                                        {new Date(rule.createdAt).toLocaleString()}
                                    </strong>
                                </div>
                            </div>
                        </section>

                        <section className="detail-section">
                            <h3>Conditions</h3>
                            <p className="field-help">
                                Every condition must be satisfied.
                            </p>

                            <div className="condition-details-list">
                                {conditions.map((condition, index) => (
                                    <div
                                        className="condition-detail-row"
                                        key={`${condition.field}-${index}`}
                                    >
                                        <span className="condition-number">
                                            {index + 1}
                                        </span>

                                        <strong>
                                            {labelize(condition.field)}
                                        </strong>

                                        <span className="condition-operator">
                                            {labelize(condition.operator)}
                                        </span>

                                        <span className="condition-value">
                                            {formatValue(condition.value)}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </section>

                        <div className="detail-actions">
                            <button
                                className="danger-button"
                                onClick={handleDelete}
                            >
                                Delete rule
                            </button>
                        </div>
                    </article>
                )}
            </section>
        </main>
    );
}
