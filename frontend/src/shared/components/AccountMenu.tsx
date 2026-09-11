import { useEffect, useRef, useState } from "react";
import { useAuth } from "../../app/AuthProvider";

interface AccountMenuProps {
    onEditProfile: () => void;
}

export function AccountMenu({ onEditProfile }: AccountMenuProps) {
    const { user, logout } = useAuth();
    const [isOpen, setIsOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const closeMenu = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        document.addEventListener("mousedown", closeMenu);
        return () => document.removeEventListener("mousedown", closeMenu);
    }, []);

    const handleEditProfile = () => {
        setIsOpen(false);
        onEditProfile();
    };

    return (
        <div className="account-menu" ref={menuRef}>
            <button
                className="account-button"
                type="button"
                aria-expanded={isOpen}
                aria-haspopup="menu"
                onClick={() => setIsOpen((open) => !open)}
            >
                <span className="account-avatar" aria-hidden="true">
                    {(user?.fullName?.trim().charAt(0) || "A").toUpperCase()}
                </span>
                <span>Account</span>
                <span className="account-chevron" aria-hidden="true">⌄</span>
            </button>

            {isOpen && (
                <div className="account-dropdown" role="menu">
                    <div className="account-summary">
                        <strong>{user?.fullName}</strong>
                        <span>{user?.email}</span>
                    </div>
                    <button type="button" role="menuitem" onClick={handleEditProfile}>
                        Edit profile
                    </button>
                    <button type="button" role="menuitem" onClick={logout}>
                        Logout
                    </button>
                </div>
            )}
        </div>
    );
}
