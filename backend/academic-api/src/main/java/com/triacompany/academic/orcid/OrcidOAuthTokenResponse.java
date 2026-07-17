package com.triacompany.academic.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrcidOAuthTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn,
        String scope,
        String name,
        String orcid
) {
}
