package com.triacompany.academic.editorial;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record EditorialDecisionResponse(
        UUID id,
        UUID researcherId,
        UUID workId,
        String workTitle,
        String journalName,
        String publisher,
        List<String> issns,
        int relevanceScore,
        String officialUrl,
        EditorialDecisionStatus status,
        boolean scopeConfirmed,
        boolean peerReviewConfirmed,
        boolean indexingConfirmed,
        boolean feesConfirmed,
        boolean languageConfirmed,
        boolean deadlinesConfirmed,
        int confirmedCriteria,
        String notes,
        String reviewedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    static EditorialDecisionResponse fromEntity(EditorialDecision decision) {
        List<String> issns = decision.getIssns() == null || decision.getIssns().isBlank()
                ? List.of()
                : Arrays.stream(decision.getIssns().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();

        int confirmedCriteria = 0;
        confirmedCriteria += Boolean.TRUE.equals(decision.getScopeConfirmed()) ? 1 : 0;
        confirmedCriteria += Boolean.TRUE.equals(decision.getPeerReviewConfirmed()) ? 1 : 0;
        confirmedCriteria += Boolean.TRUE.equals(decision.getIndexingConfirmed()) ? 1 : 0;
        confirmedCriteria += Boolean.TRUE.equals(decision.getFeesConfirmed()) ? 1 : 0;
        confirmedCriteria += Boolean.TRUE.equals(decision.getLanguageConfirmed()) ? 1 : 0;
        confirmedCriteria += Boolean.TRUE.equals(decision.getDeadlinesConfirmed()) ? 1 : 0;

        return new EditorialDecisionResponse(
                decision.getId(),
                decision.getResearcher().getId(),
                decision.getOpenAlexWork().getId(),
                decision.getOpenAlexWork().getTitle(),
                decision.getJournalName(),
                decision.getPublisher(),
                issns,
                decision.getRelevanceScore(),
                decision.getOfficialUrl(),
                decision.getStatus(),
                Boolean.TRUE.equals(decision.getScopeConfirmed()),
                Boolean.TRUE.equals(decision.getPeerReviewConfirmed()),
                Boolean.TRUE.equals(decision.getIndexingConfirmed()),
                Boolean.TRUE.equals(decision.getFeesConfirmed()),
                Boolean.TRUE.equals(decision.getLanguageConfirmed()),
                Boolean.TRUE.equals(decision.getDeadlinesConfirmed()),
                confirmedCriteria,
                decision.getNotes(),
                decision.getReviewedBy(),
                decision.getCreatedAt(),
                decision.getUpdatedAt()
        );
    }
}
