package org.schemeguard.backend.service.csvUpload;

import org.schemeguard.backend.config.UploadProperties;
import org.schemeguard.backend.exception.UploadException;
import org.schemeguard.backend.entity.UploadPart;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Stream;

@Service
public class StorageService {

    private final Path rootDirectory;

    public StorageService(UploadProperties properties) {
        this.rootDirectory = Path.of(properties.getStorageDirectory())
                .toAbsolutePath()
                .normalize();
    }

    public void createUploadDirectory(UUID uploadId) {
        try {
            Files.createDirectories(uploadDirectory(uploadId));
        } catch (IOException exception) {
            throw new UploadException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "STORAGE_ERROR",
                    "Could not prepare upload storage."
            );
        }
    }

    public TempPart writeTemporaryPart(
            UUID uploadId,
            int partNumber,
            InputStream input,
            long expectedSize
    ) {
        createUploadDirectory(uploadId);
        Path temporaryPath;
        try {
            temporaryPath = Files.createTempFile(
                    uploadDirectory(uploadId),
                    "part-" + partNumber + "-",
                    ".tmp"
            );
        } catch (IOException exception) {
            throw new UploadException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "STORAGE_ERROR",
                    "Could not create a temporary part file."
            );
        }

        long size = 0;
        MessageDigest digest = sha256Digest();
        try (InputStream source = input;
             OutputStream target = Files.newOutputStream(temporaryPath)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = source.read(buffer)) != -1) {
                size += read;
                if (size > expectedSize) {
                    throw new UploadException(
                            HttpStatus.PAYLOAD_TOO_LARGE,
                            "PART_TOO_LARGE",
                            "The upload part is larger than expected."
                    );
                }
                digest.update(buffer, 0, read);
                target.write(buffer, 0, read);
            }
        } catch (UploadException exception) {
            deleteQuietly(temporaryPath);
            throw exception;
        } catch (IOException exception) {
            deleteQuietly(temporaryPath);
            throw new UploadException(
                    HttpStatus.BAD_REQUEST,
                    "PART_READ_ERROR",
                    "The upload part could not be read."
            );
        }

        if (size != expectedSize) {
            deleteQuietly(temporaryPath);
            throw new UploadException(
                    HttpStatus.BAD_REQUEST,
                    "PART_SIZE_MISMATCH",
                    "The upload part has an unexpected size."
            );
        }

        return new TempPart(
                temporaryPath,
                size,
                HexFormat.of().formatHex(digest.digest())
        );
    }

    public String moveToFinal(UUID uploadId, int partNumber, TempPart temporaryPart) {
        String relativePath = uploadId + "/part-%05d.chunk".formatted(partNumber);
        Path finalPath = rootDirectory.resolve(relativePath).normalize();
        try {
            if (!finalPath.startsWith(rootDirectory)) {
                throw new IOException("Resolved path is outside storage root");
            }
            Files.move(
                    temporaryPart.path(),
                    finalPath,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            try {
                Files.move(
                        temporaryPart.path(),
                        finalPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException moveException) {
                throw storageException();
            }
        } catch (IOException exception) {
            throw storageException();
        }
        return relativePath;
    }

    public void deleteTemporaryPart(TempPart temporaryPart) {
        deleteQuietly(temporaryPart.path());
    }

    public InputStream openConcatenatedStream(List<UploadPart> parts) {
        Vector<InputStream> streams = new Vector<>();
        try {
            for (UploadPart part : parts) {
                streams.add(new BufferedInputStream(
                        Files.newInputStream(safeResolve(part.getStoragePath()))
                ));
            }
            return new java.io.SequenceInputStream(
                    Collections.enumeration(streams)
            );
        } catch (IOException exception) {
            streams.forEach(this::closeQuietly);
            throw new UploadException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "STORAGE_ERROR",
                    "An upload part is missing from storage."
            );
        }
    }

    public void deleteUpload(UUID uploadId) {
        Path directory = uploadDirectory(uploadId);
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted((left, right) -> right.compareTo(left))
                    .forEach(this::deleteQuietly);
        } catch (IOException exception) {
            throw new UploadException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "STORAGE_ERROR",
                    "Could not remove upload storage."
            );
        }
    }

    private Path uploadDirectory(UUID uploadId) {
        return rootDirectory.resolve(uploadId.toString()).normalize();
    }

    private Path safeResolve(String relativePath) {
        Path resolved = rootDirectory.resolve(relativePath).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new UploadException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "STORAGE_ERROR",
                    "Invalid storage path."
            );
        }
        return resolved;
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required", exception);
        }
    }

    private UploadException storageException() {
        return new UploadException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "STORAGE_ERROR",
                "Could not store the upload part."
        );
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Cleanup is best effort. The scheduled cleanup can retry it.
        }
    }

    private void closeQuietly(InputStream input) {
        try {
            input.close();
        } catch (IOException ignored) {
            // Best effort cleanup.
        }
    }

    public record TempPart(Path path, long sizeBytes, String sha256) {
    }
}
