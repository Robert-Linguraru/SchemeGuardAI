import type { ReactNode } from "react";
import type { AuthenticatedUser } from "./auth";

export interface EntityListQuery {
    page: number;
    size: number;
    search: string;
    sort: string;
    [key: string]: string | number | boolean | undefined;
}

export interface EntityListResponse<T> {
    items: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export type EntityListFetcher<T> = (
    query: EntityListQuery,
    signal?: AbortSignal
) => Promise<EntityListResponse<T>>;

export interface EntityListColumn<T> {
    key: string;
    label: string;
    sortable?: boolean;
    sortKey?: string;
    render?: (item: T) => ReactNode;
}

export interface EntityListAction<T> {
    key: string;
    label: string;
    className?: string;
    onClick: (item: T) => void | Promise<void>;
    isVisible?: (item: T, user: AuthenticatedUser | null) => boolean;
    isDisabled?: (item: T, user: AuthenticatedUser | null) => boolean;
}
