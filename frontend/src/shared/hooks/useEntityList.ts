import { useCallback, useEffect, useState } from "react";
import type {
    EntityListFetcher,
    EntityListQuery,
    EntityListResponse
} from "../types/list";

const defaultQuery: EntityListQuery = {
    page: 0,
    size: 20,
    search: "",
    sort: "name,asc"
};

interface UseEntityListOptions<T> {
    fetchData: EntityListFetcher<T>;
    initialQuery?: Partial<EntityListQuery>;
    reloadKey?: number;
}

export function useEntityList<T>({
    fetchData,
    initialQuery,
    reloadKey = 0
}: UseEntityListOptions<T>) {
    const [query, setQuery] = useState<EntityListQuery>({
        ...defaultQuery,
        ...initialQuery
    });
    const [response, setResponse] = useState<EntityListResponse<T> | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [reloadVersion, setReloadVersion] = useState(0);

    useEffect(() => {
        const controller = new AbortController();
        let isMounted = true;

        setIsLoading(true);
        setError(null);

        fetchData(query, controller.signal)
            .then((nextResponse) => {
                if (isMounted) {
                    setResponse(nextResponse);
                }
            })
            .catch((requestError: unknown) => {
                if (requestError instanceof DOMException && requestError.name === "AbortError") {
                    return;
                }
                if (isMounted) {
                    setError(
                        requestError instanceof Error
                            ? requestError.message
                            : "Could not load the list"
                    );
                }
            })
            .finally(() => {
                if (isMounted) {
                    setIsLoading(false);
                }
            });

        return () => {
            isMounted = false;
            controller.abort();
        };
    }, [fetchData, query, reloadKey, reloadVersion]);

    const updateQuery = useCallback((changes: Partial<EntityListQuery>) => {
        setQuery((currentQuery) => ({ ...currentQuery, ...changes }));
    }, []);

    const setSearch = useCallback((search: string) => {
        updateQuery({ search, page: 0 });
    }, [updateQuery]);

    const setPage = useCallback((page: number) => {
        updateQuery({ page: Math.max(page, 0) });
    }, [updateQuery]);

    const setPageSize = useCallback((size: number) => {
        updateQuery({ size, page: 0 });
    }, [updateQuery]);

    const setSort = useCallback((sort: string) => {
        updateQuery({ sort, page: 0 });
    }, [updateQuery]);

    const reload = useCallback(() => {
        setReloadVersion((version) => version + 1);
    }, []);

    return {
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
    };
}
