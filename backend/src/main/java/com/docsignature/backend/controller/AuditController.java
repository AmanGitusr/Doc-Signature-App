package com.docsignature.backend.controller;

import com.docsignature.backend.dto.AuditDtos;
import com.docsignature.backend.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    @Autowired
    private AuditService auditService;

    @GetMapping("/{docId}")
    public List<AuditDtos.AuditEventResponse> list(@PathVariable Long docId) {
        return auditService.listForDocument(docId).stream()
                .map(event -> new AuditDtos.AuditEventResponse(
                        event.getId(),
                        event.getActorType().name(),
                        event.getAction(),
                        event.getDetails(),
                        event.getIpAddress(),
                        event.getCreatedAt()))
                .toList();
    }
}
