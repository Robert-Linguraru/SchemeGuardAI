import { type FormEvent, useState } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { AccountMenu } from "../../../shared/components/AccountMenu";

interface ProfilePageProps {
    onBack: () => void;
    onEditProfile: () => void;
}

export function ProfilePage({ onBack, onEditProfile }: ProfilePageProps) {
    const { user, updateProfile } = useAuth();
    const [fullName, setFullName] = useState(user?.fullName ?? "");
    const [email, setEmail] = useState(user?.email ?? "");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [saved, setSaved] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [isSaving, setIsSaving] = useState(false);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setSaved(false);
        setError(null);
        setIsSaving(true);
        try {
            await updateProfile({
                fullName: fullName.trim(),
                email: email.trim(),
                ...(password ? { password } : {})
            });
            setPassword("");
            setSaved(true);
        } catch (profileError) {
            setError(profileError instanceof Error ? profileError.message : "Could not update profile");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <main className="dashboard profile-page">
            <div className="brand-decoration" aria-hidden="true">
                <span className="orb orb-blue orb-one"></span>
                <span className="orb orb-coral orb-two"></span>
                <span className="orb orb-white orb-three"></span>
                <span className="orb orb-blue orb-four"></span>
            </div>
            <header className="dashboard-header">
                <div className="welcome">
                    <h1>SchemeGuard AI</h1>
                    <p>Welcome, {user?.fullName}</p>
                </div>
                <AccountMenu onEditProfile={onEditProfile} />
            </header>

            <section className="content">
                <button className="page-nav-button" type="button" onClick={onBack}>
                    Transaction dashboard
                </button>
                <div className="profile-heading">
                    <p className="eyebrow">ACCOUNT SETTINGS</p>
                    <h2>Edit profile</h2>
                    <p>Keep your SchemeGuard account details up to date.</p>
                </div>

                <form className="profile-card" onSubmit={handleSubmit}>
                    <div className="profile-card-heading">
                        <div>
                            <h3>Personal information</h3>
                            <p>Update the name and email address associated with your account.</p>
                        </div>
                    </div>

                    <label>
                        Full name
                        <input
                            value={fullName}
                            onChange={(event) => setFullName(event.target.value)}
                            required
                            maxLength={150}
                        />
                    </label>
                    <label>
                        Email address
                        <input
                            type="email"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            required
                            maxLength={320}
                        />
                    </label>

                    <div className="profile-divider" />
                    {error && <div className="form-error" role="alert">{error}</div>}
                    <div className="profile-card-heading">
                        <div>
                            <h3>Change password</h3>
                            <p>Enter a new password if you want to update your sign-in credentials.</p>
                        </div>
                    </div>
                    <label>
                        New password
                        <div className="password-input">
                            <input
                                type={showPassword ? "text" : "password"}
                                value={password}
                                onChange={(event) => setPassword(event.target.value)}
                                minLength={8}
                                placeholder="Leave blank to keep your current password"
                            />
                            <button
                                className="password-toggle"
                                type="button"
                                onClick={() => setShowPassword((shown) => !shown)}
                                aria-label={showPassword ? "Hide password" : "Show password"}
                            >
                                {showPassword ? "Hide" : "Show"}
                            </button>
                        </div>
                    </label>

                    <div className="profile-form-footer">
                        {saved && <span className="profile-saved">Profile updated</span>}
                        <button className="save-profile-button" type="submit" disabled={isSaving}>
                            {isSaving ? "Saving..." : "Save changes"}
                        </button>
                    </div>
                </form>
            </section>
        </main>
    );
}
