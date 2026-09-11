import { useState } from "react";
import { useAuth } from "./AuthProvider";
import { AuthPage } from "../features/auth/pages/AuthPage";
import { RuleUploadPage } from "../features/rules/pages/RuleUploadPage";
import { RulesPage } from "../features/rules/pages/RulesPage";
import { RuleDetailsPage } from "../features/rules/pages/RuleDetailsPage";
import { DashboardPage } from "../features/transactions/pages/DashboardPage";
import { LoadingState } from "../shared/components/LoadingState";

function AuthenticatedApp() {
    const [view, setView] = useState<
        "dashboard" | "rules" | "upload" | "details"
    >("dashboard");

    const [selectedRuleId, setSelectedRuleId] =
        useState<string | null>(null);

    if (view === "upload") {
        return (
            <RuleUploadPage
                onBack={() => setView("rules")}
            />
        );
    }

    if (view === "details" && selectedRuleId) {
        return (
            <RuleDetailsPage
                ruleId={selectedRuleId}
                onBack={() => setView("rules")}
                onDeleted={() => setView("rules")}
            />
        );
    }

    if (view === "rules") {
        return (
            <RulesPage
                onBack={() => setView("dashboard")}
                onCreateRule={() => setView("upload")}
                onOpenRule={(id) => {
                    setSelectedRuleId(id);
                    setView("details");
                }}
            />
        );
    }

    return (
        <DashboardPage
            onCreateRule={() => setView("rules")}
        />
    );
}

export function App() {
    const { user, isLoading } = useAuth();

    if (isLoading) {
        return <LoadingState message="Restoring your session..." />;
    }

    return user ? <AuthenticatedApp /> : <AuthPage />;
}