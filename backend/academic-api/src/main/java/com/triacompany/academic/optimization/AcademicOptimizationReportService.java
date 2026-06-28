package com.triacompany.academic.optimization;

import com.triacompany.academic.crossref.CrossrefMatchStatus;
import com.triacompany.academic.crossref.CrossrefValidation;
import com.triacompany.academic.crossref.CrossrefValidationRepository;
import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
import com.triacompany.academic.orcid.OrcidWorkRepository;
import com.triacompany.academic.profile.AcademicProfile;
import com.triacompany.academic.profile.AcademicProfileRepository;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcademicOptimizationReportService {

    private final ResearcherRepository researcherRepository;
    private final AcademicProfileRepository academicProfileRepository;
    private final OrcidWorkRepository orcidWorkRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;
    private final CrossrefValidationRepository crossrefValidationRepository;

    @Transactional(readOnly = true)
    public AcademicOptimizationReportResponse generateReport(UUID researcherId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        AcademicProfile profile = academicProfileRepository.findByResearcherId(researcherId)
                .orElse(null);

        List<OpenAlexWork> openAlexWorks = openAlexWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);

        List<CrossrefValidation> crossrefValidations = crossrefValidationRepository
                .findByResearcherIdOrderByValidatedAtDesc(researcherId);

        int totalOrcidWorks = orcidWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId).size();

        int totalOpenAlexWorks = openAlexWorks.size();
        int confirmedOpenAlexWorks = countByReviewStatus(openAlexWorks, PublicationReviewStatus.CONFIRMED);
        int pendingReviewOpenAlexWorks = countByReviewStatus(openAlexWorks, PublicationReviewStatus.PENDING_REVIEW);
        int rejectedOpenAlexWorks = countByReviewStatus(openAlexWorks, PublicationReviewStatus.REJECTED);

        int totalCrossrefValidations = crossrefValidations.size();
        int doiConfirmedCount = countByCrossrefStatus(crossrefValidations, CrossrefMatchStatus.DOI_CONFIRMED);
        int highConfidenceCount = countByCrossrefStatus(crossrefValidations, CrossrefMatchStatus.HIGH_CONFIDENCE);
        int possibleMatchCount = countByCrossrefStatus(crossrefValidations, CrossrefMatchStatus.POSSIBLE_MATCH);
        int doiMissingCount = countByCrossrefStatus(crossrefValidations, CrossrefMatchStatus.DOI_MISSING);
        int doiNotFoundCount = countByCrossrefStatus(crossrefValidations, CrossrefMatchStatus.DOI_NOT_FOUND);
        int errorCount = countByCrossrefStatus(crossrefValidations, CrossrefMatchStatus.ERROR);

        List<OptimizationScoreItemResponse> scoreItems = buildScoreItems(
                researcher,
                profile,
                totalOrcidWorks,
                totalOpenAlexWorks,
                confirmedOpenAlexWorks,
                pendingReviewOpenAlexWorks,
                totalCrossrefValidations,
                doiConfirmedCount,
                highConfidenceCount,
                possibleMatchCount,
                doiMissingCount,
                doiNotFoundCount,
                errorCount
        );

        int overallScore = scoreItems.stream()
                .mapToInt(OptimizationScoreItemResponse::score)
                .sum();

        String overallStatus = resolveOverallStatus(overallScore);

        List<OptimizationRecommendationResponse> recommendations = buildRecommendations(
                researcher,
                profile,
                totalOrcidWorks,
                totalOpenAlexWorks,
                confirmedOpenAlexWorks,
                pendingReviewOpenAlexWorks,
                totalCrossrefValidations,
                doiConfirmedCount,
                highConfidenceCount,
                possibleMatchCount,
                doiMissingCount,
                doiNotFoundCount,
                errorCount
        );

        String executiveSummary = buildExecutiveSummary(
                researcher,
                overallScore,
                overallStatus,
                totalOrcidWorks,
                confirmedOpenAlexWorks,
                doiConfirmedCount,
                pendingReviewOpenAlexWorks,
                doiMissingCount,
                doiNotFoundCount
        );

        return new AcademicOptimizationReportResponse(
                researcher.getId(),
                researcher.getFullName(),
                researcher.getEmail(),
                researcher.getInstitution(),
                researcher.getDepartment(),
                researcher.getAcademicTitle(),
                researcher.getOrcidId(),

                profile != null ? profile.getProfileCompletionPercentage() : 0,
                totalOrcidWorks,
                totalOpenAlexWorks,
                confirmedOpenAlexWorks,
                pendingReviewOpenAlexWorks,
                rejectedOpenAlexWorks,

                totalCrossrefValidations,
                doiConfirmedCount,
                highConfidenceCount,
                possibleMatchCount,
                doiMissingCount,
                doiNotFoundCount,
                errorCount,

                overallScore,
                overallStatus,
                executiveSummary,

                scoreItems,
                recommendations,

                LocalDateTime.now()
        );
    }

    private List<OptimizationScoreItemResponse> buildScoreItems(
            Researcher researcher,
            AcademicProfile profile,
            int totalOrcidWorks,
            int totalOpenAlexWorks,
            int confirmedOpenAlexWorks,
            int pendingReviewOpenAlexWorks,
            int totalCrossrefValidations,
            int doiConfirmedCount,
            int highConfidenceCount,
            int possibleMatchCount,
            int doiMissingCount,
            int doiNotFoundCount,
            int errorCount
    ) {
        List<OptimizationScoreItemResponse> items = new ArrayList<>();

        int profileScore = calculateProfileScore(profile);
        items.add(new OptimizationScoreItemResponse(
                "PROFILE_COMPLETION",
                "Completude do perfil acadêmico",
                profileScore,
                20,
                statusFromScore(profileScore, 20),
                profile == null
                        ? "O pesquisador ainda não possui perfil acadêmico estruturado."
                        : "Perfil acadêmico com " + profile.getProfileCompletionPercentage() + "% de preenchimento."
        ));

        int orcidScore = calculateOrcidScore(researcher, totalOrcidWorks);
        items.add(new OptimizationScoreItemResponse(
                "ORCID",
                "Presença e produção no ORCID",
                orcidScore,
                20,
                statusFromScore(orcidScore, 20),
                hasText(researcher.getOrcidId())
                        ? "ORCID informado com " + totalOrcidWorks + " obra(s) importada(s)."
                        : "ORCID não informado no cadastro do pesquisador."
        ));

        int openAlexScore = calculateOpenAlexScore(totalOpenAlexWorks, confirmedOpenAlexWorks, pendingReviewOpenAlexWorks);
        items.add(new OptimizationScoreItemResponse(
                "OPENALEX",
                "Presença e curadoria no OpenAlex",
                openAlexScore,
                20,
                statusFromScore(openAlexScore, 20),
                "OpenAlex possui " + totalOpenAlexWorks + " obra(s), sendo "
                        + confirmedOpenAlexWorks + " confirmada(s) e "
                        + pendingReviewOpenAlexWorks + " pendente(s)."
        ));

        int crossrefScore = calculateCrossrefScore(
                totalCrossrefValidations,
                doiConfirmedCount,
                highConfidenceCount,
                possibleMatchCount,
                doiMissingCount,
                doiNotFoundCount,
                errorCount
        );
        items.add(new OptimizationScoreItemResponse(
                "CROSSREF",
                "Validação de DOI e metadados no Crossref",
                crossrefScore,
                25,
                statusFromScore(crossrefScore, 25),
                "Crossref possui " + totalCrossrefValidations
                        + " validação(ões), com " + doiConfirmedCount
                        + " DOI(s) confirmado(s)."
        ));

        int institutionalVisibilityScore = calculateInstitutionalVisibilityScore(researcher, profile);
        items.add(new OptimizationScoreItemResponse(
                "INSTITUTIONAL_VISIBILITY",
                "Visibilidade institucional",
                institutionalVisibilityScore,
                15,
                statusFromScore(institutionalVisibilityScore, 15),
                "Avaliação de vínculo institucional, links acadêmicos e presença pública do pesquisador."
        ));

        return items;
    }

    private int calculateProfileScore(AcademicProfile profile) {
        if (profile == null || profile.getProfileCompletionPercentage() == null) {
            return 0;
        }

        return Math.min(20, Math.max(0, Math.round(profile.getProfileCompletionPercentage() * 20.0f / 100.0f)));
    }

    private int calculateOrcidScore(Researcher researcher, int totalOrcidWorks) {
        int score = 0;

        if (hasText(researcher.getOrcidId())) {
            score += 10;
        }

        if (totalOrcidWorks > 0) {
            score += Math.min(10, totalOrcidWorks);
        }

        return score;
    }

    private int calculateOpenAlexScore(
            int totalOpenAlexWorks,
            int confirmedOpenAlexWorks,
            int pendingReviewOpenAlexWorks
    ) {
        if (totalOpenAlexWorks == 0) {
            return 0;
        }

        int score = 5;

        score += Math.min(10, confirmedOpenAlexWorks * 3);

        if (pendingReviewOpenAlexWorks == 0 && confirmedOpenAlexWorks > 0) {
            score += 5;
        }

        return Math.min(20, score);
    }

    private int calculateCrossrefScore(
            int totalCrossrefValidations,
            int doiConfirmedCount,
            int highConfidenceCount,
            int possibleMatchCount,
            int doiMissingCount,
            int doiNotFoundCount,
            int errorCount
    ) {
        if (totalCrossrefValidations == 0) {
            return 0;
        }

        int score = 0;

        score += doiConfirmedCount * 8;
        score += highConfidenceCount * 6;
        score += possibleMatchCount * 3;

        score -= doiMissingCount * 2;
        score -= doiNotFoundCount * 3;
        score -= errorCount * 4;

        return Math.min(25, Math.max(0, score));
    }

    private int calculateInstitutionalVisibilityScore(Researcher researcher, AcademicProfile profile) {
        int score = 0;

        if (hasText(researcher.getInstitution())) {
            score += 4;
        }

        if (hasText(researcher.getDepartment())) {
            score += 3;
        }

        if (profile != null && hasText(profile.getInstitutionalProfileUrl())) {
            score += 4;
        }

        if (profile != null && hasText(profile.getGoogleScholarUrl())) {
            score += 2;
        }

        if (profile != null && hasText(profile.getOrcidUrl())) {
            score += 2;
        }

        return Math.min(15, score);
    }

    private List<OptimizationRecommendationResponse> buildRecommendations(
            Researcher researcher,
            AcademicProfile profile,
            int totalOrcidWorks,
            int totalOpenAlexWorks,
            int confirmedOpenAlexWorks,
            int pendingReviewOpenAlexWorks,
            int totalCrossrefValidations,
            int doiConfirmedCount,
            int highConfidenceCount,
            int possibleMatchCount,
            int doiMissingCount,
            int doiNotFoundCount,
            int errorCount
    ) {
        List<OptimizationRecommendationResponse> recommendations = new ArrayList<>();

        if (!hasText(researcher.getOrcidId())) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "HIGH",
                    "ORCID",
                    "Cadastrar o ORCID do pesquisador para permitir importação e validação automática da produção científica."
            ));
        }

        if (totalOrcidWorks == 0 && hasText(researcher.getOrcidId())) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "HIGH",
                    "ORCID",
                    "Atualizar o perfil ORCID com as publicações do pesquisador ou verificar se as obras estão públicas."
            ));
        }

        if (profile == null || profile.getProfileCompletionPercentage() == null
                || profile.getProfileCompletionPercentage() < 70) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "HIGH",
                    "Perfil Acadêmico",
                    "Completar biografia, área de pesquisa, palavras-chave, links institucionais e identificadores acadêmicos."
            ));
        }

        if (pendingReviewOpenAlexWorks > 0) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "MEDIUM",
                    "OpenAlex",
                    "Revisar e confirmar as obras pendentes importadas do OpenAlex para melhorar a confiabilidade do perfil."
            ));
        }

        if (totalOpenAlexWorks == 0) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "MEDIUM",
                    "OpenAlex",
                    "Buscar candidatos de autor no OpenAlex e importar obras apenas após aprovação institucional."
            ));
        }

        if (confirmedOpenAlexWorks > 0 && totalCrossrefValidations == 0) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "HIGH",
                    "Crossref",
                    "Validar as obras confirmadas no Crossref para verificar DOI, título, fonte e metadados bibliográficos."
            ));
        }

        if (doiMissingCount > 0) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "MEDIUM",
                    "DOI",
                    "Investigar obras sem DOI informado e atualizar os registros quando houver DOI oficial disponível."
            ));
        }

        if (doiNotFoundCount > 0 || errorCount > 0) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "MEDIUM",
                    "Crossref",
                    "Revisar manualmente as obras com DOI não encontrado ou erro de validação."
            ));
        }

        if (profile == null || !hasText(profile.getGoogleScholarUrl())) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "MEDIUM",
                    "Google Acadêmico",
                    "Adicionar o link do Google Acadêmico ao perfil e revisar manualmente se as publicações estão corretamente vinculadas."
            ));
        }

        if (recommendations.isEmpty()) {
            recommendations.add(new OptimizationRecommendationResponse(
                    "LOW",
                    "Manutenção",
                    "Perfil acadêmico bem estruturado. Recomenda-se apenas revisão periódica das publicações e metadados."
            ));
        }

        return recommendations;
    }

    private String buildExecutiveSummary(
            Researcher researcher,
            int overallScore,
            String overallStatus,
            int totalOrcidWorks,
            int confirmedOpenAlexWorks,
            int doiConfirmedCount,
            int pendingReviewOpenAlexWorks,
            int doiMissingCount,
            int doiNotFoundCount
    ) {
        return "O pesquisador " + researcher.getFullName()
                + " possui pontuação geral de " + overallScore + "/100, classificada como "
                + overallStatus + ". Foram identificadas "
                + totalOrcidWorks + " obra(s) no ORCID, "
                + confirmedOpenAlexWorks + " obra(s) confirmada(s) no OpenAlex e "
                + doiConfirmedCount + " DOI(s) confirmado(s) no Crossref. Existem "
                + pendingReviewOpenAlexWorks + " obra(s) pendente(s) de revisão, "
                + doiMissingCount + " caso(s) sem DOI informado e "
                + doiNotFoundCount + " DOI(s) não encontrado(s).";
    }

    private int countByReviewStatus(List<OpenAlexWork> works, PublicationReviewStatus status) {
        return Math.toIntExact(
                works.stream()
                        .filter(work -> work.getReviewStatus() == status)
                        .count()
        );
    }

    private int countByCrossrefStatus(List<CrossrefValidation> validations, CrossrefMatchStatus status) {
        return Math.toIntExact(
                validations.stream()
                        .filter(validation -> validation.getMatchStatus() == status)
                        .count()
        );
    }

    private String resolveOverallStatus(int score) {
        if (score >= 85) {
            return "EXCELENTE";
        }

        if (score >= 70) {
            return "BOM";
        }

        if (score >= 50) {
            return "EM_OTIMIZACAO";
        }

        return "CRITICO";
    }

    private String statusFromScore(int score, int maxScore) {
        double percentage = (score * 100.0) / maxScore;

        if (percentage >= 85) {
            return "EXCELENTE";
        }

        if (percentage >= 70) {
            return "BOM";
        }

        if (percentage >= 50) {
            return "EM_OTIMIZACAO";
        }

        return "CRITICO";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}