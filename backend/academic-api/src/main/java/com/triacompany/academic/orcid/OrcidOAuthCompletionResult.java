package com.triacompany.academic.orcid;

import java.util.UUID;

public record OrcidOAuthCompletionResult(
        UUID researcherId,
        String orcidId,
        boolean profileSynchronized,
        String actorEmail
) {
}
