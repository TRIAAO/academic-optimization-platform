package com.triacompany.academic.orcid;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrcidWorkResponse(
        UUID id,
        UUID researcherId,
        String researcherName,
        String orcidId,
        String putCode,
        String title,
        String workType,
        Integer publicationYear,
        Integer publicationMonth,
        Integer publicationDay,
        String journalTitle,
        String doi,
        String externalUrl,
        String sourceName,
        String rawSource,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrcidWorkResponse fromEntity(OrcidWork work) {
        return new OrcidWorkResponse(
                work.getId(),
                work.getResearcher().getId(),
                work.getResearcher().getFullName(),
                work.getOrcidId(),
                work.getPutCode(),
                work.getTitle(),
                work.getWorkType(),
                work.getPublicationYear(),
                work.getPublicationMonth(),
                work.getPublicationDay(),
                work.getJournalTitle(),
                work.getDoi(),
                work.getExternalUrl(),
                work.getSourceName(),
                work.getRawSource(),
                work.getCreatedAt(),
                work.getUpdatedAt()
        );
    }
}