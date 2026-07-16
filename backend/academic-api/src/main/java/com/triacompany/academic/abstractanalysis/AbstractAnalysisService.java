package com.triacompany.academic.abstractanalysis;

import com.triacompany.academic.abstractanalysis.AbstractAnalysisEngine.AbstractEvidence;
import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
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
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AbstractAnalysisService {

    private static final String METHODOLOGY = "Análise determinística das obras confirmadas na revisão manual. "
            + "A cobertura considera abstracts recuperados do OpenAlex e versões PT–EN registadas manualmente; "
            + "os temas resultam de expressões recorrentes de duas ou três palavras presentes nos abstracts.";
    private static final String TRANSLATION_POLICY = "As traduções PT–EN são inseridas e revistas manualmente. "
            + "A plataforma não usa tradução automática nem altera o abstract original fornecido pelo OpenAlex.";

    private final ResearcherRepository researcherRepository;
    private final AcademicProfileRepository academicProfileRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;
    private final AbstractAnalysisEngine engine = new AbstractAnalysisEngine();

    @Transactional(readOnly = true)
    public AbstractAnalysisResponse analyze(UUID researcherId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
        AcademicProfile profile = academicProfileRepository.findByResearcherId(researcherId).orElse(null);
        List<OpenAlexWork> allWorks = openAlexWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);
        List<OpenAlexWork> confirmedWorks = allWorks.stream()
                .filter(work -> PublicationReviewStatus.CONFIRMED.equals(work.getReviewStatus()))
                .toList();
        List<AbstractWorkAnalysisResponse> workResponses = confirmedWorks.stream()
                .map(this::toWorkResponse)
                .toList();

        int worksWithAbstract = Math.toIntExact(workResponses.stream()
                .filter(AbstractWorkAnalysisResponse::hasOriginalAbstract)
                .count());
        int worksWithPortuguese = Math.toIntExact(workResponses.stream()
                .filter(AbstractWorkAnalysisResponse::hasPortugueseVersion)
                .count());
        int worksWithEnglish = Math.toIntExact(workResponses.stream()
                .filter(AbstractWorkAnalysisResponse::hasEnglishVersion)
                .count());
        int unclassifiedLanguageAbstracts = Math.toIntExact(workResponses.stream()
                .filter(AbstractWorkAnalysisResponse::hasOriginalAbstract)
                .filter(work -> !isPtEnLanguage(work.originalLanguage()))
                .count());
        int confirmedTotal = confirmedWorks.size();
        int abstractCoverage = percentage(worksWithAbstract, confirmedTotal);

        List<AbstractEvidence> evidence = confirmedWorks.stream()
                .map(work -> new AbstractEvidence(
                        work.getId(),
                        firstText(work.getAbstractText(), work.getAbstractPt(), work.getAbstractEn())
                ))
                .filter(item -> hasText(item.text()))
                .toList();

        return new AbstractAnalysisResponse(
                researcher.getId(),
                researcher.getFullName(),
                allWorks.size(),
                confirmedTotal,
                worksWithAbstract,
                Math.max(0, confirmedTotal - worksWithAbstract),
                abstractCoverage,
                percentage(worksWithPortuguese, confirmedTotal),
                percentage(worksWithEnglish, confirmedTotal),
                unclassifiedLanguageAbstracts,
                evidenceLevel(confirmedTotal, abstractCoverage),
                engine.extractThemes(
                        evidence,
                        profile != null ? profile.getResearchArea() : null,
                        profile != null ? profile.getKeywords() : null,
                        researcher.getFullName()
                ),
                workResponses,
                nextActions(
                        allWorks.size(),
                        confirmedTotal,
                        worksWithAbstract,
                        worksWithPortuguese,
                        worksWithEnglish
                ),
                METHODOLOGY,
                TRANSLATION_POLICY,
                LocalDateTime.now()
        );
    }

    @Transactional
    public AbstractWorkAnalysisResponse updateTranslations(UUID workId, AbstractTranslationRequest request) {
        OpenAlexWork work = openAlexWorkRepository.findById(workId)
                .orElseThrow(() -> new IllegalArgumentException("Obra OpenAlex não encontrada."));

        if (!PublicationReviewStatus.CONFIRMED.equals(work.getReviewStatus())) {
            throw new IllegalArgumentException(
                    "Confirme a autoria da obra na revisão manual antes de registar traduções."
            );
        }

        if (!hasText(work.getAbstractText())) {
            throw new IllegalArgumentException(
                    "Esta obra ainda não possui abstract original. Sincronize os dados do OpenAlex primeiro."
            );
        }

        String abstractPt = normalizeNullable(request != null ? request.abstractPt() : null);
        String abstractEn = normalizeNullable(request != null ? request.abstractEn() : null);

        if (!Objects.equals(work.getAbstractPt(), abstractPt)
                || !Objects.equals(work.getAbstractEn(), abstractEn)) {
            work.setAbstractPt(abstractPt);
            work.setAbstractEn(abstractEn);
            work.setAbstractTranslationsUpdatedAt(LocalDateTime.now());
            work = openAlexWorkRepository.save(work);
        }

        return toWorkResponse(work);
    }

    private AbstractWorkAnalysisResponse toWorkResponse(OpenAlexWork work) {
        String language = normalizeLanguage(work.getAbstractLanguage());
        boolean hasOriginal = hasText(work.getAbstractText());
        boolean hasPortuguese = hasText(work.getAbstractPt()) || (hasOriginal && "pt".equals(language));
        boolean hasEnglish = hasText(work.getAbstractEn()) || (hasOriginal && "en".equals(language));

        return new AbstractWorkAnalysisResponse(
                work.getId(),
                work.getTitle(),
                work.getPublicationYear(),
                work.getSourceName(),
                work.getOpenAlexUrl(),
                language,
                work.getAbstractText(),
                work.getAbstractPt(),
                work.getAbstractEn(),
                hasOriginal,
                hasPortuguese,
                hasEnglish,
                translationStatus(hasOriginal, hasPortuguese, hasEnglish),
                work.getAbstractTranslationsUpdatedAt()
        );
    }

    private List<AbstractAnalysisActionResponse> nextActions(
            int totalOpenAlexWorks,
            int confirmedWorks,
            int worksWithAbstract,
            int worksWithPortuguese,
            int worksWithEnglish
    ) {
        List<AbstractAnalysisActionResponse> actions = new ArrayList<>();

        if (totalOpenAlexWorks == 0) {
            actions.add(new AbstractAnalysisActionResponse(
                    1,
                    "Importar obras do OpenAlex",
                    "Busque o autor, aprove o candidato correto e importe as obras antes de sincronizar abstracts.",
                    "OPENALEX"
            ));
            return actions;
        }

        if (confirmedWorks == 0) {
            actions.add(new AbstractAnalysisActionResponse(
                    1,
                    "Confirmar obras na revisão manual",
                    "A análise considera apenas publicações cuja autoria foi confirmada institucionalmente.",
                    "MANUAL_REVIEW"
            ));
            return actions;
        }

        if (worksWithAbstract < confirmedWorks) {
            actions.add(new AbstractAnalysisActionResponse(
                    actions.size() + 1,
                    "Sincronizar abstracts do OpenAlex",
                    "Recupere os abstracts disponíveis para as obras já importadas e confirmadas.",
                    "OPENALEX"
            ));
        }

        if (worksWithPortuguese < confirmedWorks) {
            actions.add(new AbstractAnalysisActionResponse(
                    actions.size() + 1,
                    "Completar cobertura em português",
                    "Registe manualmente a versão PT dos abstracts que ainda não possuem esta cobertura.",
                    "ABSTRACT_ANALYSIS"
            ));
        }

        if (worksWithEnglish < confirmedWorks) {
            actions.add(new AbstractAnalysisActionResponse(
                    actions.size() + 1,
                    "Completar cobertura em inglês",
                    "Registe manualmente a versão EN dos abstracts que ainda não possuem esta cobertura.",
                    "ABSTRACT_ANALYSIS"
            ));
        }

        if (actions.isEmpty()) {
            actions.add(new AbstractAnalysisActionResponse(
                    1,
                    "Revisar periodicamente a cobertura",
                    "A cobertura PT–EN está completa; mantenha a revisão humana após novas importações.",
                    "ABSTRACT_ANALYSIS"
            ));
        }

        return actions;
    }

    private String translationStatus(boolean original, boolean portuguese, boolean english) {
        if (!original) {
            return "NO_ABSTRACT";
        }
        if (portuguese && english) {
            return "COMPLETE";
        }
        if (!portuguese && !english) {
            return "PT_AND_EN_MISSING";
        }
        return portuguese ? "EN_MISSING" : "PT_MISSING";
    }

    private String evidenceLevel(int confirmedWorks, int coverage) {
        if (confirmedWorks == 0 || coverage == 0) {
            return "SEM_EVIDÊNCIA";
        }
        if (coverage >= 80) {
            return "CONSOLIDADA";
        }
        return coverage >= 50 ? "MODERADA" : "INICIAL";
    }

    private int percentage(int value, int total) {
        return total == 0 ? 0 : (int) Math.round(value * 100.0 / total);
    }

    private String normalizeLanguage(String value) {
        return hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
    }

    private boolean isPtEnLanguage(String language) {
        return "pt".equals(language) || "en".equals(language);
    }

    private String normalizeNullable(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
