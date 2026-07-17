package com.triacompany.academic.orcid;

import java.time.LocalDateTime;

public record OrcidOAuthAuthorizationResponse(
        String authorizationUrl,
        LocalDateTime stateExpiresAt,
        boolean sandbox
) {
}
