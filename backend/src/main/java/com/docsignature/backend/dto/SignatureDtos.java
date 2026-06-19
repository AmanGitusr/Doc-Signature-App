package com.docsignature.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class SignatureDtos {
    private SignatureDtos() {}

    public record CreateSignatureRequest(
            @NotNull Long documentId,
            @NotBlank @Size(max = 120) String signerName,
            @NotBlank @Email @Size(max = 180) String signerEmail,
            @Positive @Min(1) int pageNumber,
            @Min(0) @Max(100) double xPercent,
            @Min(0) @Max(100) double yPercent,
            @Size(max = 255) String signatureText
    ) {}

    public record FinalizeSignatureRequest(
            @NotBlank String token,
            @NotBlank String action,
            String signatureText,
            String rejectionReason
    ) {}

    public record SignatureRequestResponse(
            Long id,
            Long documentId,
            String signerName,
            String signerEmail,
            String token,
            int pageNumber,
            double xPercent,
            double yPercent,
            String status,
            String signatureText,
            String rejectionReason,
            Instant requestedAt,
            Instant signedAt,
            Instant rejectedAt
    ) {}

    public record PublicSigningResponse(
            Long documentId,
            String title,
            String originalFilename,
            String status,
            SignatureRequestResponse signatureRequest,
            String downloadUrl
    ) {}
}
