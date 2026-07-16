package com.triacompany.academic.orcid;

import com.fasterxml.jackson.databind.JsonNode;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrcidProfileService {

    private final ResearcherRepository researcherRepository;
    private final OrcidClient orcidClient;

    @Transactional(readOnly = true)
    public OrcidProfileSummaryResponse findSummaryByResearcher(UUID researcherId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        String orcidId = OrcidId.normalize(researcher.getOrcidId());

        if (orcidId == null) {
            throw new IllegalArgumentException("Este pesquisador não possui ORCID informado.");
        }

        return buildSummary(researcher, orcidId);
    }

    @Transactional(readOnly = true)
    public OrcidProfileSummaryResponse findSummaryByOrcidId(String rawOrcidId) {
        String orcidId = OrcidId.normalize(rawOrcidId);

        if (orcidId == null) {
            throw new IllegalArgumentException("ORCID é obrigatório.");
        }

        return buildSummary(null, orcidId);
    }

    private OrcidProfileSummaryResponse buildSummary(Researcher researcher, String orcidId) {
        JsonNode record = orcidClient.fetchRecord(orcidId);

        JsonNode person = record.path("person");
        JsonNode activities = record.path("activities-summary");

        String givenNames = text(person, "name", "given-names", "value");
        String familyName = text(person, "name", "family-name", "value");
        String creditName = text(person, "name", "credit-name", "value");

        String displayName = resolveDisplayName(creditName, givenNames, familyName, researcher);
        String biography = text(person, "biography", "content");

        List<String> keywords = extractKeywords(person);
        List<OrcidWebsiteResponse> websites = extractWebsites(person);
        List<OrcidAffiliationResponse> employments = extractAffiliations(
                activities.path("employments"),
                "employment"
        );
        List<OrcidAffiliationResponse> educations = extractAffiliations(
                activities.path("educations"),
                "education"
        );

        int worksCount = countWorks(activities);

        return new OrcidProfileSummaryResponse(
                researcher != null ? researcher.getId() : null,
                researcher != null ? researcher.getFullName() : null,
                orcidId,
                "https://orcid.org/" + orcidId,
                givenNames,
                familyName,
                creditName,
                displayName,
                biography,
                keywords,
                websites,
                employments,
                educations,
                worksCount
        );
    }

    private List<String> extractKeywords(JsonNode person) {
        List<String> keywords = new ArrayList<>();

        JsonNode keywordArray = person.path("keywords").path("keyword");

        if (!keywordArray.isArray()) {
            return keywords;
        }

        for (JsonNode keyword : keywordArray) {
            String content = text(keyword, "content");

            if (content != null) {
                keywords.add(content);
            }
        }

        return keywords;
    }

    private List<OrcidWebsiteResponse> extractWebsites(JsonNode person) {
        List<OrcidWebsiteResponse> websites = new ArrayList<>();

        JsonNode websiteArray = person.path("researcher-urls").path("researcher-url");

        if (!websiteArray.isArray()) {
            return websites;
        }

        for (JsonNode website : websiteArray) {
            String name = text(website, "url-name");
            String url = text(website, "url", "value");

            if (name != null || url != null) {
                websites.add(new OrcidWebsiteResponse(name, url));
            }
        }

        return websites;
    }

    private List<OrcidAffiliationResponse> extractAffiliations(JsonNode section, String type) {
        List<OrcidAffiliationResponse> affiliations = new ArrayList<>();

        JsonNode groups = section.path("affiliation-group");

        if (!groups.isArray()) {
            return affiliations;
        }

        for (JsonNode group : groups) {
            JsonNode summaries = group.path("summaries");

            if (!summaries.isArray()) {
                continue;
            }

            for (JsonNode summaryWrapper : summaries) {
                JsonNode summary = summaryWrapper.path(type + "-summary");

                if (summary.isMissingNode() || summary.isNull()) {
                    continue;
                }

                JsonNode organization = summary.path("organization");
                JsonNode address = organization.path("address");

                OrcidAffiliationResponse affiliation = new OrcidAffiliationResponse(
                        type,
                        text(organization, "name"),
                        text(summary, "role-title"),
                        text(summary, "department-name"),
                        formatDate(summary.path("start-date")),
                        formatDate(summary.path("end-date")),
                        text(address, "city"),
                        text(address, "region"),
                        text(address, "country")
                );

                affiliations.add(affiliation);
            }
        }

        return affiliations;
    }

    private int countWorks(JsonNode activities) {
        JsonNode groups = activities.path("works").path("group");

        if (!groups.isArray()) {
            return 0;
        }

        return groups.size();
    }

    private String resolveDisplayName(
            String creditName,
            String givenNames,
            String familyName,
            Researcher researcher
    ) {
        if (creditName != null) {
            return creditName;
        }

        StringBuilder builder = new StringBuilder();

        if (givenNames != null) {
            builder.append(givenNames);
        }

        if (familyName != null) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }

            builder.append(familyName);
        }

        String builtName = builder.toString().trim();

        if (!builtName.isBlank()) {
            return builtName;
        }

        if (researcher != null) {
            return researcher.getFullName();
        }

        return null;
    }

    private String formatDate(JsonNode dateNode) {
        if (dateNode == null || dateNode.isMissingNode() || dateNode.isNull()) {
            return null;
        }

        String year = text(dateNode, "year", "value");
        String month = text(dateNode, "month", "value");
        String day = text(dateNode, "day", "value");

        if (year == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(year);

        if (month != null) {
            builder.append("-").append(month);
        }

        if (day != null) {
            builder.append("-").append(day);
        }

        return builder.toString();
    }

    private String text(JsonNode node, String... path) {
        JsonNode current = node;

        for (String part : path) {
            current = current.path(part);
        }

        if (current.isMissingNode() || current.isNull()) {
            return null;
        }

        if (current.isNumber()) {
            return current.asText();
        }

        String value = current.asText(null);
        return normalizeNullable(value);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
