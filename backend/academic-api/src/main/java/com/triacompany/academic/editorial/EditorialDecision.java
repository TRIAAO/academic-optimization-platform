package com.triacompany.academic.editorial;

import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "editorial_decisions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditorialDecision {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private Researcher researcher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "openalex_work_id", nullable = false)
    private OpenAlexWork openAlexWork;

    @Column(name = "journal_name", nullable = false, length = 500)
    private String journalName;

    @Column(length = 500)
    private String publisher;

    @Column(columnDefinition = "TEXT")
    private String issns;

    @Column(name = "relevance_score", nullable = false)
    private Integer relevanceScore;

    @Column(name = "official_url", columnDefinition = "TEXT")
    private String officialUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EditorialDecisionStatus status;

    @Column(name = "scope_confirmed", nullable = false)
    private Boolean scopeConfirmed;

    @Column(name = "peer_review_confirmed", nullable = false)
    private Boolean peerReviewConfirmed;

    @Column(name = "indexing_confirmed", nullable = false)
    private Boolean indexingConfirmed;

    @Column(name = "fees_confirmed", nullable = false)
    private Boolean feesConfirmed;

    @Column(name = "language_confirmed", nullable = false)
    private Boolean languageConfirmed;

    @Column(name = "deadlines_confirmed", nullable = false)
    private Boolean deadlinesConfirmed;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reviewed_by", nullable = false, length = 180)
    private String reviewedBy;

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
        if (status == null) {
            status = EditorialDecisionStatus.UNDER_REVIEW;
        }
        scopeConfirmed = Boolean.TRUE.equals(scopeConfirmed);
        peerReviewConfirmed = Boolean.TRUE.equals(peerReviewConfirmed);
        indexingConfirmed = Boolean.TRUE.equals(indexingConfirmed);
        feesConfirmed = Boolean.TRUE.equals(feesConfirmed);
        languageConfirmed = Boolean.TRUE.equals(languageConfirmed);
        deadlinesConfirmed = Boolean.TRUE.equals(deadlinesConfirmed);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
