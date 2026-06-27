package com.triacompany.academic.openalex;

import java.time.LocalDateTime;
import java.util.UUID;

public record OpenAlexWorkResponse(
        UUID id,
        UUID researcherId,
        String researcherName,
        String openAlexId,
        String doi,
        String title,
        String workType,
        Integer publicationYear,
        String publicationDate,
        String sourceName,
        Integer citedByCount,
        Boolean isOpenAccess,
        String openAccessStatus,
        String openAlexUrl,
        String doiUrl,
        String rawSource,
        PublicationReviewStatus reviewStatus,
        String reviewNote,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OpenAlexWorkResponse fromEntity(OpenAlexWork work) {
        return new OpenAlexWorkResponse(
                work.getId(),
                work.getResearcher().getId(),
                work.getResearcher().getFullName(),
                work.getOpenAlexId(),
                work.getDoi(),
                work.getTitle(),
                work.getWorkType(),
                work.getPublicationYear(),
                work.getPublicationDate(),
                work.getSourceName(),
                work.getCitedByCount(),
                work.getIsOpenAccess(),
                work.getOpenAccessStatus(),
                work.getOpenAlexUrl(),
                work.getDoiUrl(),
                work.getRawSource(),
                work.getReviewStatus(),
                work.getReviewNote(),
                work.getReviewedAt(),
                work.getCreatedAt(),
                work.getUpdatedAt()
        );
    }
}