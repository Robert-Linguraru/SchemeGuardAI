import { useAuth } from "./AuthProvider";
import { AuthPage } from "../features/auth/pages/AuthPage";
import { DashboardPage } from "../features/transactions/pages/DashboardPage";
import { LoadingState } from "../shared/components/LoadingState";

export function App() {
    const { user, isLoading } = useAuth();

    if (isLoading) {
        return <LoadingState message="Restoring your session..." />;
    }

    return user ? <DashboardPage /> : <AuthPage />;
}