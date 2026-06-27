package com.triacompany.academic.openalex;

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

    @GetMapping("/researchers/{researcherId}/works")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<OpenAlexWorkResponse> findWorksByResearcher(@PathVariable UUID researcherId) {
        return openAlexService.findWorksByResearcher(researcherId);
    }

    @DeleteMapping("/researchers/{researcherId}/works")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OpenAlexCleanupResponse deleteWorksByResearcher(@PathVariable UUID researcherId) {
        return openAlexService.deleteWorksByResearcher(researcherId);
    }
}