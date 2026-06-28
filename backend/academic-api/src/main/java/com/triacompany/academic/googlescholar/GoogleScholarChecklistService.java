package com.triacompany.academic.googlescholar;

import com.triacompany.academic.optimization.AcademicOptimizationReportResponse;
import com.triacompany.academic.optimization.AcademicOptimizationReportService;
import com.triacompany.academic.profile.AcademicProfile;
import com.triacompany.academic.profile.AcademicProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleScholarChecklistService {

    private final AcademicOptimizationReportService reportService;
    private final AcademicProfileRepository academicProfileRepository;

    @Transactional(readOnly = true)
    public GoogleScholarChecklistResponse generateChecklist(UUID researcherId) {
        AcademicOptimizationReportResponse report = reportService.generateReport(researcherId);

        AcademicProfile profile = academicProfileRepository.findByResearcherId(researcherId)
                .orElse(null);

        String googleScholarUrl = profile != null ? normalize(profile.getGoogleScholarUrl()) : null;

        List<GoogleScholarChecklistItemResponse> items = buildChecklistItems(report, profile, googleScholarUrl);

        int completedItems = countByStatus(items, GoogleScholarChecklistStatus.COMPLETED);
        int needsReviewItems = countByStatus(items, GoogleScholarChecklistStatus.NEEDS_REVIEW);
        int missingItems = countByStatus(items, GoogleScholarChecklistStatus.MISSING);
        int totalItems = items.size();

        int checklistScore = calculateChecklistScore(completedItems, needsReviewItems, totalItems);
        String checklistStatus = resolveChecklistStatus(checklistScore, missingItems, needsReviewItems);

        String summary = buildSummary(report, checklistScore, checklistStatus, completedItems, needsReviewItems, missingItems, totalItems);

        return new GoogleScholarChecklistResponse(
                report.researcherId(),
                report.researcherName(),
                report.researcherEmail(),
                report.institution(),
                report.department(),
                report.academicTitle(),
                report.orcidId(),
                googleScholarUrl,

                report.profileCompletionPercentage(),
                report.totalOrcidWorks(),
                report.totalOpenAlexWorks(),
                report.confirmedOpenAlexWorks(),
                report.pendingReviewOpenAlexWorks(),
                report.totalCrossrefValidations(),
                report.doiConfirmedCount(),
                report.doiMissingCount(),

                completedItems,
                needsReviewItems,
                missingItems,
                totalItems,
                checklistScore,
                checklistStatus,

                summary,
                "Este checklist não automatiza, não acessa, não altera e não coleta dados diretamente do Google Acadêmico. As ações devem ser realizadas manualmente pelo pesquisador ou pela equipa institucional autorizada.",

                items,

                LocalDateTime.now()
        );
    }

    private List<GoogleScholarChecklistItemResponse> buildChecklistItems(
            AcademicOptimizationReportResponse report,
            AcademicProfile profile,
            String googleScholarUrl
    ) {
        List<GoogleScholarChecklistItemResponse> items = new ArrayList<>();

        items.add(buildGoogleScholarProfileLinkItem(googleScholarUrl));
        items.add(buildNameConsistencyItem(report));
        items.add(buildInstitutionConsistencyItem(report));
        items.add(buildOrcidVisibilityItem(report));
        items.add(buildResearchAreaItem(profile));
        items.add(buildKeywordsItem(profile));
        items.add(buildPublicationsReviewItem(report));
        items.add(buildDoiValidationItem(report));
        items.add(buildPendingOpenAlexReviewItem(report));
        items.add(buildManualDuplicatesReviewItem(report));
        items.add(buildPublicAccessItem(report));
        items.add(buildPeriodicMaintenanceItem(report));

        return items;
    }

    private GoogleScholarChecklistItemResponse buildGoogleScholarProfileLinkItem(String googleScholarUrl) {
        if (hasText(googleScholarUrl)) {
            return new GoogleScholarChecklistItemResponse(
                    "GOOGLE_SCHOLAR_PROFILE_LINK",
                    "Link do perfil Google Acadêmico",
                    "Verifica se o perfil acadêmico interno possui o link do Google Acadêmico informado.",
                    GoogleScholarChecklistStatus.COMPLETED,
                    "HIGH",
                    "Abrir manualmente o link cadastrado e confirmar se pertence ao pesquisador correto."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "GOOGLE_SCHOLAR_PROFILE_LINK",
                "Link do perfil Google Acadêmico",
                "O perfil acadêmico interno ainda não possui link do Google Acadêmico.",
                GoogleScholarChecklistStatus.MISSING,
                "HIGH",
                "Solicitar ao pesquisador o link público do Google Acadêmico e cadastrar no perfil acadêmico institucional."
        );
    }

    private GoogleScholarChecklistItemResponse buildNameConsistencyItem(AcademicOptimizationReportResponse report) {
        if (hasText(report.researcherName())) {
            return new GoogleScholarChecklistItemResponse(
                    "NAME_CONSISTENCY",
                    "Consistência do nome acadêmico",
                    "Verifica se existe nome institucional para comparação manual com o Google Acadêmico.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "HIGH",
                    "Comparar manualmente o nome no Google Acadêmico com o nome institucional: " + report.researcherName() + ". Verificar variações, abreviações e acentos."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "NAME_CONSISTENCY",
                "Consistência do nome acadêmico",
                "O pesquisador não possui nome institucional completo cadastrado.",
                GoogleScholarChecklistStatus.MISSING,
                "HIGH",
                "Atualizar o cadastro institucional com o nome completo do pesquisador antes da revisão do Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildInstitutionConsistencyItem(AcademicOptimizationReportResponse report) {
        if (hasText(report.institution()) && hasText(report.department())) {
            return new GoogleScholarChecklistItemResponse(
                    "INSTITUTION_CONSISTENCY",
                    "Consistência institucional",
                    "Verifica se instituição e departamento estão cadastrados para revisão manual no Google Acadêmico.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "HIGH",
                    "Confirmar manualmente se o perfil Google Acadêmico apresenta vínculo com " + report.institution() + " e departamento/área compatível com " + report.department() + "."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "INSTITUTION_CONSISTENCY",
                "Consistência institucional",
                "Instituição ou departamento incompleto no cadastro interno.",
                GoogleScholarChecklistStatus.MISSING,
                "HIGH",
                "Atualizar instituição e departamento no cadastro interno antes de orientar a revisão do Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildOrcidVisibilityItem(AcademicOptimizationReportResponse report) {
        if (hasText(report.orcidId()) && report.totalOrcidWorks() > 0) {
            return new GoogleScholarChecklistItemResponse(
                    "ORCID_VISIBILITY",
                    "ORCID e produção científica",
                    "O pesquisador possui ORCID e obras importadas, permitindo comparação manual com o Google Acadêmico.",
                    GoogleScholarChecklistStatus.COMPLETED,
                    "MEDIUM",
                    "Comparar manualmente se as principais obras do ORCID também aparecem no Google Acadêmico."
            );
        }

        if (hasText(report.orcidId())) {
            return new GoogleScholarChecklistItemResponse(
                    "ORCID_VISIBILITY",
                    "ORCID e produção científica",
                    "O pesquisador possui ORCID, mas não há obras públicas importadas.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "HIGH",
                    "Orientar o pesquisador a atualizar o ORCID ou tornar as obras públicas, depois comparar manualmente com o Google Acadêmico."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "ORCID_VISIBILITY",
                "ORCID e produção científica",
                "O pesquisador não possui ORCID informado.",
                GoogleScholarChecklistStatus.MISSING,
                "HIGH",
                "Cadastrar ORCID do pesquisador e usar esse identificador como referência institucional para curadoria acadêmica."
        );
    }

    private GoogleScholarChecklistItemResponse buildResearchAreaItem(AcademicProfile profile) {
        if (profile != null && hasText(profile.getResearchArea())) {
            return new GoogleScholarChecklistItemResponse(
                    "RESEARCH_AREA",
                    "Área de pesquisa",
                    "A área de pesquisa está cadastrada no perfil acadêmico interno.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "MEDIUM",
                    "Comparar manualmente se a área de atuação no Google Acadêmico é compatível com: " + profile.getResearchArea() + "."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "RESEARCH_AREA",
                "Área de pesquisa",
                "A área de pesquisa não está cadastrada ou está incompleta.",
                GoogleScholarChecklistStatus.MISSING,
                "MEDIUM",
                "Completar a área de pesquisa no perfil acadêmico e orientar o pesquisador a refletir essa área no Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildKeywordsItem(AcademicProfile profile) {
        if (profile != null && hasText(profile.getKeywords())) {
            return new GoogleScholarChecklistItemResponse(
                    "KEYWORDS",
                    "Palavras-chave acadêmicas",
                    "As palavras-chave estão cadastradas no perfil acadêmico interno.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "MEDIUM",
                    "Comparar manualmente se os interesses de pesquisa no Google Acadêmico refletem estas palavras-chave: " + profile.getKeywords() + "."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "KEYWORDS",
                "Palavras-chave acadêmicas",
                "O perfil acadêmico ainda não possui palavras-chave cadastradas.",
                GoogleScholarChecklistStatus.MISSING,
                "MEDIUM",
                "Cadastrar palavras-chave acadêmicas e orientar o pesquisador a revisar os interesses de pesquisa no Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildPublicationsReviewItem(AcademicOptimizationReportResponse report) {
        if (report.confirmedOpenAlexWorks() > 0) {
            return new GoogleScholarChecklistItemResponse(
                    "PUBLICATIONS_REVIEW",
                    "Revisão manual das publicações",
                    "Existem obras confirmadas institucionalmente no OpenAlex para comparação manual.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "HIGH",
                    "Comparar manualmente as " + report.confirmedOpenAlexWorks() + " obra(s) confirmada(s) no OpenAlex com as publicações exibidas no Google Acadêmico."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "PUBLICATIONS_REVIEW",
                "Revisão manual das publicações",
                "Ainda não há obras OpenAlex confirmadas para servir como referência segura.",
                GoogleScholarChecklistStatus.MISSING,
                "HIGH",
                "Confirmar obras importadas do OpenAlex antes de revisar o Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildDoiValidationItem(AcademicOptimizationReportResponse report) {
        if (report.doiConfirmedCount() > 0) {
            return new GoogleScholarChecklistItemResponse(
                    "DOI_VALIDATION",
                    "Validação de DOI",
                    "Existem DOIs confirmados no Crossref para apoiar a curadoria manual.",
                    GoogleScholarChecklistStatus.COMPLETED,
                    "HIGH",
                    "Usar os " + report.doiConfirmedCount() + " DOI(s) confirmado(s) como referência para validar publicações no Google Acadêmico."
            );
        }

        if (report.totalCrossrefValidations() > 0) {
            return new GoogleScholarChecklistItemResponse(
                    "DOI_VALIDATION",
                    "Validação de DOI",
                    "Há validações Crossref, mas nenhuma com DOI confirmado.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "HIGH",
                    "Investigar manualmente obras sem DOI confirmado antes de orientar ajustes no Google Acadêmico."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "DOI_VALIDATION",
                "Validação de DOI",
                "Ainda não existem validações Crossref para este pesquisador.",
                GoogleScholarChecklistStatus.MISSING,
                "HIGH",
                "Validar as obras confirmadas no Crossref antes da revisão final do Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildPendingOpenAlexReviewItem(AcademicOptimizationReportResponse report) {
        if (report.pendingReviewOpenAlexWorks() == 0) {
            return new GoogleScholarChecklistItemResponse(
                    "PENDING_OPENALEX_REVIEW",
                    "Pendências OpenAlex",
                    "Não existem obras OpenAlex pendentes de revisão.",
                    GoogleScholarChecklistStatus.COMPLETED,
                    "MEDIUM",
                    "Manter revisão periódica para novas obras importadas."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "PENDING_OPENALEX_REVIEW",
                "Pendências OpenAlex",
                "Existem obras OpenAlex pendentes de revisão manual.",
                GoogleScholarChecklistStatus.NEEDS_REVIEW,
                "HIGH",
                "Revisar " + report.pendingReviewOpenAlexWorks() + " obra(s) pendente(s) antes de orientar correções no Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildManualDuplicatesReviewItem(AcademicOptimizationReportResponse report) {
        if (report.confirmedOpenAlexWorks() > 1 || report.totalOrcidWorks() > 1) {
            return new GoogleScholarChecklistItemResponse(
                    "DUPLICATES_REVIEW",
                    "Revisão de duplicidades",
                    "Existe produção científica suficiente para exigir revisão de possíveis duplicidades.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "MEDIUM",
                    "Verificar manualmente se o Google Acadêmico possui publicações duplicadas, versões repetidas ou obras atribuídas incorretamente."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "DUPLICATES_REVIEW",
                "Revisão de duplicidades",
                "Poucas obras identificadas até ao momento.",
                GoogleScholarChecklistStatus.NOT_APPLICABLE,
                "LOW",
                "Reavaliar duplicidades quando houver mais obras confirmadas."
        );
    }

    private GoogleScholarChecklistItemResponse buildPublicAccessItem(AcademicOptimizationReportResponse report) {
        if (report.confirmedOpenAlexWorks() > 0 || report.totalOrcidWorks() > 0) {
            return new GoogleScholarChecklistItemResponse(
                    "PUBLIC_ACCESS",
                    "Visibilidade pública das publicações",
                    "Há publicações identificadas em bases acadêmicas externas.",
                    GoogleScholarChecklistStatus.NEEDS_REVIEW,
                    "MEDIUM",
                    "Verificar manualmente se as principais publicações possuem links públicos, DOI ou páginas editoriais acessíveis a partir do Google Acadêmico."
            );
        }

        return new GoogleScholarChecklistItemResponse(
                "PUBLIC_ACCESS",
                "Visibilidade pública das publicações",
                "Nenhuma publicação confirmada foi identificada.",
                GoogleScholarChecklistStatus.MISSING,
                "MEDIUM",
                "Confirmar publicações em ORCID, OpenAlex ou Crossref antes de revisar visibilidade no Google Acadêmico."
        );
    }

    private GoogleScholarChecklistItemResponse buildPeriodicMaintenanceItem(AcademicOptimizationReportResponse report) {
        return new GoogleScholarChecklistItemResponse(
                "PERIODIC_MAINTENANCE",
                "Manutenção periódica",
                "O Google Acadêmico deve ser revisto periodicamente porque novas citações e publicações podem aparecer com o tempo.",
                GoogleScholarChecklistStatus.NEEDS_REVIEW,
                "LOW",
                "Definir rotina institucional de revisão manual mensal ou trimestral do perfil Google Acadêmico do pesquisador."
        );
    }

    private int calculateChecklistScore(int completedItems, int needsReviewItems, int totalItems) {
        if (totalItems == 0) {
            return 0;
        }

        double score = ((completedItems * 100.0) + (needsReviewItems * 50.0)) / totalItems;

        return (int) Math.round(score);
    }

    private String resolveChecklistStatus(int checklistScore, int missingItems, int needsReviewItems) {
        if (checklistScore >= 85 && missingItems == 0) {
            return "PRONTO_PARA_REVISAO_FINAL";
        }

        if (checklistScore >= 65) {
            return "EM_CURADORIA";
        }

        if (checklistScore >= 40) {
            return "PRECISA_COMPLETAR_DADOS";
        }

        return "CRITICO";
    }

    private String buildSummary(
            AcademicOptimizationReportResponse report,
            int checklistScore,
            String checklistStatus,
            int completedItems,
            int needsReviewItems,
            int missingItems,
            int totalItems
    ) {
        return "O checklist Google Acadêmico do pesquisador "
                + report.researcherName()
                + " possui pontuação de "
                + checklistScore
                + "/100, com status "
                + checklistStatus
                + ". Foram avaliados "
                + totalItems
                + " item(ns), sendo "
                + completedItems
                + " concluído(s), "
                + needsReviewItems
                + " em revisão e "
                + missingItems
                + " ausente(s). A revisão deve ser feita manualmente pelo pesquisador ou pela equipa institucional autorizada.";
    }

    private int countByStatus(List<GoogleScholarChecklistItemResponse> items, GoogleScholarChecklistStatus status) {
        return Math.toIntExact(
                items.stream()
                        .filter(item -> item.status() == status)
                        .count()
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}