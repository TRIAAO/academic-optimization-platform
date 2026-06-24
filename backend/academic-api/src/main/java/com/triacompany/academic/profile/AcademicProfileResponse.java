package com.triacompany.academic.profile;

import java.time.LocalDateTime;
import java.util.UUID;

public record AcademicProfileResponse(
        UUID id,
        UUID researcherId,
        String researcherName,
        String researcherEmail,
        String institution,
        String department,
        String researchArea,
        String biography,
        String keywords,
        String googleScholarUrl,
        String orcidUrl,
        String scopusAuthorId,
        String webOfScienceId,
        String lattesUrl,
        String institutionalProfileUrl,
        Integer profileCompletionPercentage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AcademicProfileResponse fromEntity(AcademicProfile profile) {
        return new AcademicProfileResponse(
                profile.getId(),
                profile.getResearcher().getId(),
                profile.getResearcher().getFullName(),
                profile.getResearcher().getEmail(),
                profile.getResearcher().getInstitution(),
                profile.getResearcher().getDepartment(),
                profile.getResearchArea(),
                profile.getBiography(),
                profile.getKeywords(),
                profile.getGoogleScholarUrl(),
                profile.getOrcidUrl(),
                profile.getScopusAuthorId(),
                profile.getWebOfScienceId(),
                profile.getLattesUrl(),
                profile.getInstitutionalProfileUrl(),
                profile.getProfileCompletionPercentage(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}