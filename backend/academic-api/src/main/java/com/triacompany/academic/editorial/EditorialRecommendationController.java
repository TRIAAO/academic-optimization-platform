package com.triacompany.academic.editorial;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/editorial-recommendations")
@RequiredArgsConstructor
public class EditorialRecommendationController {

    private final EditorialRecommendationService editorialRecommendationService;

    @GetMapping("/researchers/{researcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public EditorialRecommendationResponse generate(
            @PathVariable UUID researcherId,
            @RequestParam(required = false) UUID workId
    ) {
        return editorialRecommendationService.generate(researcherId, workId);
    }
}
