package com.triacompany.academic.orcid;

import com.triacompany.academic.profile.AcademicProfile;
import com.triacompany.academic.profile.AcademicProfileRepository;
import com.triacompany.academic.profile.AcademicProfileResponse;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrcidProfileSyncService {

    private final ResearcherRepository researcherRepository;
    private final AcademicProfileRepository academicProfileRepository;
    private final OrcidProfileService orcidProfileService;

    @Transactional
    public OrcidProfileSyncResponse syncProfile(UUID researcherId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        OrcidProfileSummaryResponse summary = orcidProfileService.findSummaryByResearcher(researcherId);

        boolean profileCreated = false;
        boolean biographySynced = false;
        boolean keywordsSynced = false;
        boolean websiteSynced = false;
        boolean researchAreaSynced = false;

        AcademicProfile profile = academicProfileRepository.findByResearcherId(researcherId)
                .orElse(null);

        if (profile == null) {
            profile = AcademicProfile.builder()
                    .researcher(researcher)
                    .profileCompletionPercentage(0)
                    .build();

            profileCreated = true;
        }

        if (hasText(summary.biography())) {
            profile.setBiography(summary.biography());
            biographySynced = true;
        }

        String keywords = joinKeywords(summary.keywords());

        if (hasText(keywords)) {
            profile.setKeywords(keywords);
            keywordsSynced = true;
        }

        if (hasText(summary.orcidUrl())) {
            profile.setOrcidUrl(summary.orcidUrl());
        }

        String primaryWebsite = resolvePrimaryWebsite(summary.websites());

        if (hasText(primaryWebsite)) {
            profile.setInstitutionalProfileUrl(primaryWebsite);
            websiteSynced = true;
        }

        String suggestedResearchArea = suggestResearchArea(summary);

        if (hasText(suggestedResearchArea)) {
            profile.setResearchArea(suggestedResearchArea);
            researchAreaSynced = true;
        }

        profile.setProfileCompletionPercentage(calculateCompletion(profile));

        AcademicProfile saved = academicProfileRepository.save(profile);
        AcademicProfileResponse academicProfileResponse = AcademicProfileResponse.fromEntity(saved);

        return new OrcidProfileSyncResponse(
                researcher.getId(),
                researcher.getFullName(),
                summary.orcidId(),
                summary.orcidUrl(),
                profileCreated,
                biographySynced,
                keywordsSynced,
                websiteSynced,
                researchAreaSynced,
                saved.getProfileCompletionPercentage(),
                academicProfileResponse,
                summary
        );
    }

    private String joinKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }

        String joined = String.join("; ", keywords);
        return normalizeNullable(joined);
    }

    private String resolvePrimaryWebsite(List<OrcidWebsiteResponse> websites) {
        if (websites == null || websites.isEmpty()) {
            return null;
        }

        return websites.stream()
                .map(OrcidWebsiteResponse::url)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private String suggestResearchArea(OrcidProfileSummaryResponse summary) {
        String keywords = joinKeywords(summary.keywords());

        if (hasText(keywords)) {
            return keywords;
        }

        if (summary.employments() != null && !summary.employments().isEmpty()) {
            OrcidAffiliationResponse currentEmployment = summary.employments().get(0);

            String department = normalizeNullable(currentEmployment.departmentName());
            String role = normalizeNullable(currentEmployment.roleTitle());

            if (hasText(department) && hasText(role)) {
                return department + " — " + role;
            }

            if (hasText(department)) {
                return department;
            }

            if (hasText(role)) {
                return role;
            }
        }

        if (summary.educations() != null && !summary.educations().isEmpty()) {
            OrcidAffiliationResponse education = summary.educations().get(0);

            String role = normalizeNullable(education.roleTitle());

            if (hasText(role)) {
                return role;
            }
        }

        return null;
    }

    private Integer calculateCompletion(AcademicProfile profile) {
        long filled = Stream.of(
                        profile.getResearchArea(),
                        profile.getBiography(),
                        profile.getKeywords(),
                        profile.getGoogleScholarUrl(),
                        profile.getOrcidUrl(),
                        profile.getScopusAuthorId(),
                        profile.getWebOfScienceId(),
                        profile.getLattesUrl(),
                        profile.getInstitutionalProfileUrl()
                )
                .filter(this::hasText)
                .count();

        int totalFields = 9;

        return Math.toIntExact(Math.round((filled * 100.0) / totalFields));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}