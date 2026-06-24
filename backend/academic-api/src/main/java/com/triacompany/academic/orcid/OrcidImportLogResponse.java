package com.triacompany.academic.orcid;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrcidImportLogResponse(
        UUID id,
        UUID researcherId,
        String researcherName,
        String orcidId,
        OrcidImportStatus status,
        String message,
        Integer totalFound,
        Integer totalImported,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
    public static OrcidImportLogResponse fromEntity(OrcidImportLog log) {
        return new OrcidImportLogResponse(
                log.getId(),
                log.getResearcher().getId(),
                log.getResearcher().getFullName(),
                log.getOrcidId(),
                log.getStatus(),
                log.getMessage(),
                log.getTotalFound(),
                log.getTotalImported(),
                log.getStartedAt(),
                log.getFinishedAt()
        );
    }
}