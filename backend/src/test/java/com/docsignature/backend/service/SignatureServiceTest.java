package com.docsignature.backend.service;

import com.docsignature.backend.domain.*;
import com.docsignature.backend.dto.SignatureDtos;
import com.docsignature.backend.repository.DocumentRepository;
import com.docsignature.backend.repository.SignatureRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignatureServiceTest {
    @Mock SignatureRequestRepository signatureRequestRepository;
    @Mock DocumentRepository documentRepository;
    @Mock PdfSigningService pdfSigningService;
    @Mock StorageService storageService;
    @Mock AuditService auditService;
    @InjectMocks SignatureService signatureService;

    @Test
    void createSetsPendingRequest() {
        UserEntity owner = new UserEntity();
        owner.setId(1L);

        DocumentEntity document = new DocumentEntity();
        document.setId(9L);
        document.setOwner(owner);
        document.setStatus(DocumentStatus.DRAFT);

        when(documentRepository.findByIdAndOwnerId(9L, 1L)).thenReturn(Optional.of(document));
        when(signatureRequestRepository.findByDocumentId(9L)).thenReturn(Optional.empty());
        when(signatureRequestRepository.save(any())).thenAnswer(invocation -> {
            SignatureRequestEntity request = invocation.getArgument(0);
            request.setId(55L);
            return request;
        });
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SignatureDtos.SignatureRequestResponse response = signatureService.create(
                owner,
                new SignatureDtos.CreateSignatureRequest(9L, "Bob", "bob@example.com", 1, 12, 20, "Approved"),
                "10.0.0.1"
        );

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.PENDING_SIGNATURE);
    }

    @Test
    void finalizeSignsDocumentAndUpdatesStatus() throws Exception {
        UserEntity owner = new UserEntity();
        owner.setId(1L);

        DocumentEntity document = new DocumentEntity();
        document.setId(9L);
        document.setOwner(owner);
        document.setStoredFilename("source.pdf");
        document.setStatus(DocumentStatus.PENDING_SIGNATURE);

        SignatureRequestEntity request = new SignatureRequestEntity();
        request.setId(44L);
        request.setDocument(document);
        request.setSignerName("Bob");
        request.setSignerEmail("bob@example.com");
        request.setToken("token123");
        request.setPageNumber(1);
        request.setXPosition(10);
        request.setYPosition(20);
        request.setStatus(SignatureStatus.PENDING);
        request.setRequestedAt(Instant.now());

        when(signatureRequestRepository.findByToken("token123")).thenReturn(Optional.of(request));
        when(storageService.resolve("source.pdf")).thenReturn(Path.of("source.pdf"));
        when(storageService.resolve("signed-source.pdf")).thenReturn(Path.of("signed-source.pdf"));
        when(signatureRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pdfSigningService.stamp(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        SignatureDtos.SignatureRequestResponse response = signatureService.finalizeByToken("token123", "SIGNED", "Signed", null, "127.0.0.1", null);

        assertThat(response.status()).isEqualTo("SIGNED");
        ArgumentCaptor<DocumentEntity> documentCaptor = ArgumentCaptor.forClass(DocumentEntity.class);
        verify(documentRepository, atLeastOnce()).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getStatus()).isEqualTo(DocumentStatus.SIGNED);
    }
}
