package com.triacompany.academic.orcid;

import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orcid_works")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcidWork {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private Researcher researcher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_log_id")
    private OrcidImportLog importLog;

    @Column(name = "orcid_id", nullable = false, length = 50)
    private String orcidId;

    @Column(name = "put_code", length = 100)
    private String putCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "work_type", length = 100)
    private String workType;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "publication_month")
    private Integer publicationMonth;

    @Column(name = "publication_day")
    private Integer publicationDay;

    @Column(name = "journal_title", columnDefinition = "TEXT")
    private String journalTitle;

    @Column(length = 255)
    private String doi;

    @Column(name = "external_url", columnDefinition = "TEXT")
    private String externalUrl;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "raw_source", nullable = false, length = 50)
    private String rawSource;

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

        if (rawSource == null || rawSource.isBlank()) {
            rawSource = "ORCID";
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}