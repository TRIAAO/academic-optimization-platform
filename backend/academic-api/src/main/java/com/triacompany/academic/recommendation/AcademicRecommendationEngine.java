package com.triacompany.academic.recommendation;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AcademicRecommendationEngine {

    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final String METHODOLOGY = "Análise determinística baseada no perfil institucional, títulos e fontes "
            + "das obras consolidadas, validações Crossref, métricas registradas e relações de autoria do OpenAlex. "
            + "As sugestões apoiam decisão humana e não representam garantia de publicação, citação ou colaboração.";
    private static final String GOOGLE_SCHOLAR_POLICY = "O Google Acadêmico permanece exclusivamente manual: "
            + "a plataforma não automatiza, não acessa, não altera e não coleta dados diretamente do serviço.";

    private static final Set<String> STOP_WORDS = Set.of(
            "about", "abordagem", "acerca", "across", "after", "against", "analise", "analysis",
            "among", "aplicacao", "approach", "article", "artigo", "based", "between", "caso",
            "com", "como", "comparative", "data", "dados", "desenvolvimento", "development", "during",
            "estudo", "estudos", "evaluation", "from", "impact", "impacto", "investigacao", "modelo",
            "model", "para", "paper", "pesquisa", "research", "resultados", "results", "review", "revisao",
            "sobre", "study", "system", "sistema", "through", "towards", "using", "utilizacao", "with"
    );

    public AcademicRecommendationResponse generate(RecommendationContext context) {
        List<MergedWork> works = mergeWorks(context.works());
        int evidenceScore = calculateEvidenceScore(context);

        return new AcademicRecommendationResponse(
                context.researcherId(),
                safeText(context.researcherName(), "Pesquisador"),
                safeText(context.researchArea(), "Não informada"),
                evidenceScore,
                evidenceLevel(evidenceScore),
                new RecommendationEvidenceResponse(
                        clamp(context.profileCompletionPercentage(), 0, 100),
                        Math.max(0, context.orcidWorks()),
                        Math.max(0, context.openAlexWorks()),
                        Math.max(0, context.confirmedOpenAlexWorks()),
                        Math.max(0, context.crossrefValidations()),
                        context.scientometricSnapshotAvailable(),
                        context.openAlexEnrichmentAvailable()
                ),
                buildKeywordRecommendations(context, works),
                buildCollaboratorRecommendations(context.collaborators()),
                buildJournalRecommendations(works),
                buildNextActions(context, works),
                METHODOLOGY,
                GOOGLE_SCHOLAR_POLICY,
                context.generatedAt() == null ? LocalDateTime.now() : context.generatedAt()
        );
    }

    private List<KeywordRecommendationResponse> buildKeywordRecommendations(
            RecommendationContext context,
            List<MergedWork> works
    ) {
        Map<String, KeywordCandidate> candidates = new HashMap<>();

        addProfilePhrase(candidates, context.researchArea());
        splitProfileTerms(context.profileKeywords()).forEach(value -> addProfilePhrase(candidates, value));
        splitProfileTerms(context.interests()).forEach(value -> addProfilePhrase(candidates, value));

        for (MergedWork work : works) {
            List<TitleToken> tokens = titleTokens(work.title);
            Set<String> seenInWork = new HashSet<>();

            for (TitleToken token : tokens) {
                if (seenInWork.add(token.normalized)) {
                    addProductionPhrase(candidates, token.normalized, token.display);
                }
            }

            for (int index = 0; index + 1 < tokens.size(); index++) {
                TitleToken left = tokens.get(index);
                TitleToken right = tokens.get(index + 1);
                String normalized = left.normalized + " " + right.normalized;

                if (seenInWork.add(normalized)) {
                    addProductionPhrase(candidates, normalized, left.display + " " + right.display);
                }
            }
        }

        List<KeywordCandidate> ranked = candidates.values().stream()
                .filter(candidate -> !isResearcherNameCandidate(
                        candidate.normalized,
                        context.researcherName()
                ))
                .filter(candidate -> candidate.profileSeed || candidate.productionEvidence >= 2)
                .sorted(keywordComparator())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (ranked.size() < 8) {
            Set<String> selected = new HashSet<>();
            ranked.forEach(candidate -> selected.add(candidate.normalized));

            candidates.values().stream()
                    .filter(candidate -> !selected.contains(candidate.normalized))
                    .filter(candidate -> !isResearcherNameCandidate(
                            candidate.normalized,
                            context.researcherName()
                    ))
                    .sorted(keywordComparator())
                    .limit(8L - ranked.size())
                    .forEach(ranked::add);
        }

        return ranked.stream()
                .limit(8)
                .map(this::toKeywordResponse)
                .toList();
    }

    private Comparator<KeywordCandidate> keywordComparator() {
        return Comparator
                .comparingInt(this::keywordScore)
                .reversed()
                .thenComparing(Comparator.comparingInt((KeywordCandidate value) -> value.productionEvidence).reversed())
                .thenComparing(value -> value.normalized);
    }

    private KeywordRecommendationResponse toKeywordResponse(KeywordCandidate candidate) {
        int score = keywordScore(candidate);
        int evidenceCount = Math.max(1, candidate.productionEvidence);
        String source = candidate.profileSeed && candidate.productionEvidence > 0
                ? "PERFIL_E_PRODUCAO"
                : candidate.profileSeed ? "PERFIL" : "PRODUCAO_CIENTIFICA";

        String rationale;
        if (candidate.profileSeed && candidate.productionEvidence > 0) {
            rationale = "Tema presente no perfil e recorrente em " + candidate.productionEvidence
                    + " obra(s) consolidada(s).";
        } else if (candidate.profileSeed) {
            rationale = "Tema declarado no perfil acadêmico e recomendado para padronização institucional.";
        } else {
            rationale = "Termo recorrente em " + candidate.productionEvidence
                    + " título(s) da produção científica consolidada.";
        }

        return new KeywordRecommendationResponse(
                displayPhrase(candidate.display),
                evidenceCount,
                score,
                confidence(score),
                source,
                rationale
        );
    }

    private int keywordScore(KeywordCandidate candidate) {
        int score = 38 + Math.min(36, candidate.productionEvidence * 12);
        if (candidate.profileSeed) {
            score += 14;
        }
        if (candidate.normalized.contains(" ")) {
            score += 8;
        }
        return Math.min(98, score);
    }

    private void addProfilePhrase(Map<String, KeywordCandidate> candidates, String value) {
        String display = normalizeWhitespace(value);
        String normalized = normalizePhrase(display);

        if (!isUsefulPhrase(normalized) || display.length() > 80) {
            return;
        }

        KeywordCandidate candidate = candidates.computeIfAbsent(
                normalized,
                ignored -> new KeywordCandidate(normalized, display)
        );
        candidate.profileSeed = true;
    }

    private void addProductionPhrase(
            Map<String, KeywordCandidate> candidates,
            String normalized,
            String display
    ) {
        KeywordCandidate candidate = candidates.computeIfAbsent(
                normalized,
                ignored -> new KeywordCandidate(normalized, display)
        );
        candidate.productionEvidence++;
    }

    private List<CollaboratorRecommendationResponse> buildCollaboratorRecommendations(
            List<CollaboratorEvidence> collaborators
    ) {
        return safeList(collaborators).stream()
                .filter(value -> hasText(value.displayName()))
                .map(value -> {
                    int recency = value.latestCollaborationYear() == null
                            ? 0
                            : Math.max(0, 8 - Math.max(0, Year.now().getValue() - value.latestCollaborationYear()) * 2);
                    int score = Math.min(
                            98,
                            40
                                    + Math.min(36, Math.max(0, value.sharedWorks()) * 12)
                                    + Math.min(14, Math.max(0, value.sharedCitations()) / 10)
                                    + recency
                    );

                    String institution = safeText(value.institution(), "Instituição não informada no OpenAlex");
                    String rationale = Math.max(0, value.sharedWorks()) + " obra(s) em comum, "
                            + Math.max(0, value.sharedCitations()) + " citação(ões) associadas e vínculo mais recente em "
                            + (value.latestCollaborationYear() == null ? "ano não informado" : value.latestCollaborationYear())
                            + ". Continuidade sugerida com validação institucional.";

                    return new CollaboratorRecommendationResponse(
                            value.displayName().trim(),
                            normalizeNullable(value.openAlexAuthorId()),
                            normalizeNullable(value.orcid()),
                            institution,
                            Math.max(0, value.sharedWorks()),
                            Math.max(0, value.sharedCitations()),
                            value.latestCollaborationYear(),
                            score,
                            confidence(score),
                            rationale
                    );
                })
                .sorted(Comparator
                        .comparingInt(CollaboratorRecommendationResponse::relevanceScore)
                        .reversed()
                        .thenComparing(CollaboratorRecommendationResponse::displayName))
                .limit(6)
                .toList();
    }

    private List<JournalRecommendationResponse> buildJournalRecommendations(List<MergedWork> works) {
        Map<String, JournalEvidence> journals = new LinkedHashMap<>();

        for (MergedWork work : works) {
            if (!hasText(work.sourceName)) {
                continue;
            }

            String normalized = normalizePhrase(work.sourceName);
            JournalEvidence evidence = journals.computeIfAbsent(
                    normalized,
                    ignored -> new JournalEvidence(work.sourceName.trim())
            );

            evidence.relatedWorks++;
            evidence.totalCitations += Math.max(0, work.citedByCount);
            if (work.openAccess) {
                evidence.openAccessWorks++;
            }
            if (work.doiValidated) {
                evidence.validatedDoiWorks++;
            }
        }

        return journals.values().stream()
                .map(evidence -> {
                    int score = Math.min(
                            96,
                            42
                                    + Math.min(30, evidence.relatedWorks * 10)
                                    + Math.min(14, evidence.totalCitations / 10)
                                    + Math.min(10, evidence.validatedDoiWorks * 5)
                    );

                    return new JournalRecommendationResponse(
                            evidence.name,
                            evidence.relatedWorks,
                            evidence.totalCitations,
                            evidence.openAccessWorks,
                            evidence.validatedDoiWorks,
                            score,
                            confidence(score),
                            "Fonte recorrente no histórico consolidado. Verifique escopo, política editorial, indexação, "
                                    + "custos e chamada vigente antes de qualquer submissão."
                    );
                })
                .sorted(Comparator
                        .comparingInt(JournalRecommendationResponse::relevanceScore)
                        .reversed()
                        .thenComparing(JournalRecommendationResponse::journalName))
                .limit(6)
                .toList();
    }

    private List<AcademicRecommendationActionResponse> buildNextActions(
            RecommendationContext context,
            List<MergedWork> works
    ) {
        List<AcademicRecommendationActionResponse> actions = new ArrayList<>();

        if (context.profileCompletionPercentage() < 70
                || !hasText(context.researchArea())
                || !hasText(context.profileKeywords())) {
            actions.add(new AcademicRecommendationActionResponse(
                    "HIGH",
                    "PERFIL_ACADEMICO",
                    "Fortalecer a base temática do perfil",
                    "Completar área de pesquisa, biografia e palavras-chave antes de usar as recomendações em decisões institucionais.",
                    "/admin/academic-profiles"
            ));
        }

        if (context.orcidWorks() == 0) {
            actions.add(new AcademicRecommendationActionResponse(
                    "HIGH",
                    "ORCID",
                    "Consolidar produção via ORCID",
                    "Importar obras públicas do ORCID para ampliar a evidência usada em palavras-chave e periódicos.",
                    "/admin/orcid"
            ));
        }

        if (context.confirmedOpenAlexWorks() == 0) {
            actions.add(new AcademicRecommendationActionResponse(
                    "HIGH",
                    "OPENALEX",
                    "Validar autoria no OpenAlex",
                    "Localizar o autor correto e confirmar manualmente as obras antes de recomendar relações de colaboração.",
                    "/admin/openalex"
            ));
        }

        if (context.confirmedOpenAlexWorks() > 0 && context.crossrefValidations() == 0) {
            actions.add(new AcademicRecommendationActionResponse(
                    "MEDIUM",
                    "CROSSREF",
                    "Validar periódicos e DOI",
                    "Executar a validação Crossref das obras confirmadas para elevar a confiança das sugestões editoriais.",
                    "/admin/crossref"
            ));
        }

        if (!context.scientometricSnapshotAvailable()) {
            actions.add(new AcademicRecommendationActionResponse(
                    "MEDIUM",
                    "METRICAS",
                    "Registrar panorama cientométrico",
                    "Adicionar uma medição manual atual para relacionar as recomendações ao desempenho acadêmico observado.",
                    "/admin/scientometric-metrics"
            ));
        }

        if (!context.openAlexEnrichmentAvailable() && context.openAlexWorks() > 0) {
            actions.add(new AcademicRecommendationActionResponse(
                    "MEDIUM",
                    "COLABORACAO",
                    "Atualizar relações de autoria",
                    "Revalidar o ORCID e a identidade OpenAlex para recuperar colaboradores relacionados às obras públicas.",
                    "/admin/openalex"
            ));
        }

        if (works.stream().noneMatch(work -> hasText(work.sourceName))) {
            actions.add(new AcademicRecommendationActionResponse(
                    "LOW",
                    "PERIODICOS",
                    "Completar fontes de publicação",
                    "Revisar os metadados das obras para identificar periódicos e veículos científicos recorrentes.",
                    "/admin/crossref"
            ));
        }

        if (actions.isEmpty()) {
            actions.add(new AcademicRecommendationActionResponse(
                    "LOW",
                    "MONITORAMENTO",
                    "Revisar recomendações periodicamente",
                    "Atualizar ORCID, OpenAlex, Crossref e métricas após novas publicações ou alterações no perfil.",
                    "/admin/optimization"
            ));
        }

        return actions.stream().limit(6).toList();
    }

    private int calculateEvidenceScore(RecommendationContext context) {
        int score = Math.min(20, clamp(context.profileCompletionPercentage(), 0, 100) / 5);

        if (context.orcidWorks() > 0) {
            score += Math.min(20, 8 + context.orcidWorks());
        }

        if (context.openAlexWorks() > 0) {
            score += Math.min(25, 5 + Math.max(0, context.confirmedOpenAlexWorks()) * 4);
        }

        score += Math.min(20, Math.max(0, context.crossrefValidations()) * 3);

        if (context.scientometricSnapshotAvailable()) {
            score += 15;
        }

        return clamp(score, 0, 100);
    }

    private String evidenceLevel(int score) {
        if (score >= 75) {
            return "STRONG";
        }
        if (score >= 45) {
            return "MODERATE";
        }
        return "INITIAL";
    }

    private String confidence(int score) {
        if (score >= 78) {
            return "HIGH";
        }
        if (score >= 58) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<MergedWork> mergeWorks(List<WorkEvidence> works) {
        Map<String, MergedWork> merged = new LinkedHashMap<>();

        for (WorkEvidence value : safeList(works)) {
            if (!hasText(value.title())) {
                continue;
            }

            String key = normalizePhrase(value.title());
            MergedWork work = merged.computeIfAbsent(key, ignored -> new MergedWork(value.title().trim()));

            if (!hasText(work.sourceName) && hasText(value.sourceName())) {
                work.sourceName = value.sourceName().trim();
            }
            work.citedByCount = Math.max(work.citedByCount, Math.max(0, value.citedByCount()));
            work.openAccess = work.openAccess || value.openAccess();
            work.doiValidated = work.doiValidated || value.doiValidated();
        }

        return new ArrayList<>(merged.values());
    }

    private List<TitleToken> titleTokens(String title) {
        List<TitleToken> tokens = new ArrayList<>();
        Matcher matcher = WORD_PATTERN.matcher(safeText(title, ""));

        while (matcher.find()) {
            String display = matcher.group();
            String normalized = normalizePhrase(display);

            if (normalized.length() < 4
                    || normalized.chars().allMatch(Character::isDigit)
                    || STOP_WORDS.contains(normalized)) {
                continue;
            }

            tokens.add(new TitleToken(normalized, display));
        }

        return tokens;
    }

    private List<String> splitProfileTerms(String value) {
        if (!hasText(value)) {
            return List.of();
        }

        return Pattern.compile("[,;|\\n]")
                .splitAsStream(value)
                .map(this::normalizeWhitespace)
                .filter(this::hasText)
                .limit(20)
                .toList();
    }

    private boolean isUsefulPhrase(String normalized) {
        if (!hasText(normalized) || normalized.length() < 4) {
            return false;
        }

        List<String> tokens = List.of(normalized.split(" "));
        return tokens.stream().anyMatch(token -> token.length() >= 4 && !STOP_WORDS.contains(token));
    }

    private boolean isResearcherNameCandidate(String candidate, String researcherName) {
        if (!hasText(candidate)) {
            return false;
        }

        Set<String> nameTokens = researcherNameTokens(researcherName);
        List<String> candidateTokens = List.of(candidate.split(" "));
        return !candidateTokens.isEmpty()
                && !nameTokens.isEmpty()
                && candidateTokens.stream().allMatch(nameTokens::contains);
    }

    private Set<String> researcherNameTokens(String researcherName) {
        String normalizedName = normalizePhrase(researcherName);
        if (!hasText(normalizedName)) {
            return Set.of();
        }

        Set<String> tokens = new HashSet<>(List.of(normalizedName.split(" ")));
        tokens.removeIf(token -> token.length() < 3);
        return tokens;
    }

    private String displayPhrase(String value) {
        String normalized = normalizeWhitespace(value);
        if (!hasText(normalized)) {
            return normalized;
        }
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }

    private String normalizePhrase(String value) {
        String normalized = Normalizer.normalize(safeText(value, ""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private String normalizeWhitespace(String value) {
        return safeText(value, "").trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String value) {
        String normalized = normalizeWhitespace(value);
        return normalized.isEmpty() ? null : normalized;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.min(maximum, Math.max(minimum, value));
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    public record RecommendationContext(
            UUID researcherId,
            String researcherName,
            String researchArea,
            String profileKeywords,
            String interests,
            int profileCompletionPercentage,
            int orcidWorks,
            int openAlexWorks,
            int confirmedOpenAlexWorks,
            int crossrefValidations,
            boolean scientometricSnapshotAvailable,
            boolean openAlexEnrichmentAvailable,
            List<WorkEvidence> works,
            List<CollaboratorEvidence> collaborators,
            LocalDateTime generatedAt
    ) {
        public RecommendationContext {
            works = works == null ? List.of() : List.copyOf(works);
            collaborators = collaborators == null ? List.of() : List.copyOf(collaborators);
        }
    }

    public record WorkEvidence(
            String title,
            String sourceName,
            int citedByCount,
            boolean openAccess,
            boolean doiValidated
    ) {
    }

    public record CollaboratorEvidence(
            String displayName,
            String openAlexAuthorId,
            String orcid,
            String institution,
            int sharedWorks,
            int sharedCitations,
            Integer latestCollaborationYear
    ) {
    }

    private record TitleToken(String normalized, String display) {
    }

    private static class KeywordCandidate {
        private final String normalized;
        private final String display;
        private int productionEvidence;
        private boolean profileSeed;

        private KeywordCandidate(String normalized, String display) {
            this.normalized = normalized;
            this.display = display;
        }
    }

    private static class MergedWork {
        private final String title;
        private String sourceName;
        private int citedByCount;
        private boolean openAccess;
        private boolean doiValidated;

        private MergedWork(String title) {
            this.title = title;
        }
    }

    private static class JournalEvidence {
        private final String name;
        private int relatedWorks;
        private int totalCitations;
        private int openAccessWorks;
        private int validatedDoiWorks;

        private JournalEvidence(String name) {
            this.name = name;
        }
    }
}
