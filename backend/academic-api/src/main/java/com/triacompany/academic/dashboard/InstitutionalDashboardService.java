
package com.triacompany.academic.dashboard;

import com.triacompany.academic.crossref.CrossrefMatchStatus;
import com.triacompany.academic.crossref.CrossrefValidation;
import com.triacompany.academic.crossref.CrossrefValidationRepository;
import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
import com.triacompany.academic.optimization.AcademicOptimizationReportResponse;
import com.triacompany.academic.optimization.AcademicOptimizationReportService;
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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionalDashboardService {

    private final ResearcherRepository researcherRepository;
    private final AcademicProfileRepository academicProfileRepository;
    private final OrcidWorkRepository orcidWorkRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;
    private final CrossrefValidationRepository crossrefValidationRepository;
    private final AcademicOptimizationReportService reportService;

    @Transactional(readOnly = true)
    public InstitutionalDashboardResponse generateDashboard() {
        List<Researcher> researchers = researcherRepository.findAll();

        List<AcademicProfile> profiles = academicProfileRepository.findAll();
        List<OpenAlexWork> openAlexWorks = openAlexWorkRepository.findAll();
        List<CrossrefValidation> crossrefValidations = crossrefValidationRepository.findAll();

        List<DashboardResearcherSummaryResponse> researcherSummaries = buildResearcherSummaries(researchers);

        int totalResearchers = researchers.size();
        int activeResearchers = countActiveResearchers(researchers);

        int totalProfiles = profiles.size();
        int completeProfiles = countCompleteProfiles(profiles);
        int incompleteProfiles = Math.max(0, totalProfiles - completeProfiles);

        int totalOrcidWorks = researchers.stream()
                .mapToInt(researcher -> orcidWorkRepository
                        .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcher.getId())
                        .size())
                .sum();

        int totalOpenAlexWorks = openAlexWorks.size();
        int confirmedOpenAlexWorks = countOpenAlexByStatus(openAlexWorks, PublicationReviewStatus.CONFIRMED);
        int pendingReviewOpenAlexWorks = countOpenAlexByStatus(openAlexWorks, PublicationReviewStatus.PENDING_REVIEW);
        int rejectedOpenAlexWorks = countOpenAlexByStatus(openAlexWorks, PublicationReviewStatus.REJECTED);

        int totalCrossrefValidations = crossrefValidations.size();
        int doiConfirmedCount = countCrossrefByStatus(crossrefValidations, CrossrefMatchStatus.DOI_CONFIRMED);
        int doiMissingCount = countCrossrefByStatus(crossrefValidations, CrossrefMatchStatus.DOI_MISSING);
        int doiNotFoundCount = countCrossrefByStatus(crossrefValidations, CrossrefMatchStatus.DOI_NOT_FOUND);

        int excellentResearchers = countByOverallStatus(researcherSummaries, "EXCELENTE");
        int goodResearchers = countByOverallStatus(researcherSummaries, "BOM");
        int optimizingResearchers = countByOverallStatus(researcherSummaries, "EM_OTIMIZACAO");
        int criticalResearchers = countByOverallStatus(researcherSummaries, "CRITICO");

        int averageScore = calculateAverageScore(researcherSummaries);
        String institutionalStatus = resolveInstitutionalStatus(averageScore, criticalResearchers, totalResearchers);

        List<DashboardMetricResponse> metrics = buildMetrics(
                totalResearchers,
                activeResearchers,
                totalProfiles,
                completeProfiles,
                incompleteProfiles,
                totalOrcidWorks,
                totalOpenAlexWorks,
                confirmedOpenAlexWorks,
                pendingReviewOpenAlexWorks,
                totalCrossrefValidations,
                doiConfirmedCount,
                criticalResearchers,
                averageScore
        );

        String institutionName = resolveMainInstitution(researchers);

        String executiveSummary = buildExecutiveSummary(
                institutionName,
                totalResearchers,
                averageScore,
                institutionalStatus,
                totalOrcidWorks,
                confirmedOpenAlexWorks,
                pendingReviewOpenAlexWorks,
                doiConfirmedCount,
                criticalResearchers
        );

        return new InstitutionalDashboardResponse(
                institutionName,

                totalResearchers,
                activeResearchers,

                totalProfiles,
                completeProfiles,
                incompleteProfiles,

                totalOrcidWorks,
                totalOpenAlexWorks,
                confirmedOpenAlexWorks,
                pendingReviewOpenAlexWorks,
                rejectedOpenAlexWorks,

                totalCrossrefValidations,
                doiConfirmedCount,
                doiMissingCount,
                doiNotFoundCount,

                excellentResearchers,
                goodResearchers,
                optimizingResearchers,
                criticalResearchers,

                averageScore,
                institutionalStatus,
                executiveSummary,

                metrics,
                researcherSummaries,

                LocalDateTime.now()
        );
    }

    private List<DashboardResearcherSummaryResponse> buildResearcherSummaries(List<Researcher> researchers) {
        List<DashboardResearcherSummaryResponse> summaries = new ArrayList<>();

        for (Researcher researcher : researchers) {
            AcademicOptimizationReportResponse report = reportService.generateReport(researcher.getId());

            summaries.add(new DashboardResearcherSummaryResponse(
                    report.researcherId(),
                    report.researcherName(),
                    report.institution(),
                    report.department(),
                    report.academicTitle(),
                    report.orcidId(),
                    report.profileCompletionPercentage(),
                    report.totalOrcidWorks(),
                    report.totalOpenAlexWorks(),
                    report.confirmedOpenAlexWorks(),
                    report.pendingReviewOpenAlexWorks(),
                    report.doiConfirmedCount(),
                    report.overallScore(),
                    report.overallStatus()
            ));
        }

        return summaries.stream()
                .sorted(
                        Comparator.comparing(DashboardResearcherSummaryResponse::overallScore)
                                .thenComparing(DashboardResearcherSummaryResponse::researcherName)
                )
                .toList();
    }

    private int countActiveResearchers(List<Researcher> researchers) {
        return Math.toIntExact(
                researchers.stream()
                        .filter(researcher -> Boolean.TRUE.equals(researcher.getActive()))
                        .count()
        );
    }

    private int countCompleteProfiles(List<AcademicProfile> profiles) {
        return Math.toIntExact(
                profiles.stream()
                        .filter(profile -> profile.getProfileCompletionPercentage() != null)
                        .filter(profile -> profile.getProfileCompletionPercentage() >= 70)
                        .count()
        );
    }

    private int countOpenAlexByStatus(List<OpenAlexWork> works, PublicationReviewStatus status) {
        return Math.toIntExact(
                works.stream()
                        .filter(work -> work.getReviewStatus() == status)
                        .count()
        );
    }

    private int countCrossrefByStatus(List<CrossrefValidation> validations, CrossrefMatchStatus status) {
        return Math.toIntExact(
                validations.stream()
                        .filter(validation -> validation.getMatchStatus() == status)
                        .count()
        );
    }

    private int countByOverallStatus(List<DashboardResearcherSummaryResponse> researchers, String status) {
        return Math.toIntExact(
                researchers.stream()
                        .filter(researcher -> status.equalsIgnoreCase(researcher.overallStatus()))
                        .count()
        );
    }

    private int calculateAverageScore(List<DashboardResearcherSummaryResponse> researchers) {
        if (researchers.isEmpty()) {
            return 0;
        }

        double average = researchers.stream()
                .mapToInt(DashboardResearcherSummaryResponse::overallScore)
                .average()
                .orElse(0);

        return (int) Math.round(average);
    }

    private String resolveInstitutionalStatus(
            int averageScore,
            int criticalResearchers,
            int totalResearchers
    ) {
        if (totalResearchers == 0) {
            return "SEM_DADOS";
        }

        if (averageScore >= 85 && criticalResearchers == 0) {
            return "EXCELENTE";
        }

        if (averageScore >= 70) {
            return "BOM";
        }

        if (averageScore >= 50) {
            return "EM_OTIMIZACAO";
        }

        return "CRITICO";
    }

    private List<DashboardMetricResponse> buildMetrics(
            int totalResearchers,
            int activeResearchers,
            int totalProfiles,
            int completeProfiles,
            int incompleteProfiles,
            int totalOrcidWorks,
            int totalOpenAlexWorks,
            int confirmedOpenAlexWorks,
            int pendingReviewOpenAlexWorks,
            int totalCrossrefValidations,
            int doiConfirmedCount,
            int criticalResearchers,
            int averageScore
    ) {
        List<DashboardMetricResponse> metrics = new ArrayList<>();

        metrics.add(new DashboardMetricResponse(
                "TOTAL_RESEARCHERS",
                "Pesquisadores cadastrados",
                totalResearchers,
                statusByMinimum(totalResearchers, 1),
                "Total de pesquisadores registrados na plataforma."
        ));

        metrics.add(new DashboardMetricResponse(
                "ACTIVE_RESEARCHERS",
                "Pesquisadores ativos",
                activeResearchers,
                statusByMinimum(activeResearchers, 1),
                "Total de pesquisadores ativos no cadastro institucional."
        ));

        metrics.add(new DashboardMetricResponse(
                "COMPLETE_PROFILES",
                "Perfis completos",
                completeProfiles,
                completeProfiles >= totalProfiles && totalProfiles > 0 ? "EXCELENTE" : "EM_OTIMIZACAO",
                "Perfis acadêmicos com completude igual ou superior a 70%."
        ));

        metrics.add(new DashboardMetricResponse(
                "INCOMPLETE_PROFILES",
                "Perfis incompletos",
                incompleteProfiles,
                incompleteProfiles == 0 ? "EXCELENTE" : "PRECISA_ATENCAO",
                "Perfis acadêmicos que precisam de complementação."
        ));

        metrics.add(new DashboardMetricResponse(
                "ORCID_WORKS",
                "Obras ORCID",
                totalOrcidWorks,
                statusByMinimum(totalOrcidWorks, 1),
                "Total de obras importadas do ORCID."
        ));

        metrics.add(new DashboardMetricResponse(
                "OPENALEX_WORKS",
                "Obras OpenAlex",
                totalOpenAlexWorks,
                statusByMinimum(totalOpenAlexWorks, 1),
                "Total de obras importadas do OpenAlex."
        ));

        metrics.add(new DashboardMetricResponse(
                "CONFIRMED_OPENALEX_WORKS",
                "Obras OpenAlex confirmadas",
                confirmedOpenAlexWorks,
                statusByMinimum(confirmedOpenAlexWorks, 1),
                "Obras confirmadas após revisão institucional."
        ));

        metrics.add(new DashboardMetricResponse(
                "PENDING_OPENALEX_WORKS",
                "Obras pendentes de revisão",
                pendingReviewOpenAlexWorks,
                pendingReviewOpenAlexWorks == 0 ? "EXCELENTE" : "PRECISA_ATENCAO",
                "Obras OpenAlex que ainda precisam de revisão manual."
        ));

        metrics.add(new DashboardMetricResponse(
                "CROSSREF_VALIDATIONS",
                "Validações Crossref",
                totalCrossrefValidations,
                statusByMinimum(totalCrossrefValidations, 1),
                "Total de validações de DOI e metadados realizadas no Crossref."
        ));

        metrics.add(new DashboardMetricResponse(
                "DOI_CONFIRMED",
                "DOIs confirmados",
                doiConfirmedCount,
                statusByMinimum(doiConfirmedCount, 1),
                "Quantidade de DOIs confirmados com metadados consistentes."
        ));

        metrics.add(new DashboardMetricResponse(
                "CRITICAL_RESEARCHERS",
                "Pesquisadores críticos",
                criticalResearchers,
                criticalResearchers == 0 ? "EXCELENTE" : "PRECISA_ATENCAO",
                "Pesquisadores com pontuação geral em estado crítico."
        ));

        metrics.add(new DashboardMetricResponse(
                "AVERAGE_SCORE",
                "Pontuação média institucional",
                averageScore,
                resolveScoreStatus(averageScore),
                "Pontuação média dos pesquisadores cadastrados."
        ));

        return metrics;
    }

    private String statusByMinimum(int value, int minimum) {
        return value >= minimum ? "OK" : "PRECISA_ATENCAO";
    }

    private String resolveScoreStatus(int score) {
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

    private String resolveMainInstitution(List<Researcher> researchers) {
        return researchers.stream()
                .map(Researcher::getInstitution)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse("Instituição não informada");
    }

    private String buildExecutiveSummary(
            String institutionName,
            int totalResearchers,
            int averageScore,
            String institutionalStatus,
            int totalOrcidWorks,
            int confirmedOpenAlexWorks,
            int pendingReviewOpenAlexWorks,
            int doiConfirmedCount,
            int criticalResearchers
    ) {
        return "A instituição " + institutionName
                + " possui " + totalResearchers + " pesquisador(es) cadastrado(s), com pontuação média institucional de "
                + averageScore + "/100 e status geral " + institutionalStatus + ". Foram identificadas "
                + totalOrcidWorks + " obra(s) no ORCID, "
                + confirmedOpenAlexWorks + " obra(s) OpenAlex confirmada(s), "
                + pendingReviewOpenAlexWorks + " obra(s) pendente(s) de revisão e "
                + doiConfirmedCount + " DOI(s) confirmado(s) no Crossref. Existem "
                + criticalResearchers + " pesquisador(es) em estado crítico, exigindo priorização na curadoria acadêmica.";
    }
}