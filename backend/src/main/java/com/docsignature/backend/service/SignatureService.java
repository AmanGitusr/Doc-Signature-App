package com.docsignature.backend.service;

import com.docsignature.backend.domain.*;
import com.docsignature.backend.dto.DocumentDtos;
import com.docsignature.backend.dto.SignatureDtos;
import com.docsignature.backend.exception.ApiException;
import com.docsignature.backend.repository.DocumentRepository;
import com.docsignature.backend.repository.SignatureRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SignatureService {
    @Autowired
    private SignatureRequestRepository signatureRequestRepository;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private PdfSigningService pdfSigningService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private AuditService auditService;

    public SignatureDtos.SignatureRequestResponse create(UserEntity owner, SignatureDtos.CreateSignatureRequest request, String ipAddress) {
        DocumentEntity document = documentRepository.findByIdAndOwnerId(request.documentId(), owner.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
        if (document.getStatus() == DocumentStatus.SIGNED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Signed documents cannot be assigned again");
        }

        signatureRequestRepository.findByDocumentId(document.getId()).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Signature request already exists for this document");
        });

        SignatureRequestEntity entity = new SignatureRequestEntity();
        entity.setDocument(document);
        entity.setSignerName(request.signerName());
        entity.setSignerEmail(request.signerEmail().toLowerCase());
        entity.setToken(UUID.randomUUID().toString().replace("-", ""));
        entity.setPageNumber(request.pageNumber());
        entity.setXPosition(request.xPercent());
        entity.setYPosition(request.yPercent());
        entity.setStatus(SignatureStatus.PENDING);
        entity.setSignatureText(request.signatureText());
        entity.setRequestedAt(Instant.now());
        entity = signatureRequestRepository.save(entity);

        document.setStatus(DocumentStatus.PENDING_SIGNATURE);
        document.setUpdatedAt(Instant.now());
        documentRepository.save(document);

        auditService.record(document, owner, ActorType.OWNER, "SIGNATURE_REQUEST_CREATED",
                "Created signature request for " + request.signerEmail(), ipAddress);
        return toResponse(entity);
    }

    public SignatureDtos.SignatureRequestResponse getByDocument(UserEntity owner, Long documentId) {
        DocumentEntity document = documentRepository.findByIdAndOwnerId(documentId, owner.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
        SignatureRequestEntity entity = signatureRequestRepository.findByDocumentId(document.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Signature request not found"));
        return toResponse(entity);
    }

    public SignatureDtos.PublicSigningResponse getPublic(String token) {
        SignatureRequestEntity request = signatureRequestRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Signing token not found"));
        DocumentEntity document = request.getDocument();
        return new SignatureDtos.PublicSigningResponse(
                document.getId(),
                document.getTitle(),
                document.getOriginalFilename(),
                document.getStatus().name(),
                toResponse(request),
                "/api/public/signatures/" + token + "/download"
        );
    }

    public Resource downloadPublic(String token) throws IOException {
        SignatureRequestEntity request = signatureRequestRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Signing token not found"));
        return storageService.loadAsResource(request.getDocument().getStoredFilename());
    }

    public SignatureDtos.SignatureRequestResponse finalizeByToken(String token, String action, String signatureText, String rejectionReason, String ipAddress, UserEntity actor) throws IOException {
        SignatureRequestEntity request = signatureRequestRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Signing token not found"));
        if (request.getStatus() != SignatureStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Signature request already finalized");
        }

        if ("SIGNED".equalsIgnoreCase(action)) {
            if (signatureText != null && !signatureText.isBlank()) {
                request.setSignatureText(signatureText);
            }
            request.setStatus(SignatureStatus.SIGNED);
            request.setSignedAt(Instant.now());
            finalizeDocument(request);
            auditService.record(request.getDocument(), actor, actor == null ? ActorType.SIGNER : ActorType.OWNER, "DOCUMENT_SIGNED",
                    "Document signed by " + request.getSignerEmail(), ipAddress);
        } else if ("REJECTED".equalsIgnoreCase(action)) {
            request.setStatus(SignatureStatus.REJECTED);
            request.setRejectedAt(Instant.now());
            request.setRejectionReason(rejectionReason);
            request.getDocument().setStatus(DocumentStatus.REJECTED);
            request.getDocument().setUpdatedAt(Instant.now());
            documentRepository.save(request.getDocument());
            auditService.record(request.getDocument(), actor, actor == null ? ActorType.SIGNER : ActorType.OWNER, "DOCUMENT_REJECTED",
                    "Document rejected by " + request.getSignerEmail(), ipAddress);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Action must be SIGNED or REJECTED");
        }

        request = signatureRequestRepository.save(request);
        return toResponse(request);
    }

    public List<SignatureDtos.SignatureRequestResponse> listForOwner(UserEntity owner) {
        return signatureRequestRepository.findByDocumentOwnerIdOrderByRequestedAtDesc(owner.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void finalizeDocument(SignatureRequestEntity request) throws IOException {
        DocumentEntity document = request.getDocument();
        Path source = storageService.resolve(document.getStoredFilename());
        Path signedPath = storageService.resolve("signed-" + document.getStoredFilename());
        pdfSigningService.stamp(source, signedPath, request);
        document.setStoredFilename(signedPath.getFileName().toString());
        document.setStatus(DocumentStatus.SIGNED);
        document.setFinalizedAt(Instant.now());
        document.setUpdatedAt(Instant.now());
        documentRepository.save(document);
    }

    private SignatureDtos.SignatureRequestResponse toResponse(SignatureRequestEntity request) {
        return new SignatureDtos.SignatureRequestResponse(
                request.getId(),
                request.getDocument().getId(),
                request.getSignerName(),
                request.getSignerEmail(),
                request.getToken(),
                request.getPageNumber(),
                request.getXPosition(),
                request.getYPosition(),
                request.getStatus().name(),
                request.getSignatureText(),
                request.getRejectionReason(),
                request.getRequestedAt(),
                request.getSignedAt(),
                request.getRejectedAt()
        );
    }
}
