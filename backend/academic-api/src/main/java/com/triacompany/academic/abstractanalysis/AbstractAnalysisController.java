package com.triacompany.academic.abstractanalysis;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/abstract-analysis")
@RequiredArgsConstructor
public class AbstractAnalysisController {

    private final AbstractAnalysisService abstractAnalysisService;

    @GetMapping("/researchers/{researcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AbstractAnalysisResponse analyze(@PathVariable UUID researcherId) {
        return abstractAnalysisService.analyze(researcherId);
    }

    @PatchMapping("/works/{workId}/translations")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AbstractWorkAnalysisResponse updateTranslations(
            @PathVariable UUID workId,
            @Valid @RequestBody AbstractTranslationRequest request
    ) {
        return abstractAnalysisService.updateTranslations(workId, request);
    }
}
