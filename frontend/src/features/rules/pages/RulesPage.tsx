import { useEffect, useState } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { deleteRule, getRules } from "../api/rulesApi";
import { getRuleErrorMessage } from "../api/ruleErrors";
import type { RuleListResponse } from "../types/rule";
import { AccountMenu } from "../../../shared/components/AccountMenu";

interface RulesPageProps {
    onBack: () => void;
    onCreateRule: () => void;
    onOpenRule: (id: string) => void;
    onEditProfile: () => void;
}

export function RulesPage({
                              onBack,
                              onCreateRule,
                              onOpenRule,
                              onEditProfile
                          }: RulesPageProps) {
    const { user } = useAuth();
    const [rules, setRules] = useState<RuleListResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const loadRules = async () => {
        try {
            setLoading(true);
            setError(null);
            const result = await getRules();
            setRules(result);
        } catch (error) {
            setError(getRuleErrorMessage(error));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRules();
    }, []);

    const handleDelete = async (id: string) => {
        const confirmed = window.confirm(
            "Are you sure you want to delete this rule?"
        );

        if (!confirmed) return;

        try {
            await deleteRule(id);
            setRules((currentRules) =>
                currentRules.filter((rule) => rule.id !== id)
            );
        } catch (error) {
            setError(getRuleErrorMessage(error));
        }
    };

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
                <div className="page-heading">
                    <div>
                        <p className="eyebrow">RULES WORKSPACE</p>
                        <h2>All rules</h2>
                        <p>View and manage qualification rules.</p>
                    </div>

                    <div className="heading-actions">
                        <button
                            className="page-nav-button"
                            onClick={onBack}
                        >
                            Transaction dashboard
                        </button>

                        <button
                            className="create-rule-button"
                            onClick={onCreateRule}
                        >
                            Create rule
                        </button>
                    </div>
                </div>

                {loading && <p>Loading rules...</p>}

                {error && (
                    <div className="form-error" role="alert">
                        {error}
                    </div>
                )}

                {!loading && !error && rules.length === 0 && (
                    <div className="empty-state">
                        No rules found.
                    </div>
                )}

                <div className="rules-list">
                    {rules.map((rule) => (
                        <article className="rule-card" key={rule.id}>
                            <div
                                className="rule-card-content"
                                onClick={() => onOpenRule(rule.id)}
                                role="button"
                                tabIndex={0}
                                onKeyDown={(event) => {
                                    if (event.key === "Enter") {
                                        onOpenRule(rule.id);
                                    }
                                }}
                            >
                                <div className="rule-card-top">
            <span className="rule-code">
                {rule.ruleCode}
            </span>

                                    <span className={rule.active ? "rule-status" : "rule-status inactive"}>
                {rule.active ? "Active" : "Inactive"}
            </span>
                                </div>

                                <h3>{rule.ruleName}</h3>

                                <p className="rule-scheme">
                                    {rule.schemeName} · {rule.schemeCode}
                                </p>

                                <p className="rule-category">
                                    {rule.qualificationCategory} · {rule.region}
                                </p>

                                <div className="rule-card-meta">
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
                                </div>

                                <p className="rule-validity">
                                    Valid from {rule.effectiveFrom}
                                    {rule.effectiveTo
                                        ? ` until ${rule.effectiveTo}`
                                        : " · No end date"}
                                </p>
                            </div>

                            <div className="rule-card-actions">
                                <button
                                    className="view-rule-button"
                                    onClick={() => onOpenRule(rule.id)}
                                >
                                    View details
                                </button>

                                <button
                                    className="danger-button"
                                    onClick={() => handleDelete(rule.id)}
                                >
                                    Delete
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            </section>
        </main>
    );
}
