package com.triacompany.academic.audit;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String actorEmail,
        String actorRole,
        String action,
        String module,
        String httpMethod,
        String endpoint,
        String targetType,
        String targetId,
        AuditEventStatus status,
        Integer httpStatus,
        String ipAddress,
        String userAgent,
        String message,
        LocalDateTime createdAt
) {
    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorEmail(),
                auditLog.getActorRole(),
                auditLog.getAction(),
                auditLog.getModule(),
                auditLog.getHttpMethod(),
                auditLog.getEndpoint(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getStatus(),
                auditLog.getHttpStatus(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getMessage(),
                auditLog.getCreatedAt()
        );
    }
}