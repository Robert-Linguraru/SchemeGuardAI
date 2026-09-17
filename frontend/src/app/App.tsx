import { useState } from "react";
import { useAuth } from "./AuthProvider";
import { AuthPage } from "../features/auth/pages/AuthPage";
import { RuleUploadPage } from "../features/rules/pages/RuleUploadPage";
import { RulesPage } from "../features/rules/pages/RulesPage";
import { RuleDetailsPage } from "../features/rules/pages/RuleDetailsPage";
import { DashboardPage } from "../features/transactions/pages/DashboardPage";
import { ProfilePage } from "../features/auth/pages/ProfilePage";
import { CardSchemesPage } from "../features/cardSchemes/pages/CardSchemesPage";
import { LoadingState } from "../shared/components/LoadingState";

function AuthenticatedApp() {
    const [view, setView] = useState<
        "dashboard" | "rules" | "upload" | "details" | "profile" | "card-schemes"
    >("dashboard");

    const [selectedRuleId, setSelectedRuleId] =
        useState<string | null>(null);

    if (view === "upload") {
        return (
            <RuleUploadPage
                onBack={() => setView("rules")}
                onEditProfile={() => setView("profile")}
            />
        );
    }

    if (view === "profile") {
        return <ProfilePage onBack={() => setView("dashboard")} onEditProfile={() => setView("profile")} />;
    }

    if (view === "card-schemes") {
        return (
            <CardSchemesPage
                onBack={() => setView("dashboard")}
                onEditProfile={() => setView("profile")}
            />
        );
    }

    if (view === "details" && selectedRuleId) {
        return (
            <RuleDetailsPage
                ruleId={selectedRuleId}
                onBack={() => setView("rules")}
                onDeleted={() => setView("rules")}
                onEditProfile={() => setView("profile")}
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
                onEditProfile={() => setView("profile")}
            />
        );
    }

    return (
            <DashboardPage
                onCreateRule={() => setView("rules")}
                onOpenCardSchemes={() => setView("card-schemes")}
                onEditProfile={() => setView("profile")}
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
