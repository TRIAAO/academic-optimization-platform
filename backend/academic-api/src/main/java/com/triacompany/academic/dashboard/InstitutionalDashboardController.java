package com.triacompany.academic.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/institutional-dashboard")
@RequiredArgsConstructor
public class InstitutionalDashboardController {

    private final InstitutionalDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public InstitutionalDashboardResponse generateDashboard() {
        return dashboardService.generateDashboard();
    }
}