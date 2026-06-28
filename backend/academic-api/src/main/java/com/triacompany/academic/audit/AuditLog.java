package com.triacompany.academic.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private UUID id;

    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Column(name = "actor_role", length = 100)
    private String actorRole;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 100)
    private String module;

    @Column(name = "http_method", nullable = false, length = 20)
    private String httpMethod;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "target_type", length = 100)
    private String targetType;

    @Column(name = "target_id", length = 255)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditEventStatus status;

    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = AuditEventStatus.SUCCESS;
        }
    }
}