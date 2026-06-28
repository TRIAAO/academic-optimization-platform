package com.triacompany.academic.crossref;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CrossrefValidationResponse(
        UUID id,
        UUID openAlexWorkId,
        UUID researcherId,
        String researcherName,
        String doiSubmitted,
        String doiFound,
        String titleSubmitted,
        String titleFound,
        String publisher,
        String containerTitle,
        String publicationType,
        Integer publicationYear,
        Boolean isDoiValid,
        BigDecimal titleSimilarity,
        CrossrefMatchStatus matchStatus,
        String message,
        String rawSource,
        LocalDateTime validatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CrossrefValidationResponse fromEntity(CrossrefValidation validation) {
        return new CrossrefValidationResponse(
                validation.getId(),
                validation.getOpenAlexWork().getId(),
                validation.getResearcher().getId(),
                validation.getResearcher().getFullName(),
                validation.getDoiSubmitted(),
                validation.getDoiFound(),
                validation.getTitleSubmitted(),
                validation.getTitleFound(),
                validation.getPublisher(),
                validation.getContainerTitle(),
                validation.getPublicationType(),
                validation.getPublicationYear(),
                validation.getIsDoiValid(),
                validation.getTitleSimilarity(),
                validation.getMatchStatus(),
                validation.getMessage(),
                validation.getRawSource(),
                validation.getValidatedAt(),
                validation.getCreatedAt(),
                validation.getUpdatedAt()
        );
    }
}