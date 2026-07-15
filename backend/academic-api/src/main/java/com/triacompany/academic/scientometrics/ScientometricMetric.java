package com.triacompany.academic.scientometrics;

import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scientometric_metrics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScientometricMetric {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private Researcher researcher;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(name = "google_scholar_author_id", length = 120)
    private String googleScholarAuthorId;

    @Column(name = "google_scholar_profile_url", length = 500)
    private String googleScholarProfileUrl;

    @Column(name = "h_index_total")
    private Integer hIndexTotal;

    @Column(name = "h_index_last_six_years")
    private Integer hIndexLastSixYears;

    @Column(name = "i10_index_total")
    private Integer i10IndexTotal;

    @Column(name = "i10_index_last_six_years")
    private Integer i10IndexLastSixYears;

    @Column(name = "citations_total")
    private Integer citationsTotal;

    @Column(name = "citations_last_six_years")
    private Integer citationsLastSixYears;

    @Column(name = "d_index")
    private Integer dIndex;

    @Column(name = "verified_email", length = 180)
    private String verifiedEmail;

    @Column(name = "institutional_email_verified", nullable = false)
    private Boolean institutionalEmailVerified;

    @Column(columnDefinition = "TEXT")
    private String interests;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

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

        if (source == null || source.isBlank()) {
            source = "MANUAL_GOOGLE_SCHOLAR";
        }

        if (institutionalEmailVerified == null) {
            institutionalEmailVerified = false;
        }

        if (snapshotDate == null) {
            snapshotDate = LocalDate.now();
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}