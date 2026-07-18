package com.triacompany.academic.scientometrics;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InstitutionalScientometricHistoryController {

    private final InstitutionalScientometricHistoryService historyService;

    @GetMapping("/api/v1/institutional-scientometrics/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public InstitutionalScientometricHistoryResponse findHistory(
            @RequestParam(defaultValue = "12") Integer months,
            @RequestParam(defaultValue = "12") Integer staleAfterMonths
    ) {
        return historyService.buildHistory(months, staleAfterMonths);
    }
}
