package com.triacompany.academic.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
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
            String message
    ) {
        AuditLog auditLog = AuditLog.builder()
                .actorEmail(normalize(actorEmail))
                .actorRole(normalize(actorRole))
                .action(defaultValue(action, "UNKNOWN_ACTION"))
                .module(defaultValue(module, "UNKNOWN_MODULE"))
                .httpMethod(defaultValue(httpMethod, "UNKNOWN"))
                .endpoint(defaultValue(endpoint, "UNKNOWN_ENDPOINT"))
                .targetType(normalize(targetType))
                .targetId(normalize(targetId))
                .status(status == null ? AuditEventStatus.SUCCESS : status)
                .httpStatus(httpStatus == null ? 0 : httpStatus)
                .ipAddress(normalize(ipAddress))
                .userAgent(normalize(userAgent))
                .message(normalize(message))
                .build();

        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findLatest() {
        return auditLogRepository.findTop200ByOrderByCreatedAtDesc()
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByActorEmail(String actorEmail) {
        return auditLogRepository.findTop100ByActorEmailOrderByCreatedAtDesc(actorEmail)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByModule(String module) {
        return auditLogRepository.findTop100ByModuleOrderByCreatedAtDesc(module)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByAction(String action) {
        return auditLogRepository.findTop100ByActionOrderByCreatedAtDesc(action)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByStatus(AuditEventStatus status) {
        return auditLogRepository.findTop100ByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditLogResponse findById(UUID id) {
        return auditLogRepository.findById(id)
                .map(AuditLogResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Registro de auditoria não encontrado."));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String defaultValue(String value, String fallback) {
        String normalized = normalize(value);
        return normalized == null ? fallback : normalized;
    }
}