package com.triacompany.academic.orcid;

public record OrcidOAuthConfigurationResponse(
        boolean enabled,
        boolean sandbox,
        String scope,
        String message
) {
}
