package com.docsignature.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

public final class DocumentDtos {
    private DocumentDtos() {}

    public record UploadRequest(
            @NotBlank String title,
            String description
    ) {}

    public record DocumentSummary(
            Long id,
            String title,
            String originalFilename,
            String status,
            long fileSize,
            Instant createdAt,
            Instant finalizedAt,
            String signerName,
            String signerEmail,
            String signingToken
    ) {}

    public record DocumentDetail(
            Long id,
            String title,
            String description,
            String originalFilename,
            String contentType,
            long fileSize,
            String status,
            Instant createdAt,
            Instant finalizedAt,
            SignatureDtos.SignatureRequestResponse signatureRequest
    ) {}

    public record UploadResponse(DocumentDetail document) {}
}
