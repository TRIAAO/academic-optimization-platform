package com.triacompany.academic.crossref;

import com.fasterxml.jackson.databind.JsonNode;
import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrossrefService {

    private final CrossrefClient crossrefClient;
    private final CrossrefValidationRepository crossrefValidationRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;
    private final ResearcherRepository researcherRepository;

    @Transactional
    public CrossrefValidationResponse validateOpenAlexWork(UUID workId) {
        OpenAlexWork work = findOpenAlexWork(workId);

        String submittedDoi = normalizeDoi(work.getDoi());
        String submittedTitle = normalizeNullable(work.getTitle());

        if (submittedTitle == null) {
            throw new IllegalArgumentException("A obra não possui título para validação.");
        }

        try {
            JsonNode crossrefMessage;

            if (submittedDoi != null) {
                crossrefMessage = crossrefClient.fetchWorkByDoi(submittedDoi).path("message");
            } else {
                crossrefMessage = firstBibliographicCandidate(submittedTitle);
            }

            if (crossrefMessage == null || crossrefMessage.isMissingNode() || crossrefMessage.isNull()) {
                CrossrefValidation validation = buildValidation(
                        work,
                        submittedDoi,
                        null,
                        submittedTitle,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        BigDecimal.ZERO,
                        submittedDoi == null ? CrossrefMatchStatus.DOI_MISSING : CrossrefMatchStatus.DOI_NOT_FOUND,
                        submittedDoi == null
                                ? "A obra não possui DOI. A busca bibliográfica não retornou metadados suficientes."
                                : "DOI não encontrado no Crossref."
                );

                return CrossrefValidationResponse.fromEntity(crossrefValidationRepository.save(validation));
            }

            String foundDoi = normalizeDoi(text(crossrefMessage, "DOI"));
            String foundTitle = firstText(crossrefMessage, "title");
            String publisher = text(crossrefMessage, "publisher");
            String containerTitle = firstText(crossrefMessage, "container-title");
            String publicationType = text(crossrefMessage, "type");
            Integer publicationYear = publicationYear(crossrefMessage);

            BigDecimal titleSimilarity = calculateTitleSimilarity(submittedTitle, foundTitle);
            boolean doiValid = submittedDoi != null && foundDoi != null && submittedDoi.equalsIgnoreCase(foundDoi);

            CrossrefMatchStatus matchStatus = resolveStatus(
                    submittedDoi,
                    foundDoi,
                    doiValid,
                    titleSimilarity
            );

            String message = buildMessage(matchStatus, submittedDoi, foundDoi, titleSimilarity);

            CrossrefValidation validation = buildValidation(
                    work,
                    submittedDoi,
                    foundDoi,
                    submittedTitle,
                    foundTitle,
                    publisher,
                    containerTitle,
                    publicationType,
                    publicationYear,
                    doiValid,
                    titleSimilarity,
                    matchStatus,
                    message
            );

            return CrossrefValidationResponse.fromEntity(crossrefValidationRepository.save(validation));

        } catch (IllegalArgumentException exception) {
            CrossrefValidation validation = buildValidation(
                    work,
                    submittedDoi,
                    null,
                    submittedTitle,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    BigDecimal.ZERO,
                    submittedDoi == null ? CrossrefMatchStatus.DOI_MISSING : CrossrefMatchStatus.DOI_NOT_FOUND,
                    exception.getMessage()
            );

            return CrossrefValidationResponse.fromEntity(crossrefValidationRepository.save(validation));
        } catch (Exception exception) {
            CrossrefValidation validation = buildValidation(
                    work,
                    submittedDoi,
                    null,
                    submittedTitle,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    BigDecimal.ZERO,
                    CrossrefMatchStatus.ERROR,
                    "Erro inesperado ao validar metadados no Crossref."
            );

            return CrossrefValidationResponse.fromEntity(crossrefValidationRepository.save(validation));
        }
    }

    @Transactional(readOnly = true)
    public CrossrefValidationResponse findLatestValidationByOpenAlexWork(UUID workId) {
        if (!openAlexWorkRepository.existsById(workId)) {
            throw new IllegalArgumentException("Obra OpenAlex não encontrada.");
        }

        return crossrefValidationRepository.findTopByOpenAlexWorkIdOrderByValidatedAtDesc(workId)
                .map(CrossrefValidationResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Esta obra ainda não possui validação Crossref."));
    }

    @Transactional(readOnly = true)
    public List<CrossrefValidationResponse> findValidationsByResearcher(UUID researcherId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }

        return crossrefValidationRepository.findByResearcherIdOrderByValidatedAtDesc(researcherId)
                .stream()
                .map(CrossrefValidationResponse::fromEntity)
                .toList();
    }

    private JsonNode firstBibliographicCandidate(String title) {
        JsonNode response = crossrefClient.searchWorkByBibliographicTitle(title);
        JsonNode items = response.path("message").path("items");

        if (!items.isArray() || items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    private CrossrefValidation buildValidation(
            OpenAlexWork work,
            String doiSubmitted,
            String doiFound,
            String titleSubmitted,
            String titleFound,
            String publisher,
            String containerTitle,
            String publicationType,
            Integer publicationYear,
            Boolean isDoiValid,
            BigDecimal titleSimilarity,
            CrossrefMatchStatus matchStatus,
            String message
    ) {
        return CrossrefValidation.builder()
                .openAlexWork(work)
                .researcher(work.getResearcher())
                .doiSubmitted(doiSubmitted)
                .doiFound(doiFound)
                .titleSubmitted(titleSubmitted)
                .titleFound(titleFound)
                .publisher(publisher)
                .containerTitle(containerTitle)
                .publicationType(publicationType)
                .publicationYear(publicationYear)
                .isDoiValid(isDoiValid)
                .titleSimilarity(titleSimilarity)
                .matchStatus(matchStatus)
                .message(message)
                .rawSource("CROSSREF")
                .build();
    }

    private CrossrefMatchStatus resolveStatus(
            String submittedDoi,
            String foundDoi,
            boolean doiValid,
            BigDecimal titleSimilarity
    ) {
        if (submittedDoi == null) {
            return titleSimilarity.compareTo(BigDecimal.valueOf(80)) >= 0
                    ? CrossrefMatchStatus.POSSIBLE_MATCH
                    : CrossrefMatchStatus.DOI_MISSING;
        }

        if (foundDoi == null) {
            return CrossrefMatchStatus.DOI_NOT_FOUND;
        }

        if (doiValid && titleSimilarity.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return CrossrefMatchStatus.DOI_CONFIRMED;
        }

        if (doiValid && titleSimilarity.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return CrossrefMatchStatus.HIGH_CONFIDENCE;
        }

        if (doiValid) {
            return CrossrefMatchStatus.POSSIBLE_MATCH;
        }

        return CrossrefMatchStatus.POSSIBLE_MATCH;
    }

    private String buildMessage(
            CrossrefMatchStatus status,
            String submittedDoi,
            String foundDoi,
            BigDecimal titleSimilarity
    ) {
        return switch (status) {
            case DOI_CONFIRMED -> "DOI confirmado no Crossref com alta similaridade de título.";
            case HIGH_CONFIDENCE -> "DOI encontrado no Crossref, mas o título exige revisão adicional.";
            case POSSIBLE_MATCH -> "Possível correspondência encontrada. Recomenda-se revisão manual.";
            case DOI_NOT_FOUND -> "DOI informado não foi encontrado no Crossref.";
            case DOI_MISSING -> "A obra não possui DOI informado. Validação feita por busca bibliográfica quando possível.";
            case ERROR -> "Erro ao validar metadados no Crossref.";
        } + " DOI enviado: " + nullSafe(submittedDoi)
                + ". DOI encontrado: " + nullSafe(foundDoi)
                + ". Similaridade do título: " + titleSimilarity + "%.";
    }

    private OpenAlexWork findOpenAlexWork(UUID workId) {
        return openAlexWorkRepository.findById(workId)
                .orElseThrow(() -> new IllegalArgumentException("Obra OpenAlex não encontrada."));
    }

    private String normalizeDoi(String value) {
        String normalized = normalizeNullable(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .replace("https://doi.org/", "")
                .replace("http://doi.org/", "")
                .replace("doi:", "")
                .trim()
                .toLowerCase();
    }

    private BigDecimal calculateTitleSimilarity(String submittedTitle, String foundTitle) {
        String left = normalizeForComparison(submittedTitle);
        String right = normalizeForComparison(foundTitle);

        if (left == null || right == null) {
            return BigDecimal.ZERO;
        }

        if (left.equals(right)) {
            return BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        }

        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);

        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> intersection = new HashSet<>(leftTokens);
        intersection.retainAll(rightTokens);

        int maxSize = Math.max(leftTokens.size(), rightTokens.size());

        double score = (intersection.size() * 100.0) / maxSize;

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private Set<String> tokens(String value) {
        Set<String> result = new HashSet<>();

        if (value == null || value.isBlank()) {
            return result;
        }

        for (String token : value.split("\\s+")) {
            if (token.length() >= 3) {
                result.add(token);
            }
        }

        return result;
    }

    private String firstText(JsonNode node, String field) {
        JsonNode value = node.path(field);

        if (value.isArray() && !value.isEmpty()) {
            return normalizeNullable(value.get(0).asText(null));
        }

        if (value.isTextual()) {
            return normalizeNullable(value.asText(null));
        }

        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        return normalizeNullable(value.asText(null));
    }

    private Integer publicationYear(JsonNode node) {
        JsonNode dateParts = node.path("published-print").path("date-parts");

        if (!dateParts.isArray() || dateParts.isEmpty()) {
            dateParts = node.path("published-online").path("date-parts");
        }

        if (!dateParts.isArray() || dateParts.isEmpty()) {
            dateParts = node.path("issued").path("date-parts");
        }

        if (!dateParts.isArray() || dateParts.isEmpty()) {
            return null;
        }

        JsonNode firstDate = dateParts.get(0);

        if (!firstDate.isArray() || firstDate.isEmpty()) {
            return null;
        }

        JsonNode year = firstDate.get(0);

        if (!year.isNumber()) {
            return null;
        }

        return year.asInt();
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
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase()
                .trim();
    }

    private String nullSafe(String value) {
        return value == null ? "não informado" : value;
    }
}