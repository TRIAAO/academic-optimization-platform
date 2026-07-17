package com.triacompany.academic.editorial;

import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditorialDecisionService {

    private final EditorialDecisionRepository editorialDecisionRepository;
    private final ResearcherRepository researcherRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;

    @Transactional(readOnly = true)
    public Optional<EditorialDecisionResponse> findByWork(UUID researcherId, UUID workId) {
        requireEligibleWork(researcherId, workId);
        return editorialDecisionRepository.findByResearcherIdAndOpenAlexWorkId(researcherId, workId)
                .map(EditorialDecisionResponse::fromEntity);
    }

    @Transactional
    public EditorialDecisionResponse save(
            UUID researcherId,
            UUID workId,
            SaveEditorialDecisionRequest request,
            String actorEmail
    ) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
        OpenAlexWork work = requireEligibleWork(researcherId, workId);
        String officialUrl = normalizeOfficialUrl(request.officialUrl());
        validateGovernanceRules(request, officialUrl);

        EditorialDecision decision = editorialDecisionRepository
                .findByResearcherIdAndOpenAlexWorkId(researcherId, workId)
                .orElseGet(() -> EditorialDecision.builder()
                        .researcher(researcher)
                        .openAlexWork(work)
                        .build());

        decision.setJournalName(request.journalName().trim());
        decision.setPublisher(normalizeNullable(request.publisher()));
        decision.setIssns(normalizeIssns(request.issns()));
        decision.setRelevanceScore(request.relevanceScore());
        decision.setOfficialUrl(officialUrl);
        decision.setStatus(request.status());
        decision.setScopeConfirmed(request.scopeConfirmed());
        decision.setPeerReviewConfirmed(request.peerReviewConfirmed());
        decision.setIndexingConfirmed(request.indexingConfirmed());
        decision.setFeesConfirmed(request.feesConfirmed());
        decision.setLanguageConfirmed(request.languageConfirmed());
        decision.setDeadlinesConfirmed(request.deadlinesConfirmed());
        decision.setNotes(normalizeNullable(request.notes()));
        decision.setReviewedBy(normalizeActor(actorEmail));

        EditorialDecision saved = editorialDecisionRepository.save(decision);
        return EditorialDecisionResponse.fromEntity(saved);
    }

    private OpenAlexWork requireEligibleWork(UUID researcherId, UUID workId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }

        OpenAlexWork work = openAlexWorkRepository.findById(workId)
                .orElseThrow(() -> new IllegalArgumentException("Obra OpenAlex não encontrada."));

        if (work.getResearcher() == null || !researcherId.equals(work.getResearcher().getId())) {
            throw new IllegalArgumentException("A obra selecionada não pertence ao pesquisador informado.");
        }
        if (!PublicationReviewStatus.CONFIRMED.equals(work.getReviewStatus())) {
            throw new IllegalArgumentException("A obra precisa estar confirmada antes da decisão editorial.");
        }
        return work;
    }

    private void validateGovernanceRules(SaveEditorialDecisionRequest request, String officialUrl) {
        if (EditorialDecisionStatus.REJECTED.equals(request.status())
                && normalizeNullable(request.notes()) == null) {
            throw new IllegalArgumentException("Informe o motivo ao rejeitar um periódico candidato.");
        }

        if (!requiresCompleteValidation(request.status())) {
            return;
        }

        if (officialUrl == null) {
            throw new IllegalArgumentException(
                    "Informe a URL oficial do periódico antes de aprovar ou planejar a submissão."
            );
        }
        if (!allCriteriaConfirmed(request)) {
            throw new IllegalArgumentException(
                    "Confirme os seis critérios editoriais antes de aprovar ou planejar a submissão."
            );
        }
    }

    private boolean requiresCompleteValidation(EditorialDecisionStatus status) {
        return EditorialDecisionStatus.APPROVED.equals(status)
                || EditorialDecisionStatus.SUBMISSION_PLANNED.equals(status)
                || EditorialDecisionStatus.SUBMITTED.equals(status);
    }

    private boolean allCriteriaConfirmed(SaveEditorialDecisionRequest request) {
        return request.scopeConfirmed()
                && request.peerReviewConfirmed()
                && request.indexingConfirmed()
                && request.feesConfirmed()
                && request.languageConfirmed()
                && request.deadlinesConfirmed();
    }

    private String normalizeOfficialUrl(String value) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            return null;
        }

        try {
            URI uri = URI.create(normalized);
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException("Informe uma URL oficial válida iniciada por http:// ou https://.");
            }
            return normalized;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Informe uma URL oficial válida iniciada por http:// ou https://.");
        }
    }

    private String normalizeIssns(List<String> values) {
        if (values == null) {
            return null;
        }
        String joined = values.stream()
                .map(this::normalizeNullable)
                .filter(value -> value != null)
                .distinct()
                .limit(10)
                .reduce((left, right) -> left + "," + right)
                .orElse(null);
        return normalizeNullable(joined);
    }

    private String normalizeActor(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? "Usuário autenticado" : normalized;
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
