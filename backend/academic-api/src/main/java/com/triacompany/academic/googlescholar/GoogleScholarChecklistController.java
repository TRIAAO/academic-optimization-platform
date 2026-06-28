package com.triacompany.academic.googlescholar;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/google-scholar-checklists")
@RequiredArgsConstructor
public class GoogleScholarChecklistController {

    private final GoogleScholarChecklistService checklistService;

    @GetMapping("/researchers/{researcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public GoogleScholarChecklistResponse generateChecklist(@PathVariable UUID researcherId) {
        return checklistService.generateChecklist(researcherId);
    }
}