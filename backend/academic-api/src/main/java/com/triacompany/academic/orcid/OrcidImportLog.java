package com.triacompany.academic.orcid;

import com.triacompany.academic.researcher.Researcher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orcid_import_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcidImportLog {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private Researcher researcher;

    @Column(name = "orcid_id", nullable = false, length = 50)
    private String orcidId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrcidImportStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "total_found", nullable = false)
    private Integer totalFound;

    @Column(name = "total_imported", nullable = false)
    private Integer totalImported;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (totalFound == null) {
            totalFound = 0;
        }

        if (totalImported == null) {
            totalImported = 0;
        }

        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}