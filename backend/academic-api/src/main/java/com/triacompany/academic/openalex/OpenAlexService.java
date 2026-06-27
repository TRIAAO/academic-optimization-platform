package com.triacompany.academic.openalex;

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
public class OpenAlexService {

    private final ResearcherRepository researcherRepository;
    private final OpenAlexClient openAlexClient;
    private final OpenAlexWorkRepository openAlexWorkRepository;

    @Transactional(readOnly = true)
    public OpenAlexImportResponse searchWorks(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);

        List<OpenAlexWork> parsedWorks = fetchAndParseWorks(researcher);

        return new OpenAlexImportResponse(
                researcher.getId(),
                researcher.getFullName(),
                researcher.getFullName(),
                parsedWorks.size(),
                0,
                parsedWorks.stream()
                        .map(OpenAlexWorkResponse::fromEntity)
                        .toList()
        );
    }

    @Transactional
    public OpenAlexImportResponse importWorks(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);

        List<OpenAlexWork> parsedWorks = fetchAndParseWorks(researcher);
        List<OpenAlexWork> importedWorks = new ArrayList<>();

        for (OpenAlexWork parsedWork : parsedWorks) {
            if (parsedWork.getOpenAlexId() == null ||
                    openAlexWorkRepository.existsByResearcherIdAndOpenAlexId(researcher.getId(), parsedWork.getOpenAlexId())) {
                continue;
            }

            OpenAlexWork saved = openAlexWorkRepository.save(parsedWork);
            importedWorks.add(saved);
        }

        return new OpenAlexImportResponse(
                researcher.getId(),
                researcher.getFullName(),
                researcher.getFullName(),
                parsedWorks.size(),
                importedWorks.size(),
                importedWorks.stream()
                        .map(OpenAlexWorkResponse::fromEntity)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<OpenAlexWorkResponse> findWorksByResearcher(UUID researcherId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }

        return openAlexWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId)
                .stream()
                .map(OpenAlexWorkResponse::fromEntity)
                .toList();
    }

    private List<OpenAlexWork> fetchAndParseWorks(Researcher researcher) {
        JsonNode response = openAlexClient.searchWorksByAuthorName(researcher.getFullName());
        JsonNode results = response.path("results");

        List<OpenAlexWork> works = new ArrayList<>();

        if (!results.isArray()) {
            return works;
        }

        for (JsonNode result : results) {
            String title = text(result, "title");
            String openAlexId = text(result, "id");

            if (!hasText(title) || !hasText(openAlexId)) {
                continue;
            }

            OpenAlexWork work = OpenAlexWork.builder()
                    .researcher(researcher)
                    .openAlexId(openAlexId)
                    .doi(text(result, "doi"))
                    .title(title)
                    .workType(text(result, "type"))
                    .publicationYear(integer(result, "publication_year"))
                    .publicationDate(text(result, "publication_date"))
                    .sourceName(text(result, "primary_location", "source", "display_name"))
                    .citedByCount(integerOrDefault(result, 0, "cited_by_count"))
                    .isOpenAccess(booleanValue(result, "open_access", "is_oa"))
                    .openAccessStatus(text(result, "open_access", "oa_status"))
                    .openAlexUrl(text(result, "id"))
                    .doiUrl(text(result, "doi"))
                    .rawSource("OPENALEX")
                    .build();

            works.add(work);
        }

        return works;
    }

    private Researcher findResearcher(UUID researcherId) {
        return researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
    }

    private String text(JsonNode node, String... path) {
        JsonNode current = node;

        for (String part : path) {
            current = current.path(part);
        }

        if (current.isMissingNode() || current.isNull()) {
            return null;
        }

        if (current.isNumber() || current.isBoolean()) {
            return current.asText();
        }

        return normalizeNullable(current.asText(null));
    }

    private Integer integer(JsonNode node, String... path) {
        String value = text(node, path);

        if (!hasText(value)) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer integerOrDefault(JsonNode node, Integer defaultValue, String... path) {
        Integer value = integer(node, path);
        return value != null ? value : defaultValue;
    }

    private Boolean booleanValue(JsonNode node, String... path) {
        String value = text(node, path);

        if (!hasText(value)) {
            return null;
        }

        return Boolean.valueOf(value);
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