package com.docsignature.backend.service;

import com.docsignature.backend.domain.*;
import com.docsignature.backend.repository.AuditEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AuditService {
    @Autowired
    private AuditEventRepository auditEventRepository;

    public void record(DocumentEntity document, UserEntity actorUser, ActorType actorType, String action, String details, String ipAddress) {
        AuditEventEntity event = new AuditEventEntity();
        event.setDocument(document);
        event.setActorUser(actorUser);
        event.setActorType(actorType);
        event.setAction(action);
        event.setDetails(details);
        event.setIpAddress(ipAddress);
        event.setCreatedAt(Instant.now());
        auditEventRepository.save(event);
    }

    public List<AuditEventEntity> listForDocument(Long documentId) {
        return auditEventRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);
    }
}
