package com.triacompany.academic.profile;

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
public class AcademicProfileService {

    private final AcademicProfileRepository academicProfileRepository;
    private final ResearcherRepository researcherRepository;

    @Transactional
    public AcademicProfileResponse create(CreateAcademicProfileRequest request) {
        Researcher researcher = researcherRepository.findById(request.researcherId())
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        if (academicProfileRepository.existsByResearcherId(request.researcherId())) {
            throw new IllegalArgumentException("Este pesquisador já possui um perfil acadêmico.");
        }

        AcademicProfile profile = AcademicProfile.builder()
                .researcher(researcher)
                .researchArea(normalizeNullable(request.researchArea()))
                .biography(normalizeNullable(request.biography()))
                .keywords(normalizeNullable(request.keywords()))
                .googleScholarUrl(normalizeNullable(request.googleScholarUrl()))
                .orcidUrl(resolveOrcidUrl(request.orcidUrl(), researcher.getOrcidId()))
                .scopusAuthorId(normalizeNullable(request.scopusAuthorId()))
                .webOfScienceId(normalizeNullable(request.webOfScienceId()))
                .lattesUrl(normalizeNullable(request.lattesUrl()))
                .institutionalProfileUrl(normalizeNullable(request.institutionalProfileUrl()))
                .profileCompletionPercentage(0)
                .build();

        profile.setProfileCompletionPercentage(calculateCompletion(profile));

        AcademicProfile saved = academicProfileRepository.save(profile);

        return AcademicProfileResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AcademicProfileResponse> findAll() {
        return academicProfileRepository.findAll()
                .stream()
                .map(AcademicProfileResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademicProfileResponse findById(UUID id) {
        AcademicProfile profile = findProfileById(id);
        return AcademicProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public AcademicProfileResponse findByResearcherId(UUID researcherId) {
        AcademicProfile profile = academicProfileRepository.findByResearcherId(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil acadêmico não encontrado para este pesquisador."));

        return AcademicProfileResponse.fromEntity(profile);
    }

    @Transactional
    public AcademicProfileResponse update(UUID id, UpdateAcademicProfileRequest request) {
        AcademicProfile profile = findProfileById(id);

        if (request.researchArea() != null) {
            profile.setResearchArea(normalizeNullable(request.researchArea()));
        }

        if (request.biography() != null) {
            profile.setBiography(normalizeNullable(request.biography()));
        }

        if (request.keywords() != null) {
            profile.setKeywords(normalizeNullable(request.keywords()));
        }

        if (request.googleScholarUrl() != null) {
            profile.setGoogleScholarUrl(normalizeNullable(request.googleScholarUrl()));
        }

        if (request.orcidUrl() != null) {
            profile.setOrcidUrl(normalizeNullable(request.orcidUrl()));
        }

        if (request.scopusAuthorId() != null) {
            profile.setScopusAuthorId(normalizeNullable(request.scopusAuthorId()));
        }

        if (request.webOfScienceId() != null) {
            profile.setWebOfScienceId(normalizeNullable(request.webOfScienceId()));
        }

        if (request.lattesUrl() != null) {
            profile.setLattesUrl(normalizeNullable(request.lattesUrl()));
        }

        if (request.institutionalProfileUrl() != null) {
            profile.setInstitutionalProfileUrl(normalizeNullable(request.institutionalProfileUrl()));
        }

        profile.setProfileCompletionPercentage(calculateCompletion(profile));

        AcademicProfile saved = academicProfileRepository.save(profile);

        return AcademicProfileResponse.fromEntity(saved);
    }

    private AcademicProfile findProfileById(UUID id) {
        return academicProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Perfil acadêmico não encontrado."));
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
                .filter(value -> value != null && !value.isBlank())
                .count();

        int totalFields = 9;

        return Math.toIntExact(Math.round((filled * 100.0) / totalFields));
    }

    private String resolveOrcidUrl(String requestOrcidUrl, String researcherOrcidId) {
        String normalizedRequestUrl = normalizeNullable(requestOrcidUrl);

        if (normalizedRequestUrl != null) {
            return normalizedRequestUrl;
        }

        String normalizedOrcidId = normalizeNullable(researcherOrcidId);

        if (normalizedOrcidId == null) {
            return null;
        }

        return "https://orcid.org/" + normalizedOrcidId;
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}