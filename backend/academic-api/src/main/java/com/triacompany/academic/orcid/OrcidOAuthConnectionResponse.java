package com.triacompany.academic.orcid;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrcidOAuthConnectionResponse(
        UUID researcherId,
        boolean connected,
        String orcidId,
        String authenticatedName,
        String scope,
        LocalDateTime connectedAt,
        LocalDateTime expiresAt
) {

    public static OrcidOAuthConnectionResponse disconnected(UUID researcherId) {
        return new OrcidOAuthConnectionResponse(
                researcherId,
                false,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static OrcidOAuthConnectionResponse fromEntity(OrcidOAuthConnection connection) {
        return new OrcidOAuthConnectionResponse(
                connection.getResearcher().getId(),
                connection.getRevokedAt() == null,
                connection.getOrcidId(),
                connection.getAuthenticatedName(),
                connection.getScope(),
                connection.getConnectedAt(),
                connection.getExpiresAt()
        );
    }
}
