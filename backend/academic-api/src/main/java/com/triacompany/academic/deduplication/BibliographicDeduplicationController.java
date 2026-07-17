package com.triacompany.academic.deduplication;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bibliographic-deduplication")
@RequiredArgsConstructor
public class BibliographicDeduplicationController {

    private final BibliographicDeduplicationService deduplicationService;

    @GetMapping("/researchers/{researcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public BibliographicDeduplicationResponse findByResearcher(
            @PathVariable UUID researcherId
    ) {
        return deduplicationService.findByResearcher(researcherId);
    }

    @PostMapping("/researchers/{researcherId}/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public BibliographicDeduplicationResponse scan(
            @PathVariable UUID researcherId
    ) {
        return deduplicationService.scan(researcherId);
    }

    @PutMapping("/researchers/{researcherId}/candidates/{candidateId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public BibliographicDuplicateCandidateResponse review(
            @PathVariable UUID researcherId,
            @PathVariable UUID candidateId,
            @Valid @RequestBody ReviewBibliographicDuplicateRequest request,
            Authentication authentication
    ) {
        return deduplicationService.review(
                researcherId,
                candidateId,
                request,
                authentication == null ? null : authentication.getName()
        );
    }
}
