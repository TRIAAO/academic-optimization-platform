package com.triacompany.academic.editorial;

import com.fasterxml.jackson.databind.JsonNode;
import com.triacompany.academic.crossref.CrossrefClient;
import com.triacompany.academic.editorial.EditorialMatchingEngine.SimilarWork;
import com.triacompany.academic.editorial.EditorialMatchingEngine.TargetWork;
import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditorialRecommendationService {

    private static final String METHODOLOGY = "Matching determinístico entre o título e o abstract selecionados e "
            + "publicações bibliograficamente próximas recuperadas no Crossref. Os periódicos são agregados por "
            + "recorrência, posição dos resultados, similaridade dos títulos e presença no histórico do pesquisador.";
    private static final String DECISION_POLICY = "As sugestões não constituem ranking de qualidade nem previsão de "
            + "aceite. Antes da submissão, confirme escopo, revisão por pares, indexação, custos, idioma, prazos e "
            + "políticas editoriais diretamente no site oficial do periódico.";

    private final ResearcherRepository researcherRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;
    private final CrossrefClient crossrefClient;
    private final EditorialMatchingEngine engine = new EditorialMatchingEngine();

    @Transactional(readOnly = true)
    public EditorialRecommendationResponse generate(UUID researcherId, UUID requestedWorkId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        List<OpenAlexWork> confirmedWorks = openAlexWorkRepository
                .findByResearcherIdAndReviewStatusOrderByPublicationYearDescTitleAsc(
                        researcherId,
                        PublicationReviewStatus.CONFIRMED
                );
        List<OpenAlexWork> eligibleWorks = confirmedWorks.stream()
                .filter(this::hasUsableAbstract)
                .toList();
        OpenAlexWork selectedWork = selectWork(eligibleWorks, requestedWorkId);

        if (selectedWork == null) {
            return response(
                    researcher,
                    confirmedWorks,
                    eligibleWorks,
                    null,
                    List.of(),
                    false,
                    "Confirme uma obra e sincronize o abstract no OpenAlex para iniciar o direcionamento editorial."
            );
        }

        try {
            JsonNode crossrefResponse = crossrefClient.searchSimilarJournalArticles(
                    bibliographicQuery(selectedWork),
                    40
            );
            List<SimilarWork> similarWorks = parseSimilarWorks(
                    crossrefResponse.path("message").path("items")
            );
            List<EditorialJournalRecommendationResponse> journals = engine.match(
                    new TargetWork(selectedWork.getTitle(), usableAbstract(selectedWork)),
                    similarWorks,
                    historicalSources(confirmedWorks)
            );

            return response(
                    researcher,
                    confirmedWorks,
                    eligibleWorks,
                    selectedWork,
                    journals,
                    true,
                    journals.isEmpty()
                            ? "Nenhum periódico atingiu a evidência mínima para esta obra."
                            : journals.size() + " periódico(s) candidato(s) encontrado(s) para revisão humana."
            );
        } catch (IllegalArgumentException exception) {
            return response(
                    researcher,
                    confirmedWorks,
                    eligibleWorks,
                    selectedWork,
                    List.of(),
                    false,
                    exception.getMessage()
            );
        }
    }

    private EditorialRecommendationResponse response(
            Researcher researcher,
            List<OpenAlexWork> confirmedWorks,
            List<OpenAlexWork> eligibleWorks,
            OpenAlexWork selectedWork,
            List<EditorialJournalRecommendationResponse> journals,
            boolean crossrefAvailable,
            String statusMessage
    ) {
        return new EditorialRecommendationResponse(
                researcher.getId(),
                researcher.getFullName(),
                confirmedWorks.size(),
                eligibleWorks.size(),
                eligibleWorks.stream().map(this::toWorkResponse).toList(),
                selectedWork == null ? null : selectedWork.getId(),
                selectedWork == null ? null : selectedWork.getTitle(),
                selectedWork == null ? null : selectedWork.getAbstractLanguage(),
                selectedWork == null ? null : selectedWork.getSourceName(),
                journals.size(),
                EditorialEvidenceLevel.from(
                        journals.stream()
                                .mapToInt(EditorialJournalRecommendationResponse::relevanceScore)
                                .max()
                                .orElse(0),
                        selectedWork != null
                ),
                crossrefAvailable,
                statusMessage,
                journals,
                METHODOLOGY,
                DECISION_POLICY,
                LocalDateTime.now()
        );
    }

    private OpenAlexWork selectWork(List<OpenAlexWork> eligibleWorks, UUID requestedWorkId) {
        if (requestedWorkId == null) {
            return eligibleWorks.stream().findFirst().orElse(null);
        }

        return eligibleWorks.stream()
                .filter(work -> requestedWorkId.equals(work.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "A obra selecionada não está confirmada ou não possui abstract disponível."
                ));
    }

    private EditorialWorkResponse toWorkResponse(OpenAlexWork work) {
        return new EditorialWorkResponse(
                work.getId(),
                work.getTitle(),
                work.getPublicationYear(),
                work.getSourceName(),
                firstText(work.getAbstractLanguage(), inferredTranslationLanguage(work))
        );
    }

    private String bibliographicQuery(OpenAlexWork work) {
        String value = (firstText(work.getTitle(), "") + " " + usableAbstract(work))
                .replaceAll("\\s+", " ")
                .trim();
        return value.length() > 900 ? value.substring(0, 900) : value;
    }

    private String usableAbstract(OpenAlexWork work) {
        String value = firstText(work.getAbstractText(), work.getAbstractEn(), work.getAbstractPt());
        return value == null ? "" : value;
    }

    private boolean hasUsableAbstract(OpenAlexWork work) {
        return !usableAbstract(work).isBlank();
    }

    private String inferredTranslationLanguage(OpenAlexWork work) {
        if (hasText(work.getAbstractEn())) {
            return "en";
        }
        if (hasText(work.getAbstractPt())) {
            return "pt";
        }
        return null;
    }

    private Set<String> historicalSources(List<OpenAlexWork> works) {
        Set<String> sources = new LinkedHashSet<>();
        works.stream()
                .map(OpenAlexWork::getSourceName)
                .filter(this::hasText)
                .map(String::trim)
                .forEach(sources::add);
        return sources;
    }

    private List<SimilarWork> parseSimilarWorks(JsonNode items) {
        if (!items.isArray()) {
            return List.of();
        }

        List<SimilarWork> result = new ArrayList<>();
        for (JsonNode item : items) {
            String title = firstArrayText(item, "title");
            String journal = firstArrayText(item, "container-title");
            if (!hasText(title) || !hasText(journal)) {
                continue;
            }

            result.add(new SimilarWork(
                    title,
                    journal,
                    text(item, "publisher"),
                    textArray(item, "ISSN"),
                    text(item, "DOI"),
                    safeHttpUrl(text(item, "URL")),
                    integerOrZero(item, "is-referenced-by-count")
            ));
        }
        return result;
    }

    private String firstArrayText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isArray() && !value.isEmpty() ? normalizeNullable(value.get(0).asText(null)) : null;
    }

    private List<String> textArray(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isArray()) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        value.forEach(item -> {
            String text = normalizeNullable(item.asText(null));
            if (text != null) {
                result.add(text);
            }
        });
        return result;
    }

    private String text(JsonNode node, String field) {
        return normalizeNullable(node.path(field).asText(null));
    }

    private int integerOrZero(JsonNode node, String field) {
        return Math.max(0, node.path(field).asInt(0));
    }

    private String safeHttpUrl(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }

        try {
            String scheme = URI.create(normalized).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)
                    ? normalized
                    : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String normalizeNullable(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
