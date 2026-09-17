import { useState } from "react";
import { updateCardScheme } from "../api/cardSchemeApi";
import type { CardScheme } from "../types/cardScheme";

interface CardSchemeEditDialogProps {
    scheme: CardScheme;
    onClose: () => void;
    onSaved: (scheme: CardScheme) => void;
}

export function CardSchemeEditDialog({
    scheme,
    onClose,
    onSaved
}: CardSchemeEditDialogProps) {
    const [code, setCode] = useState(scheme.code);
    const [name, setName] = useState(scheme.name);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const save = async () => {
        setIsSaving(true);
        setError(null);

        try {
            const updatedScheme = await updateCardScheme(scheme.id, { code, name });
            onSaved(updatedScheme);
        } catch (requestError) {
            setError(
                requestError instanceof Error
                    ? requestError.message
                    : "Could not update the card scheme."
            );
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div
            className="entity-list-dialog-backdrop"
            role="presentation"
            onMouseDown={(event) => {
                if (event.target === event.currentTarget && !isSaving) {
                    onClose();
                }
            }}
        >
            <section
                className="entity-list-dialog"
                role="dialog"
                aria-modal="true"
                aria-labelledby="edit-card-scheme-title"
            >
                <div className="entity-list-dialog-heading">
                    <div>
                        <p className="eyebrow">CARD SCHEME</p>
                        <h2 id="edit-card-scheme-title">Edit card scheme</h2>
                    </div>
                    <button
                        type="button"
                        className="entity-list-dialog-close"
                        onClick={onClose}
                        disabled={isSaving}
                        aria-label="Close dialog"
                    >
                        ×
                    </button>
                </div>

                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        void save();
                    }}
                >
                    <label>
                        Code
                        <input
                            value={code}
                            onChange={(event) => setCode(event.target.value)}
                            required
                            maxLength={50}
                            disabled={isSaving}
                        />
                    </label>
                    <label>
                        Name
                        <input
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                            maxLength={150}
                            disabled={isSaving}
                        />
                    </label>

                    {error && <p className="form-error">{error}</p>}

                    <div className="entity-list-dialog-actions">
                        <button
                            type="button"
                            className="secondary-button"
                            onClick={onClose}
                            disabled={isSaving}
                        >
                            Cancel
                        </button>
                        <button type="submit" className="create-rule-button" disabled={isSaving}>
                            {isSaving ? "Saving..." : "Save changes"}
                        </button>
                    </div>
                </form>
            </section>
        </div>
    );
}
