package com.triacompany.academic.orcid;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcidOAuthStateServiceTest {

    private final OrcidOAuthStateService stateService = new OrcidOAuthStateService(
            "orcid-oauth-state-secret-with-more-than-32-characters",
            10
    );

    @Test
    void shouldCreateAndValidateSignedState() {
        UUID researcherId = UUID.randomUUID();

        String state = stateService.create(researcherId, "admin@universidade.ao");
        OrcidOAuthStateService.StatePayload payload = stateService.validate(state);

        assertEquals(researcherId, payload.researcherId());
        assertEquals("admin@universidade.ao", payload.actorEmail());
        assertTrue(state.contains("."));
    }

    @Test
    void shouldRejectTamperedState() {
        String state = stateService.create(UUID.randomUUID(), "admin@universidade.ao");
        char replacement = state.charAt(0) == 'A' ? 'B' : 'A';
        String tampered = replacement + state.substring(1);

        OrcidOAuthException exception = assertThrows(
                OrcidOAuthException.class,
                () -> stateService.validate(tampered)
        );

        assertEquals("oauth_state_invalid", exception.getCode());
    }

    @Test
    void shouldRequireStrongStateSecret() {
        OrcidOAuthStateService invalidService = new OrcidOAuthStateService("short", 10);

        assertThrows(
                OrcidOAuthException.class,
                () -> invalidService.create(UUID.randomUUID(), "admin@universidade.ao")
        );
    }
}
