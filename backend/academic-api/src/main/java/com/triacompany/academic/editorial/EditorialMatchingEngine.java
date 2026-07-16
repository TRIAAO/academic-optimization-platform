package com.triacompany.academic.editorial;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class EditorialMatchingEngine {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "among", "analysis", "article", "based", "between", "com", "como",
            "dados", "data", "estudo", "estudos", "from", "investigacao", "para", "pesquisa",
            "research", "resultados", "results", "sobre", "study", "through", "using", "with"
    );

    public List<EditorialJournalRecommendationResponse> match(
            TargetWork target,
            List<SimilarWork> similarWorks,
            Set<String> researcherSources
    ) {
        Set<String> targetTokens = tokens(target.title() + " " + target.abstractText());
        Set<String> normalizedHistory = new HashSet<>();
        safeSet(researcherSources).forEach(value -> normalizedHistory.add(normalize(value)));

        Map<String, JournalAccumulator> journals = new LinkedHashMap<>();
        int position = 0;

        for (SimilarWork work : safeList(similarWorks)) {
            position++;
            if (!hasText(work.journalName()) || !hasText(work.title())) {
                continue;
            }

            int similarity = titleSimilarity(targetTokens, tokens(work.title()));
            if (similarity < 8) {
                continue;
            }

            String key = normalize(work.journalName());
            JournalAccumulator journal = journals.computeIfAbsent(
                    key,
                    ignored -> new JournalAccumulator(work.journalName().trim())
            );

            journal.publisher = firstText(journal.publisher, work.publisher());
            journal.issns.addAll(safeList(work.issns()));
            journal.relatedWorks++;
            journal.relatedCitations += Math.max(0, work.referencedByCount());
            journal.rankWeight += Math.max(5, 45 - position);
            journal.maximumSimilarity = Math.max(journal.maximumSimilarity, similarity);

            if (journal.sampleTitle == null || similarity > journal.sampleSimilarity) {
                journal.sampleSimilarity = similarity;
                journal.sampleTitle = clean(work.title());
                journal.sampleDoi = normalizeNullable(work.doi());
                journal.sampleUrl = normalizeNullable(work.url());
            }
        }

        return journals.entrySet().stream()
                .map(entry -> toResponse(
                        entry.getValue(),
                        normalizedHistory.contains(entry.getKey())
                ))
                .sorted(Comparator
                        .comparingInt(EditorialJournalRecommendationResponse::relevanceScore)
                        .reversed()
                        .thenComparing(EditorialJournalRecommendationResponse::journalName))
                .limit(8)
                .toList();
    }

    private EditorialJournalRecommendationResponse toResponse(
            JournalAccumulator journal,
            boolean presentInHistory
    ) {
        int score = Math.min(
                96,
                30
                        + Math.min(24, journal.relatedWorks * 6)
                        + Math.min(16, journal.rankWeight / 6)
                        + Math.min(18, Math.round(journal.maximumSimilarity * 0.18f))
                        + (presentInHistory ? 8 : 0)
        );

        String rationale = journal.relatedWorks + " publicação(ões) bibliograficamente próxima(s) no Crossref; "
                + "similaridade temática máxima de " + journal.maximumSimilarity + "%"
                + (presentInHistory
                ? ". O veículo também aparece no histórico consolidado do pesquisador."
                : ". O veículo não aparece no histórico consolidado e exige avaliação editorial adicional.");

        return new EditorialJournalRecommendationResponse(
                journal.name,
                journal.publisher,
                List.copyOf(journal.issns),
                journal.relatedWorks,
                journal.relatedCitations,
                score,
                confidence(score),
                presentInHistory,
                journal.maximumSimilarity,
                journal.sampleTitle,
                journal.sampleDoi,
                journal.sampleUrl,
                rationale
        );
    }

    private int titleSimilarity(Set<String> targetTokens, Set<String> titleTokens) {
        if (targetTokens.isEmpty() || titleTokens.isEmpty()) {
            return 0;
        }

        Set<String> intersection = new HashSet<>(titleTokens);
        intersection.retainAll(targetTokens);
        return Math.round(intersection.size() * 100f / titleTokens.size());
    }

    private Set<String> tokens(String value) {
        Set<String> result = new HashSet<>();
        String normalized = normalize(clean(value));

        if (normalized == null) {
            return result;
        }

        for (String token : NON_WORD.split(normalized)) {
            if (token.length() >= 3 && !STOP_WORDS.contains(token)) {
                result.add(token);
            }
        }

        return result;
    }

    private String confidence(int score) {
        if (score >= 75) {
            return "HIGH";
        }
        if (score >= 55) {
            return "MEDIUM";
        }
        return "INITIAL";
    }

    private String normalize(String value) {
        String cleaned = normalizeNullable(value);
        if (cleaned == null) {
            return null;
        }

        return Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? "" : HTML_TAG.matcher(normalized).replaceAll(" ").replaceAll("\\s+", " ").trim();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String firstText(String current, String candidate) {
        return hasText(current) ? current : normalizeNullable(candidate);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private Set<String> safeSet(Set<String> values) {
        return values == null ? Set.of() : values;
    }

    public record TargetWork(String title, String abstractText) {
    }

    public record SimilarWork(
            String title,
            String journalName,
            String publisher,
            List<String> issns,
            String doi,
            String url,
            int referencedByCount
    ) {
    }

    private static class JournalAccumulator {
        private final String name;
        private final Set<String> issns = new LinkedHashSet<>();
        private String publisher;
        private int relatedWorks;
        private int relatedCitations;
        private int rankWeight;
        private int maximumSimilarity;
        private int sampleSimilarity;
        private String sampleTitle;
        private String sampleDoi;
        private String sampleUrl;

        private JournalAccumulator(String name) {
            this.name = name;
        }
    }
}
