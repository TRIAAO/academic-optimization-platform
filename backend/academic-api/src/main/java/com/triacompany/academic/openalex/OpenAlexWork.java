package com.triacompany.academic.openalex;

import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "openalex_works")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAlexWork {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private Researcher researcher;

    @Column(name = "openalex_id", nullable = false)
    private String openAlexId;

    @Column(length = 255)
    private String doi;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "work_type", length = 100)
    private String workType;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "publication_date", length = 30)
    private String publicationDate;

    @Column(name = "source_name", columnDefinition = "TEXT")
    private String sourceName;

    @Column(name = "cited_by_count", nullable = false)
    private Integer citedByCount;

    @Column(name = "is_open_access")
    private Boolean isOpenAccess;

    @Column(name = "open_access_status", length = 100)
    private String openAccessStatus;

    @Column(name = "openalex_url", columnDefinition = "TEXT")
    private String openAlexUrl;

    @Column(name = "doi_url", columnDefinition = "TEXT")
    private String doiUrl;

    @Column(name = "abstract_text", columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "abstract_language", length = 20)
    private String abstractLanguage;

    @Column(name = "abstract_pt", columnDefinition = "TEXT")
    private String abstractPt;

    @Column(name = "abstract_en", columnDefinition = "TEXT")
    private String abstractEn;

    @Column(name = "abstract_translations_updated_at")
    private LocalDateTime abstractTranslationsUpdatedAt;

    @Column(name = "raw_source", nullable = false, length = 50)
    private String rawSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 50)
    private PublicationReviewStatus reviewStatus;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

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

        if (citedByCount == null) {
            citedByCount = 0;
        }

        if (rawSource == null || rawSource.isBlank()) {
            rawSource = "OPENALEX";
        }

        if (reviewStatus == null) {
            reviewStatus = PublicationReviewStatus.PENDING_REVIEW;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
