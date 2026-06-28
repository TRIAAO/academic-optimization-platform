package com.triacompany.academic.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> findLatest() {
        return auditLogService.findLatest();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AuditLogResponse findById(@PathVariable UUID id) {
        return auditLogService.findById(id);
    }

    @GetMapping("/actor/{actorEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> findByActorEmail(@PathVariable String actorEmail) {
        return auditLogService.findByActorEmail(actorEmail);
    }

    @GetMapping("/module/{module}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> findByModule(@PathVariable String module) {
        return auditLogService.findByModule(module);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> findByAction(@PathVariable String action) {
        return auditLogService.findByAction(action);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponse> findByStatus(@PathVariable AuditEventStatus status) {
        return auditLogService.findByStatus(status);
    }
}