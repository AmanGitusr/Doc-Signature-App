package com.docsignature.backend.service;

import com.docsignature.backend.domain.*;
import com.docsignature.backend.dto.DocumentDtos;
import com.docsignature.backend.exception.ApiException;
import com.docsignature.backend.repository.DocumentRepository;
import com.docsignature.backend.repository.SignatureRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private SignatureRequestRepository signatureRequestRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private AuditService auditService;

    public DocumentDtos.DocumentDetail upload(UserEntity owner, MultipartFile file, String title, String description, String ipAddress) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A PDF file is required");
        }
        if (title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Document title is required");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()) && (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".pdf"))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PDF files are supported");
        }

        StorageService.StoredFile stored = storageService.store(file, "doc");
        DocumentEntity document = new DocumentEntity();
        document.setOwner(owner);
        document.setTitle(title);
        document.setDescription(description);
        document.setOriginalFilename(file.getOriginalFilename());
        document.setStoredFilename(stored.storedFilename());
        document.setContentType(file.getContentType() == null ? "application/pdf" : file.getContentType());
        document.setFileSize(file.getSize());
        document.setStatus(DocumentStatus.DRAFT);
        document.setCreatedAt(Instant.now());
        document.setUpdatedAt(Instant.now());
        document = documentRepository.save(document);

        auditService.record(document, owner, ActorType.OWNER, "DOCUMENT_UPLOADED", "Uploaded document " + title, ipAddress);
        return toDetail(document);
    }

    public List<DocumentDtos.DocumentSummary> list(UserEntity owner) {
        return documentRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(document -> toSummary(document, signatureRequestRepository.findByDocumentId(document.getId()).orElse(null)))
                .toList();
    }

    public DocumentDtos.DocumentDetail get(UserEntity owner, Long documentId, String ipAddress) {
        DocumentEntity document = documentRepository.findByIdAndOwnerId(documentId, owner.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
        auditService.record(document, owner, ActorType.OWNER, "DOCUMENT_VIEWED", "Viewed document " + documentId, ipAddress);
        return toDetail(document);
    }

    public DocumentEntity requireOwnedDocument(UserEntity owner, Long documentId) {
        return documentRepository.findByIdAndOwnerId(documentId, owner.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    public DocumentEntity requireDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    public Resource download(UserEntity owner, Long documentId) throws IOException {
        DocumentEntity document = requireOwnedDocument(owner, documentId);
        return storageService.loadAsResource(document.getStoredFilename());
    }

    public DocumentDtos.DocumentDetail refreshDetail(Long documentId) {
        DocumentEntity document = requireDocument(documentId);
        return toDetail(document);
    }

    private DocumentDtos.DocumentSummary toSummary(DocumentEntity document, SignatureRequestEntity request) {
        return new DocumentDtos.DocumentSummary(
                document.getId(),
                document.getTitle(),
                document.getOriginalFilename(),
                document.getStatus().name(),
                document.getFileSize(),
                document.getCreatedAt(),
                document.getFinalizedAt(),
                request == null ? null : request.getSignerName(),
                request == null ? null : request.getSignerEmail(),
                request == null ? null : request.getToken()
        );
    }

    private DocumentDtos.DocumentDetail toDetail(DocumentEntity document) {
        SignatureRequestEntity request = signatureRequestRepository.findByDocumentId(document.getId()).orElse(null);
        return new DocumentDtos.DocumentDetail(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getStatus().name(),
                document.getCreatedAt(),
                document.getFinalizedAt(),
                request == null ? null : new com.docsignature.backend.dto.SignatureDtos.SignatureRequestResponse(
                        request.getId(),
                        document.getId(),
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
                )
        );
    }
}
