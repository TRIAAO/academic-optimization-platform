package com.triacompany.academic.crossref;

import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crossref_validations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossrefValidation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "openalex_work_id", nullable = false)
    private OpenAlexWork openAlexWork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private Researcher researcher;

    @Column(name = "doi_submitted", length = 255)
    private String doiSubmitted;

    @Column(name = "doi_found", length = 255)
    private String doiFound;

    @Column(name = "title_submitted", nullable = false, columnDefinition = "TEXT")
    private String titleSubmitted;

    @Column(name = "title_found", columnDefinition = "TEXT")
    private String titleFound;

    @Column(columnDefinition = "TEXT")
    private String publisher;

    @Column(name = "container_title", columnDefinition = "TEXT")
    private String containerTitle;

    @Column(name = "publication_type", length = 100)
    private String publicationType;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "is_doi_valid", nullable = false)
    private Boolean isDoiValid;

    @Column(name = "title_similarity", nullable = false, precision = 5, scale = 2)
    private BigDecimal titleSimilarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false, length = 50)
    private CrossrefMatchStatus matchStatus;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "raw_source", nullable = false, length = 50)
    private String rawSource;

    @Column(name = "validated_at", nullable = false)
    private LocalDateTime validatedAt;

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

        if (isDoiValid == null) {
            isDoiValid = false;
        }

        if (titleSimilarity == null) {
            titleSimilarity = BigDecimal.ZERO;
        }

        if (rawSource == null || rawSource.isBlank()) {
            rawSource = "CROSSREF";
        }

        if (validatedAt == null) {
            validatedAt = now;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}