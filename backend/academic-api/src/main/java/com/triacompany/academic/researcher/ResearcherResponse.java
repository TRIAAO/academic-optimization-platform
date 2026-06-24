package com.triacompany.academic.researcher;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResearcherResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        String institution,
        String department,
        String academicTitle,
        String orcidId,
        String country,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResearcherResponse fromEntity(Researcher researcher) {
        return new ResearcherResponse(
                researcher.getId(),
                researcher.getFullName(),
                researcher.getEmail(),
                researcher.getPhone(),
                researcher.getInstitution(),
                researcher.getDepartment(),
                researcher.getAcademicTitle(),
                researcher.getOrcidId(),
                researcher.getCountry(),
                researcher.getActive(),
                researcher.getCreatedAt(),
                researcher.getUpdatedAt()
        );
    }
}