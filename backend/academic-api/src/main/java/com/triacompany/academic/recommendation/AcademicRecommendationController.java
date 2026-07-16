package com.triacompany.academic.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic-recommendations")
@RequiredArgsConstructor
public class AcademicRecommendationController {

    private final AcademicRecommendationService recommendationService;

    @GetMapping("/researchers/{researcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AcademicRecommendationResponse generate(@PathVariable UUID researcherId) {
        return recommendationService.generate(researcherId);
    }
}
