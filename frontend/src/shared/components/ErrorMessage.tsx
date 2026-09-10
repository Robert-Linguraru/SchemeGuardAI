interface ErrorMessageProps {
    title?: string;
    message: string;
}

export function ErrorMessage({
    title = "Something went wrong",
    message
}: ErrorMessageProps) {
    return (
        <div className="error-box" role="alert">
            <strong>{title}</strong>
            <span>{message}</span>
        </div>
    );
}