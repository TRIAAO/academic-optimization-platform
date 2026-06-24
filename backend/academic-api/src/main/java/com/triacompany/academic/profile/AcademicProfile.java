package com.triacompany.academic.profile;

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
@Table(name = "academic_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProfile {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false, unique = true)
    private Researcher researcher;

    @Column(name = "research_area", length = 180)
    private String researchArea;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "google_scholar_url")
    private String googleScholarUrl;

    @Column(name = "orcid_url")
    private String orcidUrl;

    @Column(name = "scopus_author_id", length = 100)
    private String scopusAuthorId;

    @Column(name = "web_of_science_id", length = 100)
    private String webOfScienceId;

    @Column(name = "lattes_url")
    private String lattesUrl;

    @Column(name = "institutional_profile_url")
    private String institutionalProfileUrl;

    @Column(name = "profile_completion_percentage", nullable = false)
    private Integer profileCompletionPercentage;

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

        if (profileCompletionPercentage == null) {
            profileCompletionPercentage = 0;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}