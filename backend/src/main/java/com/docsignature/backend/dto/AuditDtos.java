package com.docsignature.backend.dto;

import java.time.Instant;

public final class AuditDtos {
    private AuditDtos() {}

    public record AuditEventResponse(
            Long id,
            String actorType,
            String action,
            String details,
            String ipAddress,
            Instant createdAt
    ) {}
}
