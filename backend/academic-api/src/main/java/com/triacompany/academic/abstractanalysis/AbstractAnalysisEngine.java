package com.triacompany.academic.abstractanalysis;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractAnalysisEngine {

    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "ao", "aos", "as", "com", "como", "da", "das", "de", "do", "dos", "e",
            "em", "entre", "na", "nas", "no", "nos", "o", "os", "ou", "para", "por", "que",
            "sem", "sob", "sobre", "the", "an", "and", "at", "by", "for", "from",
            "in", "into", "of", "on", "or", "through", "to", "with", "without"
    );
    private static final Set<String> GENERIC_TERMS = Set.of(
            "abstract", "analise", "analysis", "artigo", "article", "dados", "data", "estudo",
            "estudos", "evaluation", "investigacao", "metodo", "method", "paper", "pesquisa",
            "research", "resultado", "resultados", "result", "results", "study", "studies"
    );
    private static final Set<String> METADATA_TERMS = Set.of(
            "autor", "author", "doi", "edicao", "edition", "editora", "editorial", "isbn", "issn",
            "journal", "pagina", "paginas", "publisher", "revista", "volume"
    );

    public List<AbstractThemeResponse> extractThemes(
            List<AbstractEvidence> evidence,
            String researchArea,
            String profileKeywords,
            String researcherName
    ) {
        if (evidence == null || evidence.isEmpty()) {
            return List.of();
        }

        Set<String> profileTerms = profileTerms(researchArea, profileKeywords);
        Set<String> researcherTokens = significantTokens(researcherName);
        Map<String, ThemeCandidate> candidates = new HashMap<>();

        for (AbstractEvidence item : evidence) {
            if (item == null || !hasText(item.text())) {
                continue;
            }

            List<Token> tokens = tokenize(item.text());
            Map<String, Integer> occurrencesInAbstract = new HashMap<>();

            for (int windowSize = 2; windowSize <= 3; windowSize++) {
                for (int index = 0; index + windowSize <= tokens.size(); index++) {
                    List<Token> window = tokens.subList(index, index + windowSize);
                    if (!eligibleWindow(window, researcherTokens)) {
                        continue;
                    }

                    String normalized = window.stream()
                            .map(Token::normalized)
                            .reduce((left, right) -> left + " " + right)
                            .orElse("");
                    occurrencesInAbstract.merge(normalized, 1, Integer::sum);
                }
            }

            occurrencesInAbstract.forEach((normalized, occurrences) -> {
                ThemeCandidate candidate = candidates.computeIfAbsent(
                        normalized,
                        ignored -> new ThemeCandidate(normalized, displayTheme(normalized))
                );
                candidate.abstractIds.add(item.workId());
                candidate.occurrences += occurrences;
            });
        }

        int abstractTotal = Math.toIntExact(evidence.stream()
                .filter(item -> item != null && hasText(item.text()))
                .count());

        return candidates.values().stream()
                .peek(candidate -> candidate.profileAligned = alignsWithProfile(candidate.normalized, profileTerms))
                .filter(candidate -> candidate.abstractIds.size() >= 2
                        || candidate.occurrences >= 2
                        || candidate.profileAligned)
                .peek(candidate -> candidate.score = score(candidate, abstractTotal))
                .sorted(Comparator
                        .comparingInt((ThemeCandidate candidate) -> candidate.score).reversed()
                        .thenComparing(Comparator.comparingInt(
                                (ThemeCandidate candidate) -> candidate.abstractIds.size()
                        ).reversed())
                        .thenComparing(candidate -> candidate.normalized))
                .limit(8)
                .map(this::toResponse)
                .toList();
    }

    private AbstractThemeResponse toResponse(ThemeCandidate candidate) {
        int abstractCount = candidate.abstractIds.size();
        String confidence = candidate.score >= 78
                ? "ALTA"
                : candidate.score >= 55 ? "MÉDIA" : "INICIAL";
        String profileNote = candidate.profileAligned
                ? " Também apresenta alinhamento com o perfil académico informado."
                : "";

        return new AbstractThemeResponse(
                candidate.display,
                abstractCount,
                candidate.occurrences,
                candidate.score,
                confidence,
                "ABSTRACTS_OPENALEX",
                "Expressão identificada em " + abstractCount + " abstract(s) confirmado(s), com "
                        + candidate.occurrences + " ocorrência(s)." + profileNote
        );
    }

    private int score(ThemeCandidate candidate, int abstractTotal) {
        int coverage = abstractTotal == 0
                ? 0
                : (int) Math.round(candidate.abstractIds.size() * 100.0 / abstractTotal);
        int score = 25
                + Math.min(35, candidate.abstractIds.size() * 12)
                + Math.min(15, candidate.occurrences * 3)
                + Math.min(15, coverage / 5)
                + (candidate.profileAligned ? 10 : 0)
                + (candidate.normalized.split(" ").length == 3 ? 4 : 0);
        return Math.max(0, Math.min(100, score));
    }

    private boolean eligibleWindow(List<Token> window, Set<String> researcherTokens) {
        if (window.isEmpty() || STOP_WORDS.contains(window.get(0).normalized())
                || STOP_WORDS.contains(window.get(window.size() - 1).normalized())) {
            return false;
        }

        List<String> significant = window.stream()
                .map(Token::normalized)
                .filter(this::isSignificant)
                .toList();

        if (significant.size() < 2) {
            return false;
        }

        if (significant.stream().anyMatch(METADATA_TERMS::contains)) {
            return false;
        }

        return researcherTokens.isEmpty() || !researcherTokens.containsAll(significant);
    }

    private boolean isSignificant(String token) {
        return token.length() >= 3
                && !STOP_WORDS.contains(token)
                && !GENERIC_TERMS.contains(token);
    }

    private List<Token> tokenize(String value) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = WORD_PATTERN.matcher(value);

        while (matcher.find()) {
            String normalized = normalize(matcher.group());
            if (normalized.length() >= 2) {
                tokens.add(new Token(normalized));
            }
        }

        return tokens;
    }

    private Set<String> profileTerms(String researchArea, String profileKeywords) {
        Set<String> terms = new LinkedHashSet<>();
        addProfileTerms(terms, researchArea);
        addProfileTerms(terms, profileKeywords);
        return terms;
    }

    private void addProfileTerms(Set<String> terms, String value) {
        if (!hasText(value)) {
            return;
        }

        for (String part : value.split("[,;|\\n]")) {
            String normalized = normalize(part);
            if (normalized.length() >= 3) {
                terms.add(normalized);
            }
        }
    }

    private boolean alignsWithProfile(String candidate, Set<String> profileTerms) {
        if (profileTerms.isEmpty()) {
            return false;
        }

        Set<String> candidateTokens = significantTokens(candidate);
        return profileTerms.stream().anyMatch(profileTerm -> {
            Set<String> profileTokens = significantTokens(profileTerm);
            return candidate.equals(profileTerm)
                    || profileTerm.contains(candidate)
                    || candidateTokens.equals(profileTokens);
        });
    }

    private Set<String> significantTokens(String value) {
        if (!hasText(value)) {
            return Set.of();
        }

        Set<String> tokens = new HashSet<>();
        Matcher matcher = WORD_PATTERN.matcher(value);
        while (matcher.find()) {
            String normalized = normalize(matcher.group());
            if (isSignificant(normalized)) {
                tokens.add(normalized);
            }
        }
        return tokens;
    }

    private String displayTheme(String normalized) {
        if (normalized.isEmpty()) {
            return normalized;
        }
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record AbstractEvidence(UUID workId, String text) {
    }

    private record Token(String normalized) {
    }

    private static final class ThemeCandidate {
        private final String normalized;
        private final String display;
        private final Set<UUID> abstractIds = new HashSet<>();
        private int occurrences;
        private boolean profileAligned;
        private int score;

        private ThemeCandidate(String normalized, String display) {
            this.normalized = normalized;
            this.display = display;
        }
    }
}
