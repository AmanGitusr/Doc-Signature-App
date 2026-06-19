package com.docsignature.backend.repository;

import com.docsignature.backend.domain.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<DocumentEntity> findByIdAndOwnerId(Long id, Long ownerId);
}
