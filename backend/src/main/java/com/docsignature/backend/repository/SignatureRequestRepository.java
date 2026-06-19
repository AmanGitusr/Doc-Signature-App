package com.docsignature.backend.repository;

import com.docsignature.backend.domain.SignatureRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignatureRequestRepository extends JpaRepository<SignatureRequestEntity, Long> {
    Optional<SignatureRequestEntity> findByDocumentId(Long documentId);
    Optional<SignatureRequestEntity> findByToken(String token);
    List<SignatureRequestEntity> findByDocumentOwnerIdOrderByRequestedAtDesc(Long ownerId);
}
