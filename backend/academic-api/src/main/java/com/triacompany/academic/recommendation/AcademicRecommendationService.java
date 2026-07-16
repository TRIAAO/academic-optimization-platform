package com.triacompany.academic.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.triacompany.academic.crossref.CrossrefValidation;
import com.triacompany.academic.crossref.CrossrefValidationRepository;
import com.triacompany.academic.openalex.OpenAlexClient;
import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
import com.triacompany.academic.orcid.OrcidWork;
import com.triacompany.academic.orcid.OrcidWorkRepository;
import com.triacompany.academic.profile.AcademicProfile;
import com.triacompany.academic.profile.AcademicProfileRepository;
import com.triacompany.academic.recommendation.AcademicRecommendationEngine.CollaboratorEvidence;
import com.triacompany.academic.recommendation.AcademicRecommendationEngine.RecommendationContext;
import com.triacompany.academic.recommendation.AcademicRecommendationEngine.WorkEvidence;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import com.triacompany.academic.scientometrics.ScientometricMetric;
import com.triacompany.academic.scientometrics.ScientometricMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcademicRecommendationService {

    private final ResearcherRepository researcherRepository;
    private final AcademicProfileRepository academicProfileRepository;
    private final OrcidWorkRepository orcidWorkRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;
    private final CrossrefValidationRepository crossrefValidationRepository;
    private final ScientometricMetricRepository scientometricMetricRepository;
    private final OpenAlexClient openAlexClient;
    private final AcademicRecommendationEngine engine = new AcademicRecommendationEngine();

    @Transactional(readOnly = true)
    public AcademicRecommendationResponse generate(UUID researcherId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        AcademicProfile profile = academicProfileRepository.findByResearcherId(researcherId).orElse(null);
        List<OrcidWork> orcidWorks = orcidWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);
        List<OpenAlexWork> openAlexWorks = openAlexWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);
        List<CrossrefValidation> validations = crossrefValidationRepository
                .findByResearcherIdOrderByValidatedAtDesc(researcherId);
        ScientometricMetric latestMetric = scientometricMetricRepository
                .findTopByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(researcherId)
                .orElse(null);

        int confirmedOpenAlexWorks = Math.toIntExact(openAlexWorks.stream()
                .filter(work -> PublicationReviewStatus.CONFIRMED.equals(work.getReviewStatus()))
                .count());

        OpenAlexEnrichment enrichment = loadOpenAlexEnrichment(researcher);

        RecommendationContext context = new RecommendationContext(
                researcher.getId(),
                researcher.getFullName(),
                profile != null ? profile.getResearchArea() : null,
                profile != null ? profile.getKeywords() : null,
                latestMetric != null ? latestMetric.getInterests() : null,
                profile != null && profile.getProfileCompletionPercentage() != null
                        ? profile.getProfileCompletionPercentage()
                        : 0,
                orcidWorks.size(),
                openAlexWorks.size(),
                confirmedOpenAlexWorks,
                validations.size(),
                latestMetric != null,
                enrichment.available(),
                buildWorkEvidence(orcidWorks, openAlexWorks, validations),
                enrichment.collaborators(),
                LocalDateTime.now()
        );

        return engine.generate(context);
    }

    private List<WorkEvidence> buildWorkEvidence(
            List<OrcidWork> orcidWorks,
            List<OpenAlexWork> openAlexWorks,
            List<CrossrefValidation> validations
    ) {
        List<WorkEvidence> evidence = new ArrayList<>();

        for (OrcidWork work : orcidWorks) {
            evidence.add(new WorkEvidence(
                    work.getTitle(),
                    firstText(work.getJournalTitle(), work.getSourceName()),
                    0,
                    false,
                    false
            ));
        }

        openAlexWorks.stream()
                .filter(work -> PublicationReviewStatus.CONFIRMED.equals(work.getReviewStatus()))
                .forEach(work -> evidence.add(new WorkEvidence(
                        work.getTitle(),
                        work.getSourceName(),
                        valueOrZero(work.getCitedByCount()),
                        Boolean.TRUE.equals(work.getIsOpenAccess()),
                        false
                )));

        for (CrossrefValidation validation : validations) {
            evidence.add(new WorkEvidence(
                    firstText(validation.getTitleFound(), validation.getTitleSubmitted()),
                    validation.getContainerTitle(),
                    0,
                    false,
                    Boolean.TRUE.equals(validation.getIsDoiValid())
            ));
        }

        return evidence;
    }

    private OpenAlexEnrichment loadOpenAlexEnrichment(Researcher researcher) {
        String orcidId = normalizeOrcidId(researcher.getOrcidId());
        if (orcidId == null) {
            return new OpenAlexEnrichment(List.of(), false);
        }

        try {
            JsonNode author = openAlexClient.fetchAuthorByOrcid(orcidId);
            String researcherAuthorId = text(author, "id");
            String authorShortId = shortOpenAlexId(researcherAuthorId);

            if (authorShortId == null) {
                return new OpenAlexEnrichment(List.of(), false);
            }

            JsonNode worksResponse = openAlexClient.searchWorksByAuthorId(authorShortId);
            return new OpenAlexEnrichment(
                    parseCollaborators(
                            worksResponse.path("results"),
                            researcherAuthorId,
                            researcher.getFullName()
                    ),
                    true
            );
        } catch (RuntimeException ignored) {
            return new OpenAlexEnrichment(List.of(), false);
        }
    }

    private List<CollaboratorEvidence> parseCollaborators(
            JsonNode works,
            String researcherAuthorId,
            String researcherName
    ) {
        if (!works.isArray()) {
            return List.of();
        }

        Map<String, CollaboratorAccumulator> collaborators = new LinkedHashMap<>();
        String normalizedResearcherName = normalizeForComparison(researcherName);

        for (JsonNode work : works) {
            int citedByCount = integerOrZero(work, "cited_by_count");
            Integer publicationYear = integer(work, "publication_year");
            JsonNode authorships = work.path("authorships");

            if (!authorships.isArray()) {
                continue;
            }

            for (JsonNode authorship : authorships) {
                JsonNode author = authorship.path("author");
                String authorId = text(author, "id");
                String displayName = text(author, "display_name");

                if (!hasText(displayName)
                        || sameAuthor(authorId, displayName, researcherAuthorId, normalizedResearcherName)) {
                    continue;
                }

                String key = hasText(authorId) ? authorId : normalizeForComparison(displayName);
                CollaboratorAccumulator accumulator = collaborators.computeIfAbsent(
                        key,
                        ignored -> new CollaboratorAccumulator(
                                displayName,
                                authorId,
                                text(author, "orcid"),
                                institutionName(authorship)
                        )
                );

                accumulator.sharedWorks++;
                accumulator.sharedCitations += Math.max(0, citedByCount);
                if (publicationYear != null) {
                    accumulator.latestYear = accumulator.latestYear == null
                            ? publicationYear
                            : Math.max(accumulator.latestYear, publicationYear);
                }
                if (!hasText(accumulator.institution)) {
                    accumulator.institution = institutionName(authorship);
                }
            }
        }

        return collaborators.values().stream()
                .sorted(Comparator
                        .comparingInt((CollaboratorAccumulator value) -> value.sharedWorks)
                        .reversed()
                        .thenComparing(Comparator.comparingInt(
                                (CollaboratorAccumulator value) -> value.sharedCitations
                        ).reversed()))
                .limit(20)
                .map(value -> new CollaboratorEvidence(
                        value.displayName,
                        value.openAlexAuthorId,
                        value.orcid,
                        value.institution,
                        value.sharedWorks,
                        value.sharedCitations,
                        value.latestYear
                ))
                .toList();
    }

    private boolean sameAuthor(
            String authorId,
            String displayName,
            String researcherAuthorId,
            String normalizedResearcherName
    ) {
        String shortAuthorId = shortOpenAlexId(authorId);
        String shortResearcherAuthorId = shortOpenAlexId(researcherAuthorId);

        if (hasText(shortAuthorId) && hasText(shortResearcherAuthorId)
                && shortAuthorId.equalsIgnoreCase(shortResearcherAuthorId)) {
            return true;
        }

        return hasText(normalizedResearcherName)
                && normalizedResearcherName.equals(normalizeForComparison(displayName));
    }

    private String institutionName(JsonNode authorship) {
        JsonNode institutions = authorship.path("institutions");
        if (institutions.isArray() && !institutions.isEmpty()) {
            String institution = text(institutions.get(0), "display_name");
            if (hasText(institution)) {
                return institution;
            }
        }

        JsonNode rawAffiliations = authorship.path("raw_affiliation_strings");
        if (rawAffiliations.isArray() && !rawAffiliations.isEmpty()) {
            return rawAffiliations.get(0).asText(null);
        }

        return null;
    }

    private String normalizeOrcidId(String value) {
        if (!hasText(value)) {
            return null;
        }

        String normalized = value
                .replace("https://orcid.org/", "")
                .replace("http://orcid.org/", "")
                .trim();

        return normalized.matches("\\d{4}-\\d{4}-\\d{4}-\\d{3}[0-9X]") ? normalized : null;
    }

    private String shortOpenAlexId(String value) {
        if (!hasText(value)) {
            return null;
        }

        return value
                .replace("https://openalex.org/", "")
                .replace("http://openalex.org/", "")
                .trim();
    }

    private String normalizeForComparison(String value) {
        if (!hasText(value)) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.path(field);
        if (value == null || value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private Integer integer(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.path(field);
        return value != null && value.isNumber() ? value.asInt() : null;
    }

    private int integerOrZero(JsonNode node, String field) {
        Integer value = integer(node, field);
        return value == null ? 0 : value;
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record OpenAlexEnrichment(List<CollaboratorEvidence> collaborators, boolean available) {
    }

    private static class CollaboratorAccumulator {
        private final String displayName;
        private final String openAlexAuthorId;
        private final String orcid;
        private String institution;
        private int sharedWorks;
        private int sharedCitations;
        private Integer latestYear;

        private CollaboratorAccumulator(
                String displayName,
                String openAlexAuthorId,
                String orcid,
                String institution
        ) {
            this.displayName = displayName;
            this.openAlexAuthorId = openAlexAuthorId;
            this.orcid = orcid;
            this.institution = institution;
        }
    }
}
