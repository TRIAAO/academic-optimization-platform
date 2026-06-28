package com.triacompany.academic.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findTop200ByOrderByCreatedAtDesc();

    List<AuditLog> findTop100ByActorEmailOrderByCreatedAtDesc(String actorEmail);

    List<AuditLog> findTop100ByModuleOrderByCreatedAtDesc(String module);

    List<AuditLog> findTop100ByActionOrderByCreatedAtDesc(String action);

    List<AuditLog> findTop100ByStatusOrderByCreatedAtDesc(AuditEventStatus status);
}