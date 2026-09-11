package org.schemeguard.backend.service.csvUpload;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.schemeguard.backend.entity.UploadPart;
import org.schemeguard.backend.entity.UploadSession;
import org.schemeguard.backend.entity.enums.UploadStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

@Service
public class CsvImportService {

    private static final int BATCH_SIZE = 1_000;
    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "transaction_id", "network", "amount", "currency", "merchant_country",
            "merchant_id", "merchant_name", "mcc", "card_country", "card_type",
            "card_product", "is_commercial_card", "channel", "entry_mode",
            "authentication", "authorization_timestamp", "clearing_timestamp",
            "authorization_code", "settlement_status", "merchant_data_complete",
            "recurring", "refund"
    );

    private final UploadService uploadService;
    private final StorageService storageService;
    private final TransactionBatchWriter batchWriter;

    public CsvImportService(
            UploadService uploadService,
            StorageService storageService,
            TransactionBatchWriter batchWriter
    ) {
        this.uploadService = uploadService;
        this.storageService = storageService;
        this.batchWriter = batchWriter;
    }

    @Async("csvImportExecutor")
    public Future<Void> importAsync(UUID uploadId) {
        long processedRows = 0;
        try {
            UploadSession session = uploadService.getSession(uploadId);
            if (session.getStatus() != UploadStatus.QUEUED) {
                return new AsyncResult<>(null);
            }

            if (!uploadService.markProcessing(uploadId)) {
                return new AsyncResult<>(null);
            }

            List<UploadPart> parts = uploadService.getParts(uploadId);
            if (session.getExpectedFileSha256() != null) {
                verifyWholeFileChecksum(session, parts);
            }

            processedRows = importCsv(uploadId, parts);
            uploadService.markCompleted(uploadId, processedRows);
            try {
                storageService.deleteUpload(uploadId);
            } catch (RuntimeException ignored) {
                // Import is complete even if temporary-file cleanup needs a retry.
            }
        } catch (Exception exception) {
            uploadService.markFailed(
                    uploadId,
                    rootMessage(exception),
                    processedRows
            );
        }
        return new AsyncResult<>(null);
    }

    private long importCsv(UUID uploadId, List<UploadPart> parts) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get();

        long processedRows = 0;
        List<TransactionRow> batch = new ArrayList<>(BATCH_SIZE);
        try (InputStream input = storageService.openConcatenatedStream(parts);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8)
             );
             CSVParser parser = format.parse(reader)) {
            validateHeaders(parser.getHeaderNames());
            for (CSVRecord record : parser) {
                if (!record.isConsistent()) {
                    throw new IllegalArgumentException(
                            "CSV row " + record.getRecordNumber()
                                    + " has an unexpected number of columns"
                    );
                }
                batch.add(TransactionRow.from(record, record.getRecordNumber() + 1));
                if (batch.size() == BATCH_SIZE) {
                    batchWriter.write(uploadId, batch);
                    processedRows += batch.size();
                    uploadService.updateProcessedRows(uploadId, processedRows);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                batchWriter.write(uploadId, batch);
                processedRows += batch.size();
                uploadService.updateProcessedRows(uploadId, processedRows);
            }
        }
        return processedRows;
    }

    private void verifyWholeFileChecksum(
            UploadSession session,
            List<UploadPart> parts
    ) throws IOException {
        MessageDigest digest = sha256Digest();
        try (InputStream input = new DigestInputStream(
                storageService.openConcatenatedStream(parts),
                digest
        )) {
            input.transferTo(java.io.OutputStream.nullOutputStream());
        }
        String actual = HexFormat.of().formatHex(digest.digest());
        if (!actual.equalsIgnoreCase(session.getExpectedFileSha256())) {
            throw new IllegalArgumentException(
                    "The complete file checksum does not match the declared checksum"
            );
        }
    }

    private void validateHeaders(List<String> headers) {
        Set<String> normalized = new HashSet<>();
        for (String header : headers) {
            normalized.add(header.trim());
        }
        if (!normalized.containsAll(REQUIRED_HEADERS)) {
            Set<String> missing = new HashSet<>(REQUIRED_HEADERS);
            missing.removeAll(normalized);
            throw new IllegalArgumentException(
                    "CSV is missing required columns: " + missing
            );
        }
    }

    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required", exception);
        }
    }

    private static String rootMessage(Exception exception) {
        Throwable current = exception;
        while (current.getCause() != null
                && (current.getMessage() == null || current.getMessage().isBlank())) {
            current = current.getCause();
        }
        return current.getMessage() == null
                ? "Import failed."
                : current.getMessage();
    }
}
