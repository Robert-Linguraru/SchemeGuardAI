import type {
  CreateUploadResponse,
  UploadProgress,
  UploadStatusResponse,
} from "../../../types/csvUploadType";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api";
const MAX_RETRIES = 3;

export class UploadImportError extends Error {
  constructor(message: string, public readonly processedRows: number) {
    super(message);
    this.name = "UploadImportError";
  }
}

function authorizationHeaders(): Record<string, string> {
  const token = localStorage.getItem("accessToken");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function parseError(response: Response): Promise<Error> {
  try {
    const body = (await response.json()) as { message?: string };
    return new Error(
      body.message ?? `Request failed with HTTP ${response.status}`,
    );
  } catch {
    return new Error(`Request failed with HTTP ${response.status}`);
  }
}

function sleep(milliseconds: number, signal: AbortSignal): Promise<void> {
  return new Promise((resolve, reject) => {
    const timeout = window.setTimeout(resolve, milliseconds);
    signal.addEventListener(
      "abort",
      () => {
        window.clearTimeout(timeout);
        reject(new DOMException("Upload cancelled", "AbortError"));
      },
      { once: true },
    );
  });
}

async function uploadPartWithRetry(
  uploadId: string,
  partNumber: number,
  buffer: ArrayBuffer,
  checksum: string,
  signal: AbortSignal,
): Promise<void> {
  for (let attempt = 0; attempt < MAX_RETRIES; attempt += 1) {
    signal.throwIfAborted();
    const response = await fetch(
      `${API_BASE_URL}/uploads/${uploadId}/parts/${partNumber}`,
      {
        method: "PUT",
        headers: {
          ...authorizationHeaders(),
          "Content-Type": "application/octet-stream",
          "X-Chunk-SHA256": checksum,
        },
        body: buffer,
        signal,
      },
    );

    if (response.ok) {
      return;
    }

    if (response.status !== 429 && response.status < 500) {
      throw await parseError(response);
    }

    if (attempt === MAX_RETRIES - 1) {
      throw await parseError(response);
    }

    const retryAfterSeconds = Number(response.headers.get("Retry-After"));
    const backoffMilliseconds = Number.isFinite(retryAfterSeconds)
      ? retryAfterSeconds * 1_000
      : 500 * 2 ** attempt;
    await sleep(backoffMilliseconds, signal);
  }
}

async function createUpload(
  file: File,
  signal: AbortSignal,
): Promise<CreateUploadResponse> {
  const response = await fetch(`${API_BASE_URL}/uploads`, {
    method: "POST",
    headers: {
      ...authorizationHeaders(),
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ fileName: file.name, fileSizeBytes: file.size }),
    signal,
  });
  if (!response.ok) {
    throw await parseError(response);
  }
  return (await response.json()) as CreateUploadResponse;
}

async function getStatus(
  uploadId: string,
  signal: AbortSignal,
): Promise<UploadStatusResponse> {
  const response = await fetch(`${API_BASE_URL}/uploads/${uploadId}`, {
    headers: authorizationHeaders(),
    signal,
  });
  if (!response.ok) {
    throw await parseError(response);
  }
  return (await response.json()) as UploadStatusResponse;
}

export async function uploadCsv(
  file: File,
  onProgress: (progress: UploadProgress) => void,
  signal: AbortSignal,
): Promise<UploadStatusResponse> {
  const session = await createUpload(file, signal);
  try {
    let nextPart = 0;
    let completedParts = 0;
    const concurrency = Math.min(
      session.maxParallelUploads,
      session.totalParts,
    );

    const worker = async (): Promise<void> => {
      while (true) {
        signal.throwIfAborted();
        const partNumber = nextPart;
        nextPart += 1;
        if (partNumber >= session.totalParts) {
          return;
        }

        const start = partNumber * session.chunkSizeBytes;
        const end = Math.min(start + session.chunkSizeBytes, file.size);
        const buffer = await file.slice(start, end).arrayBuffer();
        const checksum = await sha256Hex(buffer);
        await uploadPartWithRetry(
          session.uploadId,
          partNumber,
          buffer,
          checksum,
          signal,
        );
        completedParts += 1;
        onProgress({
          phase: "uploading",
          completedParts,
          totalParts: session.totalParts,
          processedRows: 0,
          status: "UPLOADING",
        });
      }
    };

    onProgress({
      phase: "uploading",
      completedParts: 0,
      totalParts: session.totalParts,
      processedRows: 0,
      status: "UPLOADING",
    });
    await Promise.all(
      Array.from({ length: concurrency }, () => worker()),
    );

    const completeResponse = await fetch(
      `${API_BASE_URL}/uploads/${session.uploadId}/complete`,
      {
        method: "POST",
        headers: authorizationHeaders(),
        signal,
      },
    );
    if (!completeResponse.ok) {
      throw await parseError(completeResponse);
    }

    let status = await getStatus(session.uploadId, signal);
    while (status.status === "QUEUED" || status.status === "PROCESSING") {
      onProgress({
        phase: "processing",
        completedParts: session.totalParts,
        totalParts: session.totalParts,
        processedRows: status.processedRows,
        status: status.status,
      });
      await sleep(1_000, signal);
      status = await getStatus(session.uploadId, signal);
    }

    if (status.status !== "COMPLETED") {
      throw new UploadImportError(
        status.errorMessage ?? "The CSV import failed.",
        status.processedRows,
      );
    }

    onProgress({
      phase: "completed",
      completedParts: session.totalParts,
      totalParts: session.totalParts,
      processedRows: status.processedRows,
      status: status.status,
    });
    return status;
  } catch (error) {
    // Best effort cleanup. The backend refuses cancellation after processing starts.
    await cancelUpload(session.uploadId).catch(() => undefined);
    throw error;
  }
}

export async function cancelUpload(uploadId: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/uploads/${uploadId}`, {
    method: "DELETE",
    headers: authorizationHeaders(),
  });
  if (!response.ok) {
    throw await parseError(response);
  }
}

async function sha256Hex(buffer: ArrayBuffer): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", buffer);
  return Array.from(new Uint8Array(digest), (byte) =>
    byte.toString(16).padStart(2, "0"),
  ).join("");
}
