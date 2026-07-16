package com.triacompany.academic.openalex;

import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "openalex_author_identities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAlexAuthorIdentity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "researcher_id", nullable = false, unique = true)
    private Researcher researcher;

    @Column(name = "openalex_author_id", nullable = false, unique = true, length = 32)
    private String openAlexAuthorId;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "orcid_id", length = 50)
    private String orcidId;

    @Column(name = "last_known_institution", length = 255)
    private String lastKnownInstitution;

    @Column(name = "last_known_country_code", length = 10)
    private String lastKnownCountryCode;

    @Column(name = "works_count", nullable = false)
    private Integer worksCount;

    @Column(name = "cited_by_count", nullable = false)
    private Integer citedByCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_source", nullable = false, length = 30)
    private OpenAlexIdentityVerificationSource verificationSource;

    @Column(name = "confirmed_at", nullable = false)
    private LocalDateTime confirmedAt;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }
        if (worksCount == null) {
            worksCount = 0;
        }
        if (citedByCount == null) {
            citedByCount = 0;
        }
        if (confirmedAt == null) {
            confirmedAt = now;
        }
        if (lastSyncedAt == null) {
            lastSyncedAt = now;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
