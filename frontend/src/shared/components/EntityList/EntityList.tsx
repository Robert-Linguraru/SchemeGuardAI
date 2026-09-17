import { useEffect, useMemo, useState, type ReactNode } from "react";
import { ErrorMessage } from "../ErrorMessage";
import { LoadingState } from "../LoadingState";
import { useEntityList } from "../../hooks/useEntityList";
import type { AuthenticatedUser } from "../../types/auth";
import type {
    EntityListAction,
    EntityListColumn,
    EntityListFetcher,
    EntityListQuery
} from "../../types/list";

export interface EntityListToolbarContext {
    query: EntityListQuery;
    updateQuery: (changes: Partial<EntityListQuery>) => void;
}

interface EntityListProps<T> {
    title: string;
    description?: string;
    columns: EntityListColumn<T>[];
    fetchData: EntityListFetcher<T>;
    actions?: EntityListAction<T>[];
    user?: AuthenticatedUser | null;
    initialQuery?: Partial<EntityListQuery>;
    reloadKey?: number;
    searchPlaceholder?: string;
    emptyMessage?: string;
    toolbar?: (context: EntityListToolbarContext) => ReactNode;
}

const getSortState = (sort: string) => {
    const [field, direction] = sort.split(",", 2);
    return {
        field,
        direction: direction === "desc" ? "desc" : "asc"
    } as const;
};

export function EntityList<T>({
    title,
    description,
    columns,
    fetchData,
    actions = [],
    user = null,
    initialQuery,
    reloadKey = 0,
    searchPlaceholder = "Search...",
    emptyMessage = "No records found.",
    toolbar
}: EntityListProps<T>) {
    const {
        query,
        response,
        isLoading,
        error,
        updateQuery,
        setSearch,
        setPage,
        setPageSize,
        setSort,
        reload
    } = useEntityList({ fetchData, initialQuery, reloadKey });
    const [searchValue, setSearchValue] = useState(query.search);

    useEffect(() => {
        setSearchValue(query.search);
    }, [query.search]);

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            if (searchValue !== query.search) {
                setSearch(searchValue);
            }
        }, 350);

        return () => window.clearTimeout(timeout);
    }, [query.search, searchValue, setSearch]);

    const sortState = getSortState(query.sort);
    const visibleActionCount = useMemo(
        () => response?.items.some((item) => actions.some((action) =>
            !action.isVisible || action.isVisible(item, user)
        )),
        [actions, response?.items, user]
    );

    const toggleSort = (column: EntityListColumn<T>) => {
        if (!column.sortable) {
            return;
        }

        const field = column.sortKey || column.key;
        const direction = sortState.field === field && sortState.direction === "asc"
            ? "desc"
            : "asc";
        setSort(`${field},${direction}`);
    };

    const displayValue = (item: T, column: EntityListColumn<T>) => {
        if (column.render) {
            return column.render(item);
        }

        const value = (item as Record<string, unknown>)[column.key];
        return value === null || value === undefined || value === ""
            ? "—"
            : String(value);
    };

    const currentPage = response?.page ?? query.page;
    const totalPages = response?.totalPages ?? 0;
    const totalElements = response?.totalElements ?? 0;
    const pageSize = response?.size || query.size;
    const firstItem = totalElements === 0 ? 0 : currentPage * pageSize + 1;
    const lastItem = Math.min((currentPage + 1) * pageSize, totalElements);

    return (
        <section className="entity-list-card">
            <div className="entity-list-heading">
                <div>
                    <p className="eyebrow">LIST VIEW</p>
                    <h2>{title}</h2>
                    {description && <p>{description}</p>}
                </div>
                <span className="record-count">
                    {totalElements} {totalElements === 1 ? "record" : "records"}
                </span>
            </div>

            <div className="entity-list-toolbar">
                <label className="entity-list-search">
                    <span>Search</span>
                    <input
                        type="search"
                        value={searchValue}
                        placeholder={searchPlaceholder}
                        onChange={(event) => setSearchValue(event.target.value)}
                    />
                </label>
                {toolbar?.({ query, updateQuery })}
            </div>

            {error && (
                <div className="entity-list-feedback">
                    <ErrorMessage title="Unable to load records" message={error} />
                    <button type="button" className="secondary-button" onClick={reload}>
                        Try again
                    </button>
                </div>
            )}

            {isLoading && <LoadingState message="Loading records..." />}

            {!isLoading && !error && response?.items.length === 0 && (
                <LoadingState message={emptyMessage} />
            )}

            {!isLoading && !error && response && response.items.length > 0 && (
                <>
                    <div className="entity-list-table-container">
                        <table className="entity-list-table">
                            <thead>
                                <tr>
                                    {columns.map((column) => (
                                        <th key={column.key} scope="col">
                                            {column.sortable ? (
                                                <button
                                                    type="button"
                                                    className="entity-list-sort-button"
                                                    onClick={() => toggleSort(column)}
                                                >
                                                    {column.label}
                                                    <span aria-hidden="true">
                                                        {sortState.field === (column.sortKey || column.key)
                                                            ? sortState.direction === "asc" ? " ↑" : " ↓"
                                                            : " ↕"}
                                                    </span>
                                                </button>
                                            ) : column.label}
                                        </th>
                                    ))}
                                    {visibleActionCount && <th scope="col">Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {response.items.map((item, index) => {
                                    const rowActions = actions.filter((action) =>
                                        !action.isVisible || action.isVisible(item, user)
                                    );

                                    return (
                                        <tr key={(item as { id?: string }).id || index}>
                                            {columns.map((column) => (
                                                <td key={column.key}>{displayValue(item, column)}</td>
                                            ))}
                                            {visibleActionCount && (
                                                <td>
                                                    <div className="entity-list-actions">
                                                        {rowActions.map((action) => (
                                                            <button
                                                                key={action.key}
                                                                type="button"
                                                                className={action.className || "entity-list-action-button"}
                                                                disabled={action.isDisabled?.(item, user)}
                                                                onClick={() => void action.onClick(item)}
                                                            >
                                                                {action.label}
                                                            </button>
                                                        ))}
                                                    </div>
                                                </td>
                                            )}
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>

                    <div className="entity-list-pagination">
                        <span>
                            Showing {firstItem}–{lastItem} of {totalElements}
                        </span>
                        <div className="entity-list-pagination-controls">
                            <label>
                                <span>Rows</span>
                                <select
                                    value={query.size}
                                    onChange={(event) => setPageSize(Number(event.target.value))}
                                >
                                    {[10, 20, 50, 100].map((size) => (
                                        <option value={size} key={size}>{size}</option>
                                    ))}
                                </select>
                            </label>
                            <button
                                type="button"
                                className="secondary-button"
                                disabled={currentPage <= 0}
                                onClick={() => setPage(currentPage - 1)}
                            >
                                Previous
                            </button>
                            <span className="entity-list-page-number">
                                Page {currentPage + 1} of {Math.max(totalPages, 1)}
                            </span>
                            <button
                                type="button"
                                className="secondary-button"
                                disabled={totalPages === 0 || currentPage >= totalPages - 1}
                                onClick={() => setPage(currentPage + 1)}
                            >
                                Next
                            </button>
                        </div>
                    </div>
                </>
            )}
        </section>
    );
}
