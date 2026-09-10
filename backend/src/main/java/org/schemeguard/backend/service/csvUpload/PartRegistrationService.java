package org.schemeguard.backend.service.csvUpload;

import org.schemeguard.backend.dto.Upload.PartUploadResponse;
import org.schemeguard.backend.entity.UploadPart;
import org.schemeguard.backend.entity.UploadSession;
import org.schemeguard.backend.entity.enums.UploadStatus;
import org.schemeguard.backend.exception.UploadException;
import org.schemeguard.backend.repository.UploadPartRepository;
import org.schemeguard.backend.repository.UploadSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PartRegistrationService {

    private final UploadSessionRepository sessionRepository;
    private final UploadPartRepository partRepository;
    private final StorageService storageService;

    public PartRegistrationService(
            UploadSessionRepository sessionRepository,
            UploadPartRepository partRepository,
            StorageService storageService
    ) {
        this.sessionRepository = sessionRepository;
        this.partRepository = partRepository;
        this.storageService = storageService;
    }

    @Transactional
    public PartUploadResponse registerPart(
            UUID uploadId,
            int partNumber,
            StorageService.TempPart temporaryPart
    ) {
        UploadSession session = sessionRepository.findByIdForUpdate(uploadId)
                .orElseThrow(() -> new UploadException(
                        HttpStatus.NOT_FOUND,
                        "UPLOAD_NOT_FOUND",
                        "Upload session not found."
                ));

        if (session.getStatus() != UploadStatus.UPLOADING) {
            storageService.deleteTemporaryPart(temporaryPart);
            throw new UploadException(
                    HttpStatus.CONFLICT,
                    "UPLOAD_NOT_ACCEPTING_PARTS",
                    "This upload no longer accepts parts."
            );
        }

        UploadPart existing = partRepository
                .findByUploadIdAndPartNumber(uploadId, partNumber)
                .orElse(null);
        if (existing != null) {
            storageService.deleteTemporaryPart(temporaryPart);
            if (existing.getSizeBytes() != temporaryPart.sizeBytes()
                    || !existing.getSha256().equals(temporaryPart.sha256())) {
                throw new UploadException(
                        HttpStatus.CONFLICT,
                        "PART_ALREADY_EXISTS",
                        "A different part already exists at this part number."
                );
            }
            return new PartUploadResponse(
                    partNumber,
                    existing.getSizeBytes(),
                    existing.getSha256(),
                    true
            );
        }

        String storagePath = storageService.moveToFinal(
                uploadId,
                partNumber,
                temporaryPart
        );
        UploadPart part = new UploadPart(
                session,
                partNumber,
                temporaryPart.sizeBytes(),
                temporaryPart.sha256(),
                storagePath
        );
        partRepository.save(part);
        session.addUploadedBytes(temporaryPart.sizeBytes());
        session.incrementUploadedParts();
        sessionRepository.save(session);

        return new PartUploadResponse(
                partNumber,
                temporaryPart.sizeBytes(),
                temporaryPart.sha256(),
                false
        );
    }
}
