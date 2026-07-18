package com.triacompany.academic.deduplication;

import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.orcid.OrcidWork;
import com.triacompany.academic.orcid.OrcidWorkRepository;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BibliographicDeduplicationService {

    private static final String ORCID = "ORCID";
    private static final String OPENALEX = "OPENALEX";

    private final BibliographicDuplicateReviewRepository duplicateReviewRepository;
    private final BibliographicSimilarityEngine similarityEngine;
    private final ResearcherRepository researcherRepository;
    private final OrcidWorkRepository orcidWorkRepository;
    private final OpenAlexWorkRepository openAlexWorkRepository;

    @Transactional
    public BibliographicDeduplicationResponse scan(UUID researcherId) {
        Researcher researcher = requireResearcher(researcherId);
        List<OrcidWork> orcidWorks = orcidWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);
        List<OpenAlexWork> openAlexWorks = openAlexWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);

        for (OrcidWork orcidWork : orcidWorks) {
            for (OpenAlexWork openAlexWork : openAlexWorks) {
                BibliographicSimilarityEngine.Result result = similarityEngine.compare(
                        orcidWork.getTitle(),
                        orcidWork.getDoi(),
                        orcidWork.getPublicationYear(),
                        openAlexWork.getTitle(),
                        openAlexWork.getDoi(),
                        openAlexWork.getPublicationYear()
                );

                if (!result.candidate()) {
                    continue;
                }

                BibliographicDuplicateReview review = duplicateReviewRepository
                        .findByResearcherIdAndLeftSourceAndLeftWorkIdAndRightSourceAndRightWorkId(
                                researcherId,
                                ORCID,
                                orcidWork.getId(),
                                OPENALEX,
                                openAlexWork.getId()
                        )
                        .orElseGet(() -> BibliographicDuplicateReview.builder()
                                .researcher(researcher)
                                .leftSource(ORCID)
                                .leftWorkId(orcidWork.getId())
                                .rightSource(OPENALEX)
                                .rightWorkId(openAlexWork.getId())
                                .reviewStatus(DuplicateReviewStatus.PENDING)
                                .build());

                review.setSimilarityScore(result.similarityScore());
                review.setTitleSimilarity(result.titleSimilarity());
                review.setDoiExactMatch(result.doiExactMatch());
                review.setPublicationYearCompatible(result.publicationYearCompatible());
                review.setRationale(result.rationale());
                duplicateReviewRepository.save(review);
            }
        }

        return buildResponse(researcherId, orcidWorks, openAlexWorks);
    }

    @Transactional(readOnly = true)
    public BibliographicDeduplicationResponse findByResearcher(UUID researcherId) {
        requireResearcher(researcherId);
        List<OrcidWork> orcidWorks = orcidWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);
        List<OpenAlexWork> openAlexWorks = openAlexWorkRepository
                .findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId);
        return buildResponse(researcherId, orcidWorks, openAlexWorks);
    }

    @Transactional
    public BibliographicDuplicateCandidateResponse review(
            UUID researcherId,
            UUID candidateId,
            ReviewBibliographicDuplicateRequest request,
            String actorEmail
    ) {
        requireResearcher(researcherId);
        validateDecision(request);

        BibliographicDuplicateReview review = duplicateReviewRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidato de duplicidade não encontrado."));

        if (review.getResearcher() == null
                || !researcherId.equals(review.getResearcher().getId())) {
            throw new IllegalArgumentException("O candidato não pertence ao pesquisador informado.");
        }

        review.setReviewStatus(request.status());
        review.setReviewerNote(normalizeNullable(request.note()));
        review.setReviewedBy(normalizeActor(actorEmail));
        review.setReviewedAt(LocalDateTime.now());

        BibliographicDuplicateReview saved = duplicateReviewRepository.save(review);
        return toCandidateResponse(saved, loadOrcidWorks(researcherId), loadOpenAlexWorks(researcherId));
    }

    private BibliographicDeduplicationResponse buildResponse(
            UUID researcherId,
            List<OrcidWork> orcidWorks,
            List<OpenAlexWork> openAlexWorks
    ) {
        Map<UUID, OrcidWork> orcidById = orcidWorks.stream()
                .collect(Collectors.toMap(OrcidWork::getId, Function.identity()));
        Map<UUID, OpenAlexWork> openAlexById = openAlexWorks.stream()
                .collect(Collectors.toMap(OpenAlexWork::getId, Function.identity()));

        List<BibliographicDuplicateCandidateResponse> candidates = duplicateReviewRepository
                .findByResearcherIdOrderBySimilarityScoreDescCreatedAtDesc(researcherId)
                .stream()
                .filter(review -> orcidById.containsKey(review.getLeftWorkId()))
                .filter(review -> openAlexById.containsKey(review.getRightWorkId()))
                .map(review -> toCandidateResponse(review, orcidById, openAlexById))
                .toList();

        int pending = (int) candidates.stream()
                .filter(candidate -> DuplicateReviewStatus.PENDING.equals(candidate.reviewStatus()))
                .count();
        int confirmed = (int) candidates.stream()
                .filter(candidate -> DuplicateReviewStatus.CONFIRMED.equals(candidate.reviewStatus()))
                .count();
        int rejected = (int) candidates.stream()
                .filter(candidate -> DuplicateReviewStatus.REJECTED.equals(candidate.reviewStatus()))
                .count();

        return new BibliographicDeduplicationResponse(
                researcherId,
                candidates.size(),
                pending,
                confirmed,
                rejected,
                candidates
        );
    }

    private BibliographicDuplicateCandidateResponse toCandidateResponse(
            BibliographicDuplicateReview review,
            Map<UUID, OrcidWork> orcidById,
            Map<UUID, OpenAlexWork> openAlexById
    ) {
        OrcidWork orcidWork = orcidById.get(review.getLeftWorkId());
        OpenAlexWork openAlexWork = openAlexById.get(review.getRightWorkId());

        if (orcidWork == null || openAlexWork == null) {
            throw new IllegalArgumentException("Uma das obras relacionadas ao candidato não foi encontrada.");
        }

        return new BibliographicDuplicateCandidateResponse(
                review.getId(),
                review.getResearcher().getId(),
                toWorkResponse(orcidWork),
                toWorkResponse(openAlexWork),
                review.getSimilarityScore(),
                review.getTitleSimilarity(),
                review.getDoiExactMatch(),
                review.getPublicationYearCompatible(),
                review.getRationale(),
                review.getReviewStatus(),
                review.getReviewerNote(),
                review.getReviewedBy(),
                review.getReviewedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private BibliographicWorkResponse toWorkResponse(OrcidWork work) {
        return new BibliographicWorkResponse(
                ORCID,
                work.getId(),
                work.getPutCode(),
                work.getTitle(),
                work.getDoi(),
                work.getPublicationYear(),
                work.getJournalTitle(),
                null
        );
    }

    private BibliographicWorkResponse toWorkResponse(OpenAlexWork work) {
        return new BibliographicWorkResponse(
                OPENALEX,
                work.getId(),
                work.getOpenAlexId(),
                work.getTitle(),
                work.getDoi(),
                work.getPublicationYear(),
                work.getSourceName(),
                work.getCitedByCount()
        );
    }

    private Map<UUID, OrcidWork> loadOrcidWorks(UUID researcherId) {
        return orcidWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId)
                .stream()
                .collect(Collectors.toMap(OrcidWork::getId, Function.identity()));
    }

    private Map<UUID, OpenAlexWork> loadOpenAlexWorks(UUID researcherId) {
        return openAlexWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId)
                .stream()
                .collect(Collectors.toMap(OpenAlexWork::getId, Function.identity()));
    }

    private Researcher requireResearcher(UUID researcherId) {
        return researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
    }

    private void validateDecision(ReviewBibliographicDuplicateRequest request) {
        if (DuplicateReviewStatus.PENDING.equals(request.status())) {
            throw new IllegalArgumentException("A decisão deve confirmar ou rejeitar o candidato.");
        }
        if (DuplicateReviewStatus.REJECTED.equals(request.status())
                && normalizeNullable(request.note()) == null) {
            throw new IllegalArgumentException("Informe o motivo ao rejeitar um candidato de duplicidade.");
        }
    }

    private String normalizeActor(String value) {
        String normalized = normalizeNullable(value);
        return normalized == null ? "Usuário autenticado" : normalized;
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
