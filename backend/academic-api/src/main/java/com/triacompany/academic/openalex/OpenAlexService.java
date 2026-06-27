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
    public OpenAlexAuthorResponse findVerifiedAuthor(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);
        String orcidId = normalizeOrcidId(researcher.getOrcidId());

        JsonNode author = openAlexClient.fetchAuthorByOrcid(orcidId);

        return parseAuthor(author);
    }

    @Transactional(readOnly = true)
    public OpenAlexImportResponse searchWorks(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);
        OpenAlexAuthorResponse verifiedAuthor = findVerifiedAuthor(researcherId);

        List<OpenAlexWork> parsedWorks = fetchAndParseWorksByVerifiedAuthor(researcher, verifiedAuthor);

        return new OpenAlexImportResponse(
                researcher.getId(),
                researcher.getFullName(),
                verifiedAuthor.displayName(),
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
        OpenAlexAuthorResponse verifiedAuthor = findVerifiedAuthor(researcherId);

        List<OpenAlexWork> parsedWorks = fetchAndParseWorksByVerifiedAuthor(researcher, verifiedAuthor);
        List<OpenAlexWork> importedWorks = new ArrayList<>();

        for (OpenAlexWork parsedWork : parsedWorks) {
            if (parsedWork.getOpenAlexId() == null ||
                    openAlexWorkRepository.existsByResearcherIdAndOpenAlexId(
                            researcher.getId(),
                            parsedWork.getOpenAlexId()
                    )) {
                continue;
            }

            OpenAlexWork saved = openAlexWorkRepository.save(parsedWork);
            importedWorks.add(saved);
        }

        return new OpenAlexImportResponse(
                researcher.getId(),
                researcher.getFullName(),
                verifiedAuthor.displayName(),
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

    @Transactional
    public OpenAlexCleanupResponse deleteWorksByResearcher(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);

        long deleted = openAlexWorkRepository.deleteByResearcherId(researcherId);

        return new OpenAlexCleanupResponse(
                researcher.getId(),
                researcher.getFullName(),
                Math.toIntExact(deleted),
                "Obras OpenAlex removidas com sucesso para reimportação validada."
        );
    }

    private List<OpenAlexWork> fetchAndParseWorksByVerifiedAuthor(
            Researcher researcher,
            OpenAlexAuthorResponse verifiedAuthor
    ) {
        if (!hasText(verifiedAuthor.openAlexAuthorId())) {
            throw new IllegalArgumentException("Autor OpenAlex validado não possui ID.");
        }

        JsonNode response = openAlexClient.searchWorksByAuthorId(verifiedAuthor.openAlexAuthorId());
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

    private OpenAlexAuthorResponse parseAuthor(JsonNode author) {
        String openAlexAuthorId = text(author, "id");
        String orcid = text(author, "orcid");
        String displayName = text(author, "display_name");
        Integer worksCount = integer(author, "works_count");
        Integer citedByCount = integer(author, "cited_by_count");

        if (!hasText(openAlexAuthorId)) {
            throw new IllegalArgumentException("Autor OpenAlex inválido ou sem ID.");
        }

        return new OpenAlexAuthorResponse(
                openAlexAuthorId,
                orcid,
                displayName,
                worksCount,
                citedByCount
        );
    }

    private Researcher findResearcher(UUID researcherId) {
        return researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
    }

    private String normalizeOrcidId(String value) {
        String normalized = normalizeNullable(value);

        if (normalized == null) {
            throw new IllegalArgumentException("Este pesquisador não possui ORCID informado.");
        }

        normalized = normalized
                .replace("https://orcid.org/", "")
                .replace("http://orcid.org/", "")
                .trim();

        if (!normalized.matches("\\d{4}-\\d{4}-\\d{4}-\\d{3}[0-9X]")) {
            throw new IllegalArgumentException("ORCID inválido. Use o formato 0000-0000-0000-0000.");
        }

        return normalized;
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