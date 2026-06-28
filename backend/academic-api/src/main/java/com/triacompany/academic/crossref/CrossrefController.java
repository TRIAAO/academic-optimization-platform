package com.triacompany.academic.crossref;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crossref")
@RequiredArgsConstructor
public class CrossrefController {

    private final CrossrefService crossrefService;

    @PostMapping("/openalex-works/{workId}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public CrossrefValidationResponse validateOpenAlexWork(@PathVariable UUID workId) {
        return crossrefService.validateOpenAlexWork(workId);
    }

    @GetMapping("/openalex-works/{workId}/validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public CrossrefValidationResponse findLatestValidationByOpenAlexWork(@PathVariable UUID workId) {
        return crossrefService.findLatestValidationByOpenAlexWork(workId);
    }

    @GetMapping("/researchers/{researcherId}/validations")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<CrossrefValidationResponse> findValidationsByResearcher(@PathVariable UUID researcherId) {
        return crossrefService.findValidationsByResearcher(researcherId);
    }
}