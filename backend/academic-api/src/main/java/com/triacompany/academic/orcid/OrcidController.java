package com.triacompany.academic.orcid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orcid")
@RequiredArgsConstructor
public class OrcidController {

    private final OrcidImportService orcidImportService;
    private final OrcidProfileService orcidProfileService;
    private final OrcidProfileSyncService orcidProfileSyncService;

    @PostMapping("/researchers/{researcherId}/import-works")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidImportResponse importWorks(@PathVariable UUID researcherId) {
        return orcidImportService.importWorks(researcherId);
    }

    @GetMapping("/researchers/{researcherId}/works")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<OrcidWorkResponse> findWorksByResearcher(@PathVariable UUID researcherId) {
        return orcidImportService.findWorksByResearcher(researcherId);
    }

    @GetMapping("/researchers/{researcherId}/logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<OrcidImportLogResponse> findImportLogsByResearcher(@PathVariable UUID researcherId) {
        return orcidImportService.findImportLogsByResearcher(researcherId);
    }

    @GetMapping("/researchers/{researcherId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidProfileSummaryResponse findSummaryByResearcher(@PathVariable UUID researcherId) {
        return orcidProfileService.findSummaryByResearcher(researcherId);
    }

    @PostMapping("/researchers/{researcherId}/sync-profile")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidProfileSyncResponse syncProfile(@PathVariable UUID researcherId) {
        return orcidProfileSyncService.syncProfile(researcherId);
    }

    @GetMapping("/{orcidId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidProfileSummaryResponse findSummaryByOrcidId(@PathVariable String orcidId) {
        return orcidProfileService.findSummaryByOrcidId(orcidId);
    }
}