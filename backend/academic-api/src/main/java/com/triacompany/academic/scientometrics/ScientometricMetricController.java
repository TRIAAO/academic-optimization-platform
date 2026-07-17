package com.triacompany.academic.scientometrics;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ScientometricMetricController {

    private final ScientometricMetricService scientometricMetricService;
    private final ScientometricAnalysisService scientometricAnalysisService;

    @PostMapping("/api/v1/researchers/{researcherId}/scientometric-metrics")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public ScientometricMetricResponse create(
            @PathVariable UUID researcherId,
            @Valid @RequestBody CreateScientometricMetricRequest request
    ) {
        return scientometricMetricService.create(researcherId, request);
    }

    @GetMapping("/api/v1/researchers/{researcherId}/scientometric-metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<ScientometricMetricResponse> findByResearcher(@PathVariable UUID researcherId) {
        return scientometricMetricService.findByResearcher(researcherId);
    }

    @GetMapping("/api/v1/researchers/{researcherId}/scientometric-metrics/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public ScientometricMetricResponse findLatestByResearcher(@PathVariable UUID researcherId) {
        return scientometricMetricService.findLatestByResearcher(researcherId);
    }

    @GetMapping("/api/v1/researchers/{researcherId}/scientometric-analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public ScientometricAnalysisResponse analyze(@PathVariable UUID researcherId) {
        return scientometricAnalysisService.analyze(researcherId);
    }

    @GetMapping("/api/v1/scientometric-metrics/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public ScientometricMetricResponse findById(@PathVariable UUID id) {
        return scientometricMetricService.findById(id);
    }

    @PutMapping("/api/v1/scientometric-metrics/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public ScientometricMetricResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateScientometricMetricRequest request
    ) {
        return scientometricMetricService.update(id, request);
    }

    @DeleteMapping("/api/v1/scientometric-metrics/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public void delete(@PathVariable UUID id) {
        scientometricMetricService.delete(id);
    }
}
