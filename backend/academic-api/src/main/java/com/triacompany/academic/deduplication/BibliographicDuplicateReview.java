package com.triacompany.academic.deduplication;

import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "bibliographic_duplicate_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_bibliographic_duplicate_pair",
                columnNames = {
                        "researcher_id",
                        "left_source",
                        "left_work_id",
                        "right_source",
                        "right_work_id"
                }
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BibliographicDuplicateReview {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private Researcher researcher;

    @Column(name = "left_source", nullable = false, length = 30)
    private String leftSource;

    @Column(name = "left_work_id", nullable = false)
    private UUID leftWorkId;

    @Column(name = "right_source", nullable = false, length = 30)
    private String rightSource;

    @Column(name = "right_work_id", nullable = false)
    private UUID rightWorkId;

    @Column(name = "similarity_score", nullable = false)
    private Integer similarityScore;

    @Column(name = "title_similarity", nullable = false)
    private Integer titleSimilarity;

    @Column(name = "doi_exact_match", nullable = false)
    private Boolean doiExactMatch;

    @Column(name = "publication_year_compatible", nullable = false)
    private Boolean publicationYearCompatible;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rationale;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 30)
    private DuplicateReviewStatus reviewStatus;

    @Column(name = "reviewer_note", columnDefinition = "TEXT")
    private String reviewerNote;

    @Column(name = "reviewed_by", length = 180)
    private String reviewedBy;

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
        if (reviewStatus == null) {
            reviewStatus = DuplicateReviewStatus.PENDING;
        }
        if (doiExactMatch == null) {
            doiExactMatch = false;
        }
        if (publicationYearCompatible == null) {
            publicationYearCompatible = false;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
