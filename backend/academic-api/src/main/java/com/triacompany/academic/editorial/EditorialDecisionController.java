package com.triacompany.academic.editorial;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/editorial-decisions")
@RequiredArgsConstructor
public class EditorialDecisionController {

    private final EditorialDecisionService editorialDecisionService;

    @GetMapping("/researchers/{researcherId}/works/{workId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public ResponseEntity<EditorialDecisionResponse> findByWork(
            @PathVariable UUID researcherId,
            @PathVariable UUID workId
    ) {
        return editorialDecisionService.findByWork(researcherId, workId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/researchers/{researcherId}/works/{workId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public EditorialDecisionResponse save(
            @PathVariable UUID researcherId,
            @PathVariable UUID workId,
            @Valid @RequestBody SaveEditorialDecisionRequest request,
            Authentication authentication
    ) {
        return editorialDecisionService.save(
                researcherId,
                workId,
                request,
                authentication == null ? null : authentication.getName()
        );
    }
}
