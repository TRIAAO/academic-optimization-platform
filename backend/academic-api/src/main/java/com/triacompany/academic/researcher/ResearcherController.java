package com.triacompany.academic.researcher;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/researchers")
@RequiredArgsConstructor
public class ResearcherController {

    private final ResearcherService researcherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResearcherResponse create(@Valid @RequestBody CreateResearcherRequest request) {
        return researcherService.create(request);
    }

    @GetMapping
    public List<ResearcherResponse> findAll() {
        return researcherService.findAll();
    }

    @GetMapping("/{id}")
    public ResearcherResponse findById(@PathVariable UUID id) {
        return researcherService.findById(id);
    }

    @PutMapping("/{id}")
    public ResearcherResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResearcherRequest request
    ) {
        return researcherService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        researcherService.deactivate(id);
    }
}