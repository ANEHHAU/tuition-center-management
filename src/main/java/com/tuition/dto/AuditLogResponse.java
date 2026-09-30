package com.tuition.dto;

import com.tuition.entity.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String action,
        String entityType,
        Long entityId,
        Long performedById,
        String performedByUsername,
        LocalDateTime performedAt,
        String oldValue,
        String newValue,
        String ipAddress
) {
    public static AuditLogResponse fromEntity(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getPerformedBy() != null ? log.getPerformedBy().getId() : null,
                log.getPerformedBy() != null ? log.getPerformedBy().getUsername() : null,
                log.getPerformedAt(),
                log.getOldValue(),
                log.getNewValue(),
                log.getIpAddress()
        );
    }
}
