import { useState, type FormEvent } from "react";
import { useAuth } from "../../../app/AuthProvider";
import { ErrorMessage } from "../../../shared/components/ErrorMessage";

export function RegisterForm() {
    const { register, error } = useAuth();
    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setIsSubmitting(true);
        try {
            await register({ fullName, email, password });
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <label>
                Full name
                <input value={fullName} onChange={(event) => setFullName(event.target.value)} required />
            </label>
            <label>
                Email
                <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
            </label>
            <label>
                Password
                <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} minLength={8} required />
            </label>
            <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Please wait..." : "Create account"}
            </button>
            {error && <ErrorMessage title="Unable to create account" message={error} />}
        </form>
    );
}