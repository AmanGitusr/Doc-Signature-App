package com.docsignature.backend.repository;

import com.docsignature.backend.domain.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {
    List<AuditEventEntity> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}
