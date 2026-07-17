package com.triacompany.academic.orcid;

import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orcid_oauth_connections")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcidOAuthConnection {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false, unique = true)
    private Researcher researcher;

    @Column(name = "orcid_id", nullable = false, unique = true, length = 50)
    private String orcidId;

    @Column(name = "authenticated_name", length = 255)
    private String authenticatedName;

    @Column(name = "token_type", length = 50)
    private String tokenType;

    @Column(nullable = false, length = 255)
    private String scope;

    @Column(name = "encrypted_access_token", nullable = false, columnDefinition = "TEXT")
    private String encryptedAccessToken;

    @Column(name = "encrypted_refresh_token", columnDefinition = "TEXT")
    private String encryptedRefreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (connectedAt == null) {
            connectedAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
