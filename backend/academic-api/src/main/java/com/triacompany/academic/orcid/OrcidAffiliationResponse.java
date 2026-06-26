package com.triacompany.academic.orcid;

public record OrcidAffiliationResponse(
        String type,
        String organizationName,
        String roleTitle,
        String departmentName,
        String startDate,
        String endDate,
        String city,
        String region,
        String country
) {
}