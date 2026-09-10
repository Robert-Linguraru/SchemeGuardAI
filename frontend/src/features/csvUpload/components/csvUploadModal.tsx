import { useEffect, useRef, useState } from "react";
import { UploadImportError, uploadCsv } from "../api/csvUploadApi";
import type {
  UploadProgress,
  UploadStatusResponse,
} from "../../../types/csvUploadType";

type TransactionUploadModalProps = {
  onCompleted?: (result: UploadStatusResponse) => void;
};

type Notification = {
  kind: "success" | "warning" | "error";
  title: string;
  message: string;
};

export default function CsvTransactionUploadModal({
  onCompleted,
}: TransactionUploadModalProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [progress, setProgress] = useState<UploadProgress | null>(null);
  const [message, setMessage] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [notification, setNotification] = useState<Notification | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const abortController = useRef<AbortController | null>(null);
  const resetTimer = useRef<number | null>(null);
  const notificationTimer = useRef<number | null>(null);

  const clearTimers = () => {
    if (resetTimer.current !== null) {
      window.clearTimeout(resetTimer.current);
      resetTimer.current = null;
    }
    if (notificationTimer.current !== null) {
      window.clearTimeout(notificationTimer.current);
      notificationTimer.current = null;
    }
  };

  const resetForm = () => {
    setFile(null);
    setProgress(null);
    setMessage("");
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const showNotification = (nextNotification: Notification) => {
    if (notificationTimer.current !== null) {
      window.clearTimeout(notificationTimer.current);
    }
    setNotification(nextNotification);
    notificationTimer.current = window.setTimeout(() => {
      setNotification(null);
      notificationTimer.current = null;
    }, nextNotification.kind === "success" ? 10_000 : 8_000);
  };

  useEffect(() => () => clearTimers(), []);

  const openModal = () => {
    clearTimers();
    setNotification(null);
    setMessage("");
    setProgress(null);
    setFile(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
    setIsOpen(true);
  };

  const closeModal = () => {
    if (isUploading) {
      return;
    }
    clearTimers();
    setIsOpen(false);
    resetForm();
  };

  const startUpload = async () => {
    if (!file) {
      const errorMessage = "Choose a CSV file first.";
      setMessage(errorMessage);
      showNotification({ kind: "error", title: "File required", message: errorMessage });
      return;
    }
    if (!file.name.toLowerCase().endsWith(".csv")) {
      const errorMessage = "Only .csv files are accepted.";
      setMessage(errorMessage);
      showNotification({ kind: "error", title: "Invalid file", message: errorMessage });
      return;
    }

    const controller = new AbortController();
    abortController.current = controller;
    setIsUploading(true);
    setMessage("");
    setProgress(null);
    setNotification(null);

    try {
      const result = await uploadCsv(file, setProgress, controller.signal);
      const importedMessage = `Imported ${result.processedRows.toLocaleString()} transactions.`;
      setMessage(importedMessage);
      showNotification({
        kind: "success",
        title: "Import completed",
        message: importedMessage,
      });
      onCompleted?.(result);
      resetTimer.current = window.setTimeout(() => {
        setIsOpen(false);
        resetForm();
        resetTimer.current = null;
      }, 10_000);
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") {
        const cancelledMessage = "Upload cancelled.";
        setMessage(cancelledMessage);
        showNotification({
          kind: "warning",
          title: "Upload cancelled",
          message: cancelledMessage,
        });
      } else {
        const processedRows = error instanceof UploadImportError
          ? error.processedRows
          : 0;
        const errorMessage = error instanceof Error
          ? error.message
          : "Upload failed.";
        const partial = processedRows > 0;
        const displayedMessage = partial
          ? `Imported ${processedRows.toLocaleString()} transactions, but some rows failed. ${errorMessage}`
          : errorMessage;
        setMessage(displayedMessage);
        showNotification({
          kind: partial ? "warning" : "error",
          title: partial ? "Import partially completed" : "Import failed",
          message: displayedMessage,
        });
      }
    } finally {
      setIsUploading(false);
      abortController.current = null;
    }
  };

  const cancelUpload = () => {
    abortController.current?.abort();
  };

  const uploadPercent = progress && progress.totalParts > 0
    ? Math.round((progress.completedParts / progress.totalParts) * 100)
    : 0;

  return (
    <>
      <button
        type="button"
        className="transaction-upload-open-button"
        onClick={openModal}
      >
        Upload CSV
      </button>

      {notification && (
        <aside
          className={`transaction-upload-toast transaction-upload-toast-${notification.kind}`}
          role="alert"
        >
          <div>
            <strong>{notification.title}</strong>
            <p>{notification.message}</p>
          </div>
          <button
            type="button"
            className="transaction-upload-toast-close"
            onClick={() => {
              if (notificationTimer.current !== null) {
                window.clearTimeout(notificationTimer.current);
                notificationTimer.current = null;
              }
              setNotification(null);
            }}
            aria-label="Dismiss notification"
          >
            x
          </button>
        </aside>
      )}

      {isOpen && (
        <div
          className="transaction-upload-backdrop"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              closeModal();
            }
          }}
        >
          <section
            className="transaction-upload-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="transaction-upload-title"
          >
            <div className="transaction-upload-modal-header">
              <div>
                <p className="eyebrow">ASYNC CSV IMPORT</p>
                <h2 id="transaction-upload-title">Upload transactions</h2>
              </div>
              <button
                type="button"
                className="transaction-upload-close-button"
                onClick={closeModal}
                disabled={isUploading}
                aria-label="Close upload dialog"
              >
                ×
              </button>
            </div>

            <p className="transaction-upload-intro">
              The file is split into checksum-verified chunks and imported in
              the background after all chunks arrive.
            </p>

            <form
              className="transaction-upload-form"
              onSubmit={(event) => {
                event.preventDefault();
                void startUpload();
              }}
            >
              <label className="transaction-upload-picker">
                <span>Select a CSV file</span>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".csv,text/csv"
                  disabled={isUploading}
                  onChange={(event) => {
                    setFile(event.target.files?.[0] ?? null);
                    setMessage("");
                    setProgress(null);
                    setNotification(null);
                  }}
                />
              </label>

              {file && (
                <p className="transaction-upload-file-name">
                  {file.name} · {(file.size / 1024 / 1024).toFixed(2)} MB
                </p>
              )}

              <div className="transaction-upload-actions">
                <button
                  type="submit"
                  disabled={!file || isUploading}
                >
                  {isUploading ? "Uploading…" : "Start import"}
                </button>
                {isUploading && (
                  <button
                    type="button"
                    className="transaction-upload-secondary-button"
                    onClick={cancelUpload}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>

            {progress && (
              <div className="transaction-upload-progress" aria-live="polite">
                <div className="transaction-upload-progress-label">
                  <span>
                    {progress.phase === "processing"
                      ? "Importing rows"
                      : "Uploading parts"}
                  </span>
                  <strong>
                    {progress.phase === "processing"
                      ? `${progress.processedRows.toLocaleString()} rows`
                      : `${uploadPercent}%`}
                  </strong>
                </div>
                <div className="transaction-upload-progress-track">
                  <div
                    className="transaction-upload-progress-value"
                    style={{ width: `${uploadPercent}%` }}
                  />
                </div>
                <div className="transaction-upload-progress-summary">
                  <span>
                    {progress.completedParts} / {progress.totalParts} parts completed
                  </span>
                </div>
              </div>
            )}

            {message && (
              <p className="transaction-upload-message" role="status">
                {message}
              </p>
            )}
          </section>
        </div>
      )}
    </>
  );
}
