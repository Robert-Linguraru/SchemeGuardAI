import type { AuthenticatedUser } from "../../../shared/types/auth";
import type {
    EntityListAction,
    EntityListColumn
} from "../../../shared/types/list";
import type { CardScheme } from "../types/cardScheme";

const isAdmin = (user: AuthenticatedUser | null) => user?.role === "ADMIN";

export const cardSchemeColumns: EntityListColumn<CardScheme>[] = [
    {
        key: "code",
        label: "Code",
        sortable: true
    },
    {
        key: "name",
        label: "Name",
        sortable: true
    },
    {
        key: "active",
        label: "Status",
        sortKey: "active",
        sortable: true,
        render: (scheme) => (
            <span className={`entity-list-status ${scheme.active ? "active" : "inactive"}`}>
                {scheme.active ? "Active" : "Inactive"}
            </span>
        )
    }
];

export interface CardSchemeActionHandlers {
    onEdit: (scheme: CardScheme) => void;
    onActivate: (scheme: CardScheme) => void | Promise<void>;
    onDeactivate: (scheme: CardScheme) => void | Promise<void>;
    onDelete: (scheme: CardScheme) => void | Promise<void>;
    isBusy: () => boolean;
}

export const createCardSchemeActions = ({
    onEdit,
    onActivate,
    onDeactivate,
    onDelete,
    isBusy
}: CardSchemeActionHandlers): EntityListAction<CardScheme>[] => [
    {
        key: "edit",
        label: "Edit",
        className: "entity-list-action-button",
        onClick: onEdit,
        isVisible: (_, user) => isAdmin(user),
        isDisabled: () => isBusy()
    },
    {
        key: "activate",
        label: "Activate",
        className: "entity-list-action-button entity-list-action-success",
        onClick: onActivate,
        isVisible: (scheme, user) => isAdmin(user) && !scheme.active,
        isDisabled: () => isBusy()
    },
    {
        key: "deactivate",
        label: "Deactivate",
        className: "entity-list-action-button entity-list-action-warning",
        onClick: onDeactivate,
        isVisible: (scheme, user) => isAdmin(user) && scheme.active,
        isDisabled: () => isBusy()
    },
    {
        key: "delete",
        label: "Delete",
        className: "entity-list-action-button entity-list-action-danger",
        onClick: onDelete,
        isVisible: (_, user) => isAdmin(user),
        isDisabled: () => isBusy()
    }
];
