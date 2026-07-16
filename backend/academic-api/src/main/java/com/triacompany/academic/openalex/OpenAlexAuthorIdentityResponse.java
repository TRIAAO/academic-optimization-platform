package com.triacompany.academic.openalex;

import java.time.LocalDateTime;
import java.util.UUID;

public record OpenAlexAuthorIdentityResponse(
        UUID id,
        UUID researcherId,
        String researcherName,
        String openAlexAuthorId,
        String openAlexUrl,
        String displayName,
        String orcid,
        String lastKnownInstitution,
        String lastKnownCountryCode,
        Integer worksCount,
        Integer citedByCount,
        OpenAlexIdentityVerificationSource verificationSource,
        LocalDateTime confirmedAt,
        LocalDateTime lastSyncedAt
) {
    public static OpenAlexAuthorIdentityResponse fromEntity(OpenAlexAuthorIdentity identity) {
        return new OpenAlexAuthorIdentityResponse(
                identity.getId(),
                identity.getResearcher().getId(),
                identity.getResearcher().getFullName(),
                identity.getOpenAlexAuthorId(),
                OpenAlexAuthorId.url(identity.getOpenAlexAuthorId()),
                identity.getDisplayName(),
                identity.getOrcidId(),
                identity.getLastKnownInstitution(),
                identity.getLastKnownCountryCode(),
                identity.getWorksCount(),
                identity.getCitedByCount(),
                identity.getVerificationSource(),
                identity.getConfirmedAt(),
                identity.getLastSyncedAt()
        );
    }
}
