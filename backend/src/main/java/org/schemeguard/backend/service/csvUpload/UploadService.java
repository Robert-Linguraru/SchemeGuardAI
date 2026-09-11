package org.schemeguard.backend.service.csvUpload;

import org.schemeguard.backend.dto.Upload.CompleteUploadResponse;
import org.schemeguard.backend.dto.Upload.CreateUploadRequest;
import org.schemeguard.backend.dto.Upload.CreateUploadResponse;
import org.schemeguard.backend.dto.Upload.PartUploadResponse;
import org.schemeguard.backend.dto.Upload.UploadStatusResponse;
import org.schemeguard.backend.entity.ProcessingJob;
import org.schemeguard.backend.entity.UploadPart;
import org.schemeguard.backend.entity.UploadSession;
import org.schemeguard.backend.entity.enums.JobStatus;
import org.schemeguard.backend.entity.enums.JobType;
import org.schemeguard.backend.entity.enums.UploadStatus;
import org.schemeguard.backend.exception.UploadException;
import org.schemeguard.backend.repository.ProcessingJobRepository;
import org.schemeguard.backend.repository.UploadPartRepository;
import org.schemeguard.backend.repository.UploadSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UploadService {

    private final UploadSessionRepository sessionRepository;
    private final UploadPartRepository partRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final UploadPolicy policy;
    private final StorageService storageService;
    private final PartRegistrationService partRegistrationService;
    private final ObjectProvider<CsvImportService> csvImportServiceProvider;

    public UploadService(
            UploadSessionRepository sessionRepository,
            UploadPartRepository partRepository,
            ProcessingJobRepository processingJobRepository,
            UploadPolicy policy,
            StorageService storageService,
            PartRegistrationService partRegistrationService,
            ObjectProvider<CsvImportService> csvImportServiceProvider
    ) {
        this.sessionRepository = sessionRepository;
        this.partRepository = partRepository;
        this.processingJobRepository = processingJobRepository;
        this.policy = policy;
        this.storageService = storageService;
        this.partRegistrationService = partRegistrationService;
        this.csvImportServiceProvider = csvImportServiceProvider;
    }

    @Transactional
    public CreateUploadResponse createUpload(CreateUploadRequest request) {
        policy.validateCreateRequest(request.fileName(), request.fileSizeBytes());
        int totalParts = policy.calculateTotalParts(request.fileSizeBytes());
        UUID uploadId = UUID.randomUUID();

        UploadSession session = new UploadSession(
                uploadId,
                request.fileName(),
                request.fileSizeBytes(),
                totalParts,
                normalizeHash(request.fileSha256())
        );
        ProcessingJob processingJob = processingJobRepository.save(
                newProcessingJob(request.fileName())
        );
        session.setProcessingJobId(processingJob.getId());
        sessionRepository.save(session);
        storageService.createUploadDirectory(uploadId);

        return new CreateUploadResponse(
                uploadId,
                policy.getChunkSizeBytes(),
                totalParts,
                policy.getMaxParallelUploads()
        );
    }

    public PartUploadResponse uploadPart(
            UUID uploadId,
            int partNumber,
            InputStream input,
            String clientSha256
    ) {
        UploadSession session = getSession(uploadId);
        policy.validatePartNumber(partNumber, session.getExpectedTotalParts());

        if (session.getStatus() != UploadStatus.UPLOADING) {
            throw new UploadException(
                    HttpStatus.CONFLICT,
                    "UPLOAD_NOT_ACCEPTING_PARTS",
                    "This upload no longer accepts parts."
            );
        }

        long expectedSize = policy.expectedPartSize(
                session.getExpectedFileSizeBytes(),
                partNumber
        );
        StorageService.TempPart temporaryPart = storageService.writeTemporaryPart(
                uploadId,
                partNumber,
                input,
                expectedSize
        );

        String normalizedClientHash = normalizeHash(clientSha256);
        if (normalizedClientHash != null
                && !normalizedClientHash.equals(temporaryPart.sha256())) {
            storageService.deleteTemporaryPart(temporaryPart);
            throw new UploadException(
                    HttpStatus.BAD_REQUEST,
                    "PART_CHECKSUM_MISMATCH",
                    "The upload part checksum is invalid."
            );
        }

        return partRegistrationService.registerPart(
                uploadId,
                partNumber,
                temporaryPart
        );
    }

    @Transactional
    public CompleteUploadResponse completeUpload(UUID uploadId) {
        UploadSession session = sessionRepository.findByIdForUpdate(uploadId)
                .orElseThrow(() -> new UploadException(
                        HttpStatus.NOT_FOUND,
                        "UPLOAD_NOT_FOUND",
                        "Upload session not found."
                ));

        if (session.getStatus() == UploadStatus.QUEUED
                || session.getStatus() == UploadStatus.PROCESSING
                || session.getStatus() == UploadStatus.COMPLETED) {
            return toCompleteResponse(session);
        }

        if (session.getStatus() != UploadStatus.UPLOADING) {
            throw new UploadException(
                    HttpStatus.CONFLICT,
                    "UPLOAD_CANNOT_COMPLETE",
                    "This upload cannot be completed."
            );
        }

        if (session.getUploadedParts() != session.getExpectedTotalParts()
                || session.getUploadedBytes() != session.getExpectedFileSizeBytes()) {
            throw new UploadException(
                    HttpStatus.CONFLICT,
                    "UPLOAD_INCOMPLETE",
                    "Not all upload parts have been received."
            );
        }

        List<UploadPart> parts = partRepository
                .findAllByUploadIdOrderByPartNumber(uploadId);
        if (parts.size() != session.getExpectedTotalParts()) {
            throw new UploadException(
                    HttpStatus.CONFLICT,
                    "UPLOAD_INCOMPLETE",
                    "Not all upload parts have been received."
            );
        }

        session.setStatus(UploadStatus.QUEUED);
        sessionRepository.save(session);
        runImportAfterCommit(uploadId);
        return toCompleteResponse(session);
    }

    @Transactional(readOnly = true)
    public UploadStatusResponse getStatus(UUID uploadId) {
        return toStatusResponse(getSession(uploadId));
    }

    @Transactional
    public void cancelUpload(UUID uploadId) {
        UploadSession session = sessionRepository.findByIdForUpdate(uploadId)
                .orElseThrow(() -> new UploadException(
                        HttpStatus.NOT_FOUND,
                        "UPLOAD_NOT_FOUND",
                        "Upload session not found."
                ));

        if (session.getStatus() == UploadStatus.PROCESSING
                || session.getStatus() == UploadStatus.COMPLETED) {
            throw new UploadException(
                    HttpStatus.CONFLICT,
                    "UPLOAD_CANNOT_CANCEL",
                    "A processing or completed upload cannot be cancelled."
            );
        }

        session.setStatus(UploadStatus.CANCELLED);
        sessionRepository.save(session);
        if (session.getProcessingJobId() != null) {
            processingJobRepository.findById(session.getProcessingJobId())
                    .ifPresent(processingJobRepository::delete);
        }
        storageService.deleteUpload(uploadId);
    }

    public UploadSession getSession(UUID uploadId) {
        return sessionRepository.findById(uploadId)
                .orElseThrow(() -> new UploadException(
                        HttpStatus.NOT_FOUND,
                        "UPLOAD_NOT_FOUND",
                        "Upload session not found."
                ));
    }

    public List<UploadPart> getParts(UUID uploadId) {
        return partRepository.findAllByUploadIdOrderByPartNumber(uploadId);
    }

    @Transactional
    public boolean markProcessing(UUID uploadId) {
        UploadSession session = sessionRepository.findByIdForUpdate(uploadId)
                .orElseThrow(() -> new UploadException(
                        HttpStatus.NOT_FOUND,
                        "UPLOAD_NOT_FOUND",
                        "Upload session not found."
                ));

        if (session.getStatus() != UploadStatus.QUEUED) {
            return false;
        }

        session.setStatus(UploadStatus.PROCESSING);
        session.setErrorMessage(null);
        sessionRepository.save(session);
        updateProcessingJob(session.getProcessingJobId(), JobStatus.RUNNING, 0, 0, null);
        return true;
    }

    @Transactional
    public void updateProcessedRows(UUID uploadId, long processedRows) {
        sessionRepository.findById(uploadId).ifPresent(session -> {
            session.setProcessedRows(processedRows);
            sessionRepository.save(session);
            updateProcessingJob(
                    session.getProcessingJobId(),
                    JobStatus.RUNNING,
                    processedRows,
                    0,
                    null
            );
        });
    }

    @Transactional
    public void markCompleted(UUID uploadId, long processedRows) {
        sessionRepository.findById(uploadId).ifPresent(session -> {
            session.setProcessedRows(processedRows);
            session.setStatus(UploadStatus.COMPLETED);
            session.setErrorMessage(null);
            sessionRepository.save(session);
            updateProcessingJob(
                    session.getProcessingJobId(),
                    JobStatus.COMPLETED,
                    processedRows,
                    0,
                    null
            );
        });
    }

    @Transactional
    public void markFailed(UUID uploadId, String message, long processedRows) {
        String safeMessage = message == null
                ? "Import failed."
                : message.substring(0, Math.min(message.length(), 1000));
        sessionRepository.findById(uploadId).ifPresent(session -> {
            session.setProcessedRows(processedRows);
            session.setStatus(UploadStatus.FAILED);
            session.setErrorMessage(safeMessage);
            sessionRepository.save(session);
            updateProcessingJob(
                    session.getProcessingJobId(),
                    JobStatus.FAILED,
                    processedRows,
                    1,
                    safeMessage
            );
        });
    }

    private void runImportAfterCommit(UUID uploadId) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        csvImportServiceProvider.getObject().importAsync(uploadId);
                    }
                }
        );
    }

    private ProcessingJob newProcessingJob(String fileName) {
        ProcessingJob job = new ProcessingJob();
        job.setJobType(JobType.CSV_IMPORT);
        job.setFileName(fileName);
        job.setStatus(JobStatus.PENDING);
        job.setTotalRecords(0);
        job.setProcessedRecords(0);
        job.setFailedRecords(0);
        return job;
    }

    private void updateProcessingJob(
            UUID processingJobId,
            JobStatus status,
            long processedRows,
            int failedRecords,
            String errorMessage
    ) {
        if (processingJobId == null) {
            return;
        }
        processingJobRepository.findById(processingJobId).ifPresent(job -> {
            job.setStatus(status);
            job.setProcessedRecords(Math.toIntExact(processedRows));
            job.setTotalRecords(Math.toIntExact(processedRows));
            job.setFailedRecords(failedRecords);
            job.setErrorMessage(errorMessage);
            if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
                job.setCompletedAt(java.time.Instant.now());
            }
            processingJobRepository.save(job);
        });
    }

    private UploadStatusResponse toStatusResponse(UploadSession session) {
        return new UploadStatusResponse(
                session.getId(),
                session.getOriginalFileName(),
                session.getStatus(),
                session.getExpectedFileSizeBytes(),
                session.getUploadedBytes(),
                session.getUploadedParts(),
                session.getExpectedTotalParts(),
                session.getProcessedRows(),
                session.getErrorMessage()
        );
    }

    private CompleteUploadResponse toCompleteResponse(UploadSession session) {
        return new CompleteUploadResponse(
                session.getId(),
                session.getStatus(),
                session.getUploadedParts(),
                session.getExpectedTotalParts()
        );
    }

    private String normalizeHash(String hash) {
        return hash == null || hash.isBlank()
                ? null
                : hash.toLowerCase(Locale.ROOT);
    }
}
