package com.triacompany.academic.deduplication;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BibliographicSimilarityEngine {

    private static final int CANDIDATE_THRESHOLD = 72;
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "o", "as", "os", "um", "uma", "de", "da", "do", "das", "dos",
            "e", "em", "na", "no", "nas", "nos", "para", "por", "com", "sem",
            "the", "a", "an", "and", "of", "in", "on", "for", "to", "with", "without"
    );

    public Result compare(
            String leftTitle,
            String leftDoi,
            Integer leftYear,
            String rightTitle,
            String rightDoi,
            Integer rightYear
    ) {
        String normalizedLeftTitle = normalizeTitle(leftTitle);
        String normalizedRightTitle = normalizeTitle(rightTitle);
        String normalizedLeftDoi = normalizeDoi(leftDoi);
        String normalizedRightDoi = normalizeDoi(rightDoi);

        boolean doiExactMatch = normalizedLeftDoi != null
                && normalizedLeftDoi.equals(normalizedRightDoi);
        boolean bothDoiPresent = normalizedLeftDoi != null && normalizedRightDoi != null;
        boolean publicationYearCompatible = yearsCompatible(leftYear, rightYear);

        int characterSimilarity = characterSimilarity(normalizedLeftTitle, normalizedRightTitle);
        int tokenSimilarity = tokenSimilarity(normalizedLeftTitle, normalizedRightTitle);
        int titleSimilarity = Math.max(
                tokenSimilarity,
                (int) Math.round((characterSimilarity * 0.65) + (tokenSimilarity * 0.35))
        );

        int score;
        if (doiExactMatch) {
            score = 100;
        } else {
            score = titleSimilarity;
            score += publicationYearCompatible ? 5 : -18;
            if (bothDoiPresent) {
                score -= 18;
            }
            if (!normalizedLeftTitle.isBlank()
                    && normalizedLeftTitle.equals(normalizedRightTitle)) {
                score += 5;
            }
            score = clamp(score);
        }

        boolean candidate = doiExactMatch
                || (score >= CANDIDATE_THRESHOLD && titleSimilarity >= 65);

        String rationale = buildRationale(
                doiExactMatch,
                bothDoiPresent,
                titleSimilarity,
                publicationYearCompatible,
                leftYear,
                rightYear
        );

        return new Result(
                candidate,
                score,
                titleSimilarity,
                doiExactMatch,
                publicationYearCompatible,
                rationale
        );
    }

    String normalizeTitle(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return withoutAccents
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    String normalizeDoi(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://(dx\\.)?doi\\.org/", "")
                .replaceFirst("^doi:\\s*", "")
                .trim();

        return normalized.isBlank() ? null : normalized;
    }

    private boolean yearsCompatible(Integer leftYear, Integer rightYear) {
        if (leftYear == null || rightYear == null) {
            return true;
        }
        return Math.abs(leftYear - rightYear) <= 1;
    }

    private int characterSimilarity(String left, String right) {
        if (left.isBlank() || right.isBlank()) {
            return 0;
        }
        if (left.equals(right)) {
            return 100;
        }

        int maxLength = Math.max(left.length(), right.length());
        int distance = levenshteinDistance(left, right);
        double similarity = 1.0 - ((double) distance / maxLength);
        return clamp((int) Math.round(similarity * 100));
    }

    private int tokenSimilarity(String left, String right) {
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);

        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0;
        }

        Set<String> intersection = new LinkedHashSet<>(leftTokens);
        intersection.retainAll(rightTokens);

        Set<String> union = new LinkedHashSet<>(leftTokens);
        union.addAll(rightTokens);

        return clamp((int) Math.round((intersection.size() * 100.0) / union.size()));
    }

    private Set<String> tokens(String normalizedTitle) {
        if (normalizedTitle.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(normalizedTitle.split("\\s+"))
                .filter(token -> token.length() > 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int levenshteinDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];

        for (int index = 0; index <= right.length(); index++) {
            previous[index] = index;
        }

        for (int leftIndex = 1; leftIndex <= left.length(); leftIndex++) {
            current[0] = leftIndex;
            for (int rightIndex = 1; rightIndex <= right.length(); rightIndex++) {
                int substitutionCost = left.charAt(leftIndex - 1) == right.charAt(rightIndex - 1)
                        ? 0
                        : 1;
                current[rightIndex] = Math.min(
                        Math.min(
                                current[rightIndex - 1] + 1,
                                previous[rightIndex] + 1
                        ),
                        previous[rightIndex - 1] + substitutionCost
                );
            }

            int[] temporary = previous;
            previous = current;
            current = temporary;
        }

        return previous[right.length()];
    }

    private String buildRationale(
            boolean doiExactMatch,
            boolean bothDoiPresent,
            int titleSimilarity,
            boolean publicationYearCompatible,
            Integer leftYear,
            Integer rightYear
    ) {
        if (doiExactMatch) {
            return "DOI idêntico nas duas fontes; a confirmação humana continua obrigatória.";
        }

        StringBuilder rationale = new StringBuilder()
                .append("Similaridade de título: ")
                .append(titleSimilarity)
                .append("%. ");

        if (publicationYearCompatible) {
            rationale.append("Anos de publicação compatíveis. ");
        } else {
            rationale.append("Anos divergentes (")
                    .append(leftYear)
                    .append(" e ")
                    .append(rightYear)
                    .append("). ");
        }

        if (bothDoiPresent) {
            rationale.append("Os DOI informados são diferentes; revisar com atenção.");
        } else {
            rationale.append("Ao menos uma fonte não possui DOI para confirmação exata.");
        }

        return rationale.toString().trim();
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public record Result(
            boolean candidate,
            int similarityScore,
            int titleSimilarity,
            boolean doiExactMatch,
            boolean publicationYearCompatible,
            String rationale
    ) {
    }
}
