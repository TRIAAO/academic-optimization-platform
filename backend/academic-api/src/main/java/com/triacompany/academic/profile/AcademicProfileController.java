package com.triacompany.academic.profile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic-profiles")
@RequiredArgsConstructor
public class AcademicProfileController {

    private final AcademicProfileService academicProfileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AcademicProfileResponse create(@Valid @RequestBody CreateAcademicProfileRequest request) {
        return academicProfileService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public List<AcademicProfileResponse> findAll() {
        return academicProfileService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AcademicProfileResponse findById(@PathVariable UUID id) {
        return academicProfileService.findById(id);
    }

    @GetMapping("/researcher/{researcherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AcademicProfileResponse findByResearcherId(@PathVariable UUID researcherId) {
        return academicProfileService.findByResearcherId(researcherId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public AcademicProfileResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAcademicProfileRequest request
    ) {
        return academicProfileService.update(id, request);
    }
}