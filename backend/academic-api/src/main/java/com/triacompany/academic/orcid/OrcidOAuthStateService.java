package com.triacompany.academic.orcid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Component
public class OrcidOAuthStateService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String stateSecret;
    private final long stateTtlMinutes;

    public OrcidOAuthStateService(
            @Value("${app.orcid.oauth.state-secret:}") String stateSecret,
            @Value("${app.orcid.oauth.state-ttl-minutes:10}") long stateTtlMinutes
    ) {
        this.stateSecret = stateSecret == null ? "" : stateSecret.trim();
        this.stateTtlMinutes = Math.max(1, Math.min(stateTtlMinutes, 30));
    }

    public boolean isConfigured() {
        return stateSecret.length() >= 32;
    }

    public String create(UUID researcherId, String actorEmail) {
        if (!isConfigured()) {
            throw new OrcidOAuthException(
                    "oauth_not_configured",
                    "O segredo de estado do OAuth ORCID não está configurado."
            );
        }

        if (researcherId == null || actorEmail == null || actorEmail.isBlank()) {
            throw new OrcidOAuthException(
                    "oauth_state_invalid",
                    "Não foi possível identificar o pesquisador ou o utilizador responsável."
            );
        }

        String payload = researcherId
                + "|" + Instant.now().getEpochSecond()
                + "|" + UUID.randomUUID()
                + "|" + actorEmail.trim();

        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        String signature = encode(sign(encodedPayload));
        return encodedPayload + "." + signature;
    }

    public StatePayload validate(String state) {
        if (!isConfigured() || state == null || state.isBlank()) {
            throw invalidState();
        }

        String[] stateParts = state.split("\\.", -1);
        if (stateParts.length != 2) {
            throw invalidState();
        }

        byte[] expectedSignature = sign(stateParts[0]);
        byte[] receivedSignature;

        try {
            receivedSignature = Base64.getUrlDecoder().decode(stateParts[1]);
        } catch (IllegalArgumentException exception) {
            throw invalidState();
        }

        if (!MessageDigest.isEqual(expectedSignature, receivedSignature)) {
            throw invalidState();
        }

        try {
            String payload = new String(
                    Base64.getUrlDecoder().decode(stateParts[0]),
                    StandardCharsets.UTF_8
            );
            String[] payloadParts = payload.split("\\|", 4);
            if (payloadParts.length != 4) {
                throw invalidState();
            }

            UUID researcherId = UUID.fromString(payloadParts[0]);
            Instant issuedAt = Instant.ofEpochSecond(Long.parseLong(payloadParts[1]));
            String actorEmail = payloadParts[3].trim();

            Instant now = Instant.now();
            Instant earliestAccepted = now.minus(stateTtlMinutes, ChronoUnit.MINUTES);
            Instant latestAccepted = now.plus(60, ChronoUnit.SECONDS);

            if (issuedAt.isBefore(earliestAccepted)
                    || issuedAt.isAfter(latestAccepted)
                    || actorEmail.isBlank()) {
                throw invalidState();
            }

            return new StatePayload(researcherId, actorEmail, issuedAt);
        } catch (OrcidOAuthException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalidState();
        }
    }

    public Instant expiresAt() {
        return Instant.now().plus(stateTtlMinutes, ChronoUnit.MINUTES);
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(stateSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new OrcidOAuthException(
                    "oauth_state_signing_failed",
                    "Não foi possível proteger o estado OAuth ORCID.",
                    exception
            );
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private OrcidOAuthException invalidState() {
        return new OrcidOAuthException(
                "oauth_state_invalid",
                "O estado OAuth ORCID é inválido ou expirou. Reinicie a conexão."
        );
    }

    public record StatePayload(
            UUID researcherId,
            String actorEmail,
            Instant issuedAt
    ) {
    }
}
