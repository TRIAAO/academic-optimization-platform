package com.triacompany.academic.openalex;

import com.fasterxml.jackson.databind.JsonNode;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
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
    public List<OpenAlexAuthorCandidateResponse> findAuthorCandidates(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);

        JsonNode response = openAlexClient.searchAuthorCandidatesByName(researcher.getFullName());
        JsonNode results = response.path("results");

        List<OpenAlexAuthorCandidateResponse> candidates = new ArrayList<>();

        if (!results.isArray()) {
            return candidates;
        }

        for (JsonNode result : results) {
            candidates.add(parseCandidate(result, researcher));
        }

        return candidates.stream()
                .sorted(Comparator.comparing(OpenAlexAuthorCandidateResponse::relevanceScore).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public OpenAlexImportResponse searchWorks(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);
        OpenAlexAuthorResponse verifiedAuthor = findVerifiedAuthor(researcherId);

        String openAlexAuthorShortId = normalizeOpenAlexAuthorId(verifiedAuthor.openAlexAuthorId());

        List<OpenAlexWork> parsedWorks = fetchAndParseWorksByAuthorId(
                researcher,
                openAlexAuthorShortId
        );

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

        String openAlexAuthorShortId = normalizeOpenAlexAuthorId(verifiedAuthor.openAlexAuthorId());

        return importWorksByAuthorId(
                researcher,
                openAlexAuthorShortId,
                verifiedAuthor.displayName()
        );
    }

    @Transactional
    public OpenAlexImportResponse importWorksByApprovedAuthor(
            UUID researcherId,
            String openAlexAuthorShortId
    ) {
        Researcher researcher = findResearcher(researcherId);

        String normalizedAuthorId = normalizeOpenAlexAuthorId(openAlexAuthorShortId);

        return importWorksByAuthorId(
                researcher,
                normalizedAuthorId,
                normalizedAuthorId
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
    public OpenAlexAbstractSyncResponse syncAbstracts(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);
        List<OpenAlexWork> importedWorks = openAlexWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);
        int foundWorks = 0;
        int matchedImportedWorks = 0;
        int updatedWorks = 0;
        IllegalArgumentException lastSyncError = null;

        for (OpenAlexWork importedWork : importedWorks) {
            if (!hasText(importedWork.getOpenAlexId())) {
                continue;
            }

            try {
                JsonNode foundWork = openAlexClient.fetchWorkById(importedWork.getOpenAlexId());
                foundWorks++;
                matchedImportedWorks++;
                boolean changed = false;
                String abstractText = reconstructAbstract(foundWork.path("abstract_inverted_index"));
                String abstractLanguage = normalizeLanguage(text(foundWork, "language"));

                if (hasText(abstractText)
                        && !Objects.equals(importedWork.getAbstractText(), abstractText)) {
                    importedWork.setAbstractText(abstractText);
                    changed = true;
                }

                if (hasText(abstractLanguage)
                        && !Objects.equals(importedWork.getAbstractLanguage(), abstractLanguage)) {
                    importedWork.setAbstractLanguage(abstractLanguage);
                    changed = true;
                }

                if (changed) {
                    openAlexWorkRepository.save(importedWork);
                    updatedWorks++;
                }
            } catch (IllegalArgumentException exception) {
                lastSyncError = exception;
            }
        }

        if (!importedWorks.isEmpty() && foundWorks == 0 && lastSyncError != null) {
            throw new IllegalArgumentException(
                    "Não foi possível sincronizar nenhuma das obras importadas no OpenAlex neste momento."
            );
        }

        int worksWithAbstract = Math.toIntExact(importedWorks.stream()
                .filter(work -> hasText(work.getAbstractText()))
                .count());

        return new OpenAlexAbstractSyncResponse(
                researcher.getId(),
                researcher.getFullName(),
                foundWorks,
                matchedImportedWorks,
                updatedWorks,
                worksWithAbstract,
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public List<OpenAlexWorkResponse> findWorksByResearcherAndStatus(
            UUID researcherId,
            PublicationReviewStatus reviewStatus
    ) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }

        return openAlexWorkRepository
                .findByResearcherIdAndReviewStatusOrderByPublicationYearDescTitleAsc(
                        researcherId,
                        reviewStatus
                )
                .stream()
                .map(OpenAlexWorkResponse::fromEntity)
                .toList();
    }

    @Transactional
    public OpenAlexWorkResponse confirmWork(UUID workId, OpenAlexWorkReviewRequest request) {
        OpenAlexWork work = findOpenAlexWork(workId);

        work.setReviewStatus(PublicationReviewStatus.CONFIRMED);
        work.setReviewNote(normalizeNullable(request != null ? request.reviewNote() : null));
        work.setReviewedAt(LocalDateTime.now());

        return OpenAlexWorkResponse.fromEntity(openAlexWorkRepository.save(work));
    }

    @Transactional
    public OpenAlexWorkResponse rejectWork(UUID workId, OpenAlexWorkReviewRequest request) {
        OpenAlexWork work = findOpenAlexWork(workId);

        work.setReviewStatus(PublicationReviewStatus.REJECTED);
        work.setReviewNote(normalizeNullable(request != null ? request.reviewNote() : null));
        work.setReviewedAt(LocalDateTime.now());

        return OpenAlexWorkResponse.fromEntity(openAlexWorkRepository.save(work));
    }

    @Transactional
    public OpenAlexWorkResponse markWorkAsPendingReview(UUID workId, OpenAlexWorkReviewRequest request) {
        OpenAlexWork work = findOpenAlexWork(workId);

        work.setReviewStatus(PublicationReviewStatus.PENDING_REVIEW);
        work.setReviewNote(normalizeNullable(request != null ? request.reviewNote() : null));
        work.setReviewedAt(null);

        return OpenAlexWorkResponse.fromEntity(openAlexWorkRepository.save(work));
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

    private OpenAlexImportResponse importWorksByAuthorId(
            Researcher researcher,
            String openAlexAuthorId,
            String searchName
    ) {
        String openAlexAuthorShortId = normalizeOpenAlexAuthorId(openAlexAuthorId);

        List<OpenAlexWork> parsedWorks = fetchAndParseWorksByAuthorId(
                researcher,
                openAlexAuthorShortId
        );

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
                searchName,
                parsedWorks.size(),
                importedWorks.size(),
                importedWorks.stream()
                        .map(OpenAlexWorkResponse::fromEntity)
                        .toList()
        );
    }

    private List<OpenAlexWork> fetchAndParseWorksByAuthorId(
            Researcher researcher,
            String openAlexAuthorId
    ) {
        String openAlexAuthorShortId = normalizeOpenAlexAuthorId(openAlexAuthorId);

        JsonNode response = openAlexClient.searchWorksByAuthorId(openAlexAuthorShortId);
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
                    .abstractText(reconstructAbstract(result.path("abstract_inverted_index")))
                    .abstractLanguage(normalizeLanguage(text(result, "language")))
                    .rawSource("OPENALEX")
                    .reviewStatus(PublicationReviewStatus.PENDING_REVIEW)
                    .build();

            works.add(work);
        }

        return works;
    }

    String reconstructAbstract(JsonNode invertedIndex) {
        if (invertedIndex == null || !invertedIndex.isObject() || invertedIndex.isEmpty()) {
            return null;
        }

        Map<Integer, String> wordsByPosition = new TreeMap<>();
        invertedIndex.fields().forEachRemaining(entry -> {
            JsonNode positions = entry.getValue();
            if (!positions.isArray()) {
                return;
            }

            for (JsonNode position : positions) {
                if (position.isIntegralNumber()) {
                    wordsByPosition.putIfAbsent(position.asInt(), entry.getKey());
                }
            }
        });

        if (wordsByPosition.isEmpty()) {
            return null;
        }

        return normalizeNullable(String.join(" ", wordsByPosition.values())
                .replaceAll("\\s+([,.;:!?])", "$1")
                .replace("( ", "(")
                .replace(" )", ")")
                .replace("[ ", "[")
                .replace(" ]", "]"));
    }

    private String normalizeLanguage(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
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

    private OpenAlexAuthorCandidateResponse parseCandidate(
            JsonNode author,
            Researcher researcher
    ) {
        String openAlexAuthorId = text(author, "id");
        String openAlexAuthorShortId = extractShortAuthorId(openAlexAuthorId);
        String orcid = text(author, "orcid");
        String displayName = text(author, "display_name");
        Integer worksCount = integer(author, "works_count");
        Integer citedByCount = integer(author, "cited_by_count");

        String institutionName = text(author, "last_known_institutions", "0", "display_name");
        String countryCode = text(author, "last_known_institutions", "0", "country_code");

        double score = calculateCandidateScore(
                researcher,
                displayName,
                institutionName,
                countryCode,
                worksCount,
                citedByCount
        );

        return new OpenAlexAuthorCandidateResponse(
                openAlexAuthorId,
                openAlexAuthorShortId,
                orcid,
                displayName,
                institutionName,
                countryCode,
                worksCount,
                citedByCount,
                score
        );
    }

    private double calculateCandidateScore(
            Researcher researcher,
            String displayName,
            String institutionName,
            String countryCode,
            Integer worksCount,
            Integer citedByCount
    ) {
        double score = 0;

        String researcherName = normalizeForComparison(researcher.getFullName());
        String candidateName = normalizeForComparison(displayName);
        String researcherInstitution = normalizeForComparison(researcher.getInstitution());
        String candidateInstitution = normalizeForComparison(institutionName);

        if (hasText(researcherName) && researcherName.equals(candidateName)) {
            score += 60;
        } else if (hasText(researcherName) && hasText(candidateName)) {
            for (String part : researcherName.split(" ")) {
                if (part.length() >= 3 && candidateName.contains(part)) {
                    score += 8;
                }
            }
        }

        if (hasText(researcherInstitution) && hasText(candidateInstitution)) {
            if (candidateInstitution.contains(researcherInstitution)
                    || researcherInstitution.contains(candidateInstitution)) {
                score += 25;
            }
        }

        if ("AO".equalsIgnoreCase(countryCode)) {
            score += 10;
        }

        if (worksCount != null && worksCount > 0) {
            score += Math.min(worksCount, 20) * 0.5;
        }

        if (citedByCount != null && citedByCount > 0) {
            score += Math.min(citedByCount, 50) * 0.1;
        }

        return score;
    }

    private OpenAlexWork findOpenAlexWork(UUID workId) {
        return openAlexWorkRepository.findById(workId)
                .orElseThrow(() -> new IllegalArgumentException("Obra OpenAlex não encontrada."));
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

    private String normalizeOpenAlexAuthorId(String value) {
        String normalized = normalizeNullable(value);

        if (normalized == null) {
            throw new IllegalArgumentException("Author ID OpenAlex é obrigatório.");
        }

        normalized = URLDecoder.decode(normalized, StandardCharsets.UTF_8)
                .replace("https://openalex.org/", "")
                .replace("http://openalex.org/", "")
                .trim();

        if (!normalized.matches("A\\d+")) {
            throw new IllegalArgumentException("Author ID OpenAlex inválido. Use o formato A123456789.");
        }

        return normalized;
    }

    private String extractShortAuthorId(String openAlexAuthorId) {
        if (!hasText(openAlexAuthorId)) {
            return null;
        }

        return openAlexAuthorId
                .replace("https://openalex.org/", "")
                .replace("http://openalex.org/", "")
                .trim();
    }

    private String text(JsonNode node, String... path) {
        JsonNode current = node;

        for (String part : path) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return null;
            }

            if (current.isArray()) {
                try {
                    int index = Integer.parseInt(part);
                    current = current.path(index);
                } catch (NumberFormatException exception) {
                    return null;
                }
            } else {
                current = current.path(part);
            }
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

    private String normalizeForComparison(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
