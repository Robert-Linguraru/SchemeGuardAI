package org.schemeguard.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "upload_parts", uniqueConstraints = {
        @UniqueConstraint(
                name = "uq_upload_parts_session_number",
                columnNames = {"upload_id", "part_number"}
        )
})
public class UploadPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upload_id", nullable = false)
    private UploadSession uploadSession;

    @Column(name = "part_number", nullable = false)
    private int partNumber;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    protected UploadPart() {
    }

    public UploadPart(
            UploadSession uploadSession,
            int partNumber,
            long sizeBytes,
            String sha256,
            String storagePath
    ) {
        this.uploadSession = uploadSession;
        this.partNumber = partNumber;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
        this.storagePath = storagePath;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getSha256() {
        return sha256;
    }

    public String getStoragePath() {
        return storagePath;
    }
}
