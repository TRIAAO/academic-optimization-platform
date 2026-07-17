package com.triacompany.academic.orcid;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orcid/oauth")
@RequiredArgsConstructor
public class OrcidOAuthController {

    private final OrcidOAuthService oauthService;

    @GetMapping("/configuration")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidOAuthConfigurationResponse configuration() {
        return oauthService.configuration();
    }

    @GetMapping("/researchers/{researcherId}/connection")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidOAuthConnectionResponse findConnection(
            @PathVariable UUID researcherId
    ) {
        return oauthService.findConnection(researcherId);
    }

    @PostMapping("/researchers/{researcherId}/authorization-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidOAuthAuthorizationResponse createAuthorization(
            @PathVariable UUID researcherId,
            Authentication authentication
    ) {
        return oauthService.createAuthorization(
                researcherId,
                authentication.getName()
        );
    }

    @DeleteMapping("/researchers/{researcherId}/connection")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTITUTION')")
    public OrcidOAuthConnectionResponse disconnect(
            @PathVariable UUID researcherId,
            Authentication authentication
    ) {
        return oauthService.disconnect(researcherId, authentication.getName());
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String authorizationError,
            HttpServletRequest request
    ) {
        String ipAddress = resolveIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        if (authorizationError != null && !authorizationError.isBlank()) {
            oauthService.recordCallbackFailure(authorizationError, ipAddress, userAgent);
            return redirectToFrontend("error", safeCode(authorizationError), null, null, false);
        }

        try {
            OrcidOAuthCompletionResult result = oauthService.completeAuthorization(
                    code,
                    state,
                    ipAddress,
                    userAgent
            );

            return redirectToFrontend(
                    "success",
                    null,
                    result.researcherId(),
                    result.orcidId(),
                    result.profileSynchronized()
            );
        } catch (OrcidOAuthException exception) {
            oauthService.recordCallbackFailure(exception.getCode(), ipAddress, userAgent);
            return redirectToFrontend("error", exception.getCode(), null, null, false);
        } catch (RuntimeException exception) {
            oauthService.recordCallbackFailure("oauth_failed", ipAddress, userAgent);
            return redirectToFrontend("error", "oauth_failed", null, null, false);
        }
    }

    private ResponseEntity<Void> redirectToFrontend(
            String status,
            String errorCode,
            UUID researcherId,
            String orcidId,
            boolean profileSynchronized
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(oauthService.frontendRedirectUri())
                .queryParam("orcidOAuth", status);

        if (errorCode != null) {
            builder.queryParam("code", safeCode(errorCode));
        }

        if (researcherId != null) {
            builder.queryParam("researcherId", researcherId);
        }

        if (orcidId != null) {
            builder.queryParam("orcid", orcidId);
        }

        if ("success".equals(status)) {
            builder.queryParam("profileSynchronized", profileSynchronized);
        }

        URI location = builder.build(true).toUri();

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, location.toString())
                .build();
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private String safeCode(String value) {
        if (value == null || value.isBlank()) {
            return "oauth_failed";
        }

        String normalized = value.replaceAll("[^a-zA-Z0-9_-]", "");
        return normalized.isBlank() ? "oauth_failed" : normalized;
    }
}
