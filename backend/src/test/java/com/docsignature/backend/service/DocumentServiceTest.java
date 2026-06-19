package com.docsignature.backend.service;

import com.docsignature.backend.domain.*;
import com.docsignature.backend.dto.DocumentDtos;
import com.docsignature.backend.repository.DocumentRepository;
import com.docsignature.backend.repository.SignatureRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock DocumentRepository documentRepository;
    @Mock SignatureRequestRepository signatureRequestRepository;
    @Mock StorageService storageService;
    @Mock AuditService auditService;
    @InjectMocks DocumentService documentService;

    @Test
    void uploadPersistsDocumentAndReturnsDetail() throws Exception {
        UserEntity owner = new UserEntity();
        owner.setId(3L);
        owner.setEmail("owner@example.com");
        owner.setRole(Role.OWNER);

        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", "pdf-bytes".getBytes());
        when(storageService.store(any(), any())).thenReturn(new StorageService.StoredFile("stored.pdf", java.nio.file.Path.of("stored.pdf")));
        when(documentRepository.save(any())).thenAnswer(invocation -> {
            DocumentEntity document = invocation.getArgument(0);
            document.setId(22L);
            return document;
        });

        DocumentDtos.DocumentDetail detail = documentService.upload(owner, file, "Contract", "Important", "127.0.0.1");

        assertThat(detail.title()).isEqualTo("Contract");
        ArgumentCaptor<DocumentEntity> captor = ArgumentCaptor.forClass(DocumentEntity.class);
        verify(documentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DocumentStatus.DRAFT);
        verify(auditService).record(any(), eq(owner), eq(ActorType.OWNER), eq("DOCUMENT_UPLOADED"), anyString(), eq("127.0.0.1"));
    }

    @Test
    void listMapsDocumentSummaries() {
        UserEntity owner = new UserEntity();
        owner.setId(3L);
        DocumentEntity document = new DocumentEntity();
        document.setId(22L);
        document.setTitle("Contract");
        document.setOriginalFilename("contract.pdf");
        document.setStatus(DocumentStatus.PENDING_SIGNATURE);
        document.setFileSize(100L);
        document.setCreatedAt(Instant.now());
        document.setUpdatedAt(Instant.now());

        when(documentRepository.findByOwnerIdOrderByCreatedAtDesc(3L)).thenReturn(java.util.List.of(document));
        when(signatureRequestRepository.findByDocumentId(22L)).thenReturn(Optional.empty());

        assertThat(documentService.list(owner)).hasSize(1);
    }
}
