package com.triacompany.academic.openalex;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/openalex")
@RequiredArgsConstructor
public class OpenAlexController {

    private final OpenAlexService openAlexService;

    @GetMapping("/researchers/{researcherId}/author")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexAuthorResponse findVerifiedAuthor(@PathVariable UUID researcherId) {
        return openAlexService.findVerifiedAuthor(researcherId);
    }

    @GetMapping("/researchers/{researcherId}/author-candidates")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<OpenAlexAuthorCandidateResponse> findAuthorCandidates(@PathVariable UUID researcherId) {
        return openAlexService.findAuthorCandidates(researcherId);
    }

    @GetMapping("/researchers/{researcherId}/search-works")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexImportResponse searchWorks(@PathVariable UUID researcherId) {
        return openAlexService.searchWorks(researcherId);
    }

    @PostMapping("/researchers/{researcherId}/import-works")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexImportResponse importWorks(@PathVariable UUID researcherId) {
        return openAlexService.importWorks(researcherId);
    }

    @PostMapping("/researchers/{researcherId}/import-works-by-author/{openAlexAuthorShortId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexImportResponse importWorksByApprovedAuthor(
            @PathVariable UUID researcherId,
            @PathVariable String openAlexAuthorShortId
    ) {
        return openAlexService.importWorksByApprovedAuthor(researcherId, openAlexAuthorShortId);
    }

    @GetMapping("/researchers/{researcherId}/works")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<OpenAlexWorkResponse> findWorksByResearcher(@PathVariable UUID researcherId) {
        return openAlexService.findWorksByResearcher(researcherId);
    }

    @GetMapping("/researchers/{researcherId}/works/status/{reviewStatus}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<OpenAlexWorkResponse> findWorksByResearcherAndStatus(
            @PathVariable UUID researcherId,
            @PathVariable PublicationReviewStatus reviewStatus
    ) {
        return openAlexService.findWorksByResearcherAndStatus(researcherId, reviewStatus);
    }

    @GetMapping("/researchers/{researcherId}/works/pending-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<OpenAlexWorkResponse> findPendingReviewWorks(@PathVariable UUID researcherId) {
        return openAlexService.findWorksByResearcherAndStatus(
                researcherId,
                PublicationReviewStatus.PENDING_REVIEW
        );
    }

    @PatchMapping("/works/{workId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexWorkResponse confirmWork(
            @PathVariable UUID workId,
            @Valid @RequestBody(required = false) OpenAlexWorkReviewRequest request
    ) {
        return openAlexService.confirmWork(workId, request);
    }

    @PatchMapping("/works/{workId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexWorkResponse rejectWork(
            @PathVariable UUID workId,
            @Valid @RequestBody(required = false) OpenAlexWorkReviewRequest request
    ) {
        return openAlexService.rejectWork(workId, request);
    }

    @PatchMapping("/works/{workId}/pending-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexWorkResponse markWorkAsPendingReview(
            @PathVariable UUID workId,
            @Valid @RequestBody(required = false) OpenAlexWorkReviewRequest request
    ) {
        return openAlexService.markWorkAsPendingReview(workId, request);
    }

    @DeleteMapping("/researchers/{researcherId}/works")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexCleanupResponse deleteWorksByResearcher(@PathVariable UUID researcherId) {
        return openAlexService.deleteWorksByResearcher(researcherId);
    }
}