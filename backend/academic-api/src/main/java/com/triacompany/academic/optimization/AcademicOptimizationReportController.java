package com.triacompany.academic.optimization;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/optimization-reports")
@RequiredArgsConstructor
public class AcademicOptimizationReportController {

    private final AcademicOptimizationReportService reportService;

    @GetMapping("/researchers/{researcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AcademicOptimizationReportResponse generateReport(@PathVariable UUID researcherId) {
        return reportService.generateReport(researcherId);
    }
}