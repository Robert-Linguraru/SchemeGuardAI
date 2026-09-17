import { useCallback, useMemo, useState } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { AccountMenu } from "../../../shared/components/AccountMenu";
import { EntityList } from "../../../shared/components/EntityList/EntityList";
import type { EntityListQuery } from "../../../shared/types/list";
import {
    activateCardScheme,
    deleteCardScheme,
    deactivateCardScheme,
    getCardSchemes
} from "../api/cardSchemeApi";
import { createCardSchemeActions, cardSchemeColumns } from "../config/cardSchemeListConfig";
import { CardSchemeEditDialog } from "../components/CardSchemeEditDialog";
import type { CardScheme } from "../types/cardScheme";

interface CardSchemesPageProps {
    onBack: () => void;
    onEditProfile: () => void;
}

export function CardSchemesPage({ onBack, onEditProfile }: CardSchemesPageProps) {
    const { user } = useAuth();
    const [editingScheme, setEditingScheme] = useState<CardScheme | null>(null);
    const [actionError, setActionError] = useState<string | null>(null);
    const [actionSuccess, setActionSuccess] = useState<string | null>(null);
    const [pendingAction, setPendingAction] = useState<string | null>(null);
    const [reloadKey, setReloadKey] = useState(0);

    const fetchCardSchemes = useCallback(
        (query: EntityListQuery, signal?: AbortSignal) => getCardSchemes(query, signal),
        []
    );
    const runAction = async (
        actionKey: string,
        action: () => Promise<void>,
        successMessage: string
    ) => {
        setPendingAction(actionKey);
        setActionError(null);
        setActionSuccess(null);

        try {
            await action();
            setActionSuccess(successMessage);
            setReloadKey((currentKey) => currentKey + 1);
        } catch (requestError) {
            setActionError(
                requestError instanceof Error
                    ? requestError.message
                    : "The card-scheme action could not be completed."
            );
        } finally {
            setPendingAction(null);
        }
    };

    const actions = useMemo(() => createCardSchemeActions({
        onEdit: (scheme) => {
            setActionError(null);
            setActionSuccess(null);
            setEditingScheme(scheme);
        },
        onActivate: (scheme) => {
            const cascade = window.confirm(
                "Activate the card scheme and all associated rules, transactions and results?\n\nChoose Cancel if you only want to activate the scheme."
            );

            return runAction(
                `activate-${scheme.id}`,
                () => activateCardScheme(scheme.id, cascade),
                cascade ? "Card scheme and associated entities activated." : "Card scheme activated."
            );
        },
        onDeactivate: (scheme) => {
            const confirmed = window.confirm(
                "Deactivate this card scheme and all associated rules, transactions and results?"
            );

            return confirmed
                ? runAction(
                    `deactivate-${scheme.id}`,
                    () => deactivateCardScheme(scheme.id),
                    "Card scheme and associated entities deactivated."
                )
                : Promise.resolve();
        },
        onDelete: (scheme) => {
            const confirmed = window.confirm(
                `Permanently delete ${scheme.name} and all associated data? This cannot be undone.`
            );

            return confirmed
                ? runAction(
                    `delete-${scheme.id}`,
                    () => deleteCardScheme(scheme.id),
                    "Card scheme and associated data deleted."
                )
                : Promise.resolve();
        },
        isBusy: () => pendingAction !== null
    }), [pendingAction]);

    return (
        <main className="dashboard card-scheme-page">
            <header className="dashboard-header">
                <div className="welcome">
                    <h1>SchemeGuard AI</h1>
                    <p>Welcome, {user?.fullName}</p>
                </div>
                <div className="dashboard-account-actions">
                    <span className="role-badge">{user?.status} · {user?.role}</span>
                    <AccountMenu onEditProfile={onEditProfile} />
                </div>
            </header>

            <section className="content">
                <div className="page-heading">
                    <div>
                        <p className="eyebrow">CARD SCHEMES WORKSPACE</p>
                        <h2>Card schemes</h2>
                        <p>Search, sort and manage payment card schemes.</p>
                    </div>
                    <div className="heading-actions">
                        <button type="button" className="page-nav-button" onClick={onBack}>
                            Transaction dashboard
                        </button>
                    </div>
                </div>

                {actionError && <div className="form-error" role="alert">{actionError}</div>}
                {actionSuccess && <div className="entity-list-success" role="status">{actionSuccess}</div>}

                <EntityList
                    title="All card schemes"
                    description="Active schemes are shown by default. Administrators can include inactive schemes."
                    columns={cardSchemeColumns}
                    fetchData={fetchCardSchemes}
                    actions={actions}
                    user={user}
                    reloadKey={reloadKey}
                    initialQuery={{ includeInactive: false }}
                    searchPlaceholder="Search by code or name..."
                    emptyMessage="No card schemes found."
                    toolbar={({ query, updateQuery }) => user?.role === "ADMIN" ? (
                        <label className="entity-list-checkbox">
                            <input
                                type="checkbox"
                                checked={query.includeInactive === true}
                                onChange={(event) => updateQuery({
                                    includeInactive: event.target.checked,
                                    page: 0
                                })}
                            />
                            <span>Include inactive</span>
                        </label>
                    ) : null}
                />
            </section>

            {editingScheme && (
                <CardSchemeEditDialog
                    scheme={editingScheme}
                    onClose={() => setEditingScheme(null)}
                    onSaved={(updatedScheme) => {
                        setEditingScheme(null);
                        setActionSuccess(`${updatedScheme.name} was updated.`);
                        setReloadKey((currentKey) => currentKey + 1);
                    }}
                />
            )}
        </main>
    );
}
