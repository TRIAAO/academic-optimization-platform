package com.triacompany.academic.orcid;

import com.triacompany.academic.audit.AuditEventStatus;
import com.triacompany.academic.audit.AuditLogService;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrcidOAuthService {

    private final ResearcherRepository researcherRepository;
    private final OrcidOAuthConnectionRepository connectionRepository;
    private final OrcidOAuthConnectionPersistenceService persistenceService;
    private final OrcidProfileSyncService profileSyncService;
    private final OrcidOAuthClient oauthClient;
    private final OrcidOAuthStateService stateService;
    private final OrcidTokenCipher tokenCipher;
    private final AuditLogService auditLogService;

    public OrcidOAuthConfigurationResponse configuration() {
        boolean configured = isConfigured();
        String message = configured
                ? "A conexão autenticada com o ORCID está disponível."
                : "A conexão OAuth ORCID aguarda credenciais e segredos do ambiente.";

        return new OrcidOAuthConfigurationResponse(
                configured,
                oauthClient.isSandbox(),
                oauthClient.scope(),
                message
        );
    }

    public OrcidOAuthAuthorizationResponse createAuthorization(
            UUID researcherId,
            String actorEmail
    ) {
        requireConfigured();

        if (!researcherRepository.existsById(researcherId)) {
            throw new OrcidOAuthException(
                    "researcher_not_found",
                    "Pesquisador não encontrado."
            );
        }

        String state = stateService.create(researcherId, actorEmail);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                stateService.expiresAt(),
                ZoneOffset.UTC
        );

        return new OrcidOAuthAuthorizationResponse(
                oauthClient.buildAuthorizationUrl(state),
                expiresAt,
                oauthClient.isSandbox()
        );
    }

    @Transactional(readOnly = true)
    public OrcidOAuthConnectionResponse findConnection(UUID researcherId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new OrcidOAuthException(
                    "researcher_not_found",
                    "Pesquisador não encontrado."
            );
        }

        return connectionRepository.findByResearcherIdAndRevokedAtIsNull(researcherId)
                .map(OrcidOAuthConnectionResponse::fromEntity)
                .orElseGet(() -> OrcidOAuthConnectionResponse.disconnected(researcherId));
    }

    public OrcidOAuthCompletionResult completeAuthorization(
            String code,
            String state,
            String ipAddress,
            String userAgent
    ) {
        requireConfigured();

        if (code == null || code.isBlank()) {
            throw new OrcidOAuthException(
                    "oauth_code_missing",
                    "O ORCID não devolveu o código de autorização."
            );
        }

        OrcidOAuthStateService.StatePayload statePayload = stateService.validate(state);
        OrcidOAuthTokenResponse tokenResponse = oauthClient.exchangeAuthorizationCode(code.trim());
        OrcidOAuthConnection connection = persistenceService.saveAuthenticatedConnection(
                statePayload.researcherId(),
                tokenResponse
        );

        boolean profileSynchronized = synchronizeProfileSafely(statePayload.researcherId());

        auditLogService.record(
                statePayload.actorEmail(),
                "OAUTH_CALLBACK",
                "ORCID_OAUTH_CONNECT",
                "ORCID",
                "GET",
                "/api/v1/orcid/oauth/callback",
                "RESEARCHER",
                statePayload.researcherId().toString(),
                AuditEventStatus.SUCCESS,
                303,
                ipAddress,
                userAgent,
                "ORCID autenticado e vinculado com consentimento do pesquisador."
        );

        return new OrcidOAuthCompletionResult(
                statePayload.researcherId(),
                connection.getOrcidId(),
                profileSynchronized,
                statePayload.actorEmail()
        );
    }

    public OrcidOAuthConnectionResponse disconnect(
            UUID researcherId,
            String actorEmail
    ) {
        requireConfigured();

        OrcidOAuthConnection connection = connectionRepository
                .findByResearcherIdAndRevokedAtIsNull(researcherId)
                .orElseThrow(() -> new OrcidOAuthException(
                        "oauth_connection_not_found",
                        "Nenhuma conexão OAuth ORCID ativa foi encontrada."
                ));

        String tokenToRevoke = tokenCipher.decrypt(connection.getEncryptedRefreshToken());
        if (tokenToRevoke == null) {
            tokenToRevoke = tokenCipher.decrypt(connection.getEncryptedAccessToken());
        }

        oauthClient.revoke(tokenToRevoke);
        persistenceService.markRevoked(researcherId);

        auditLogService.record(
                actorEmail,
                null,
                "ORCID_OAUTH_DISCONNECT",
                "ORCID",
                "DELETE",
                "/api/v1/orcid/oauth/researchers/" + researcherId + "/connection",
                "RESEARCHER",
                researcherId.toString(),
                AuditEventStatus.SUCCESS,
                200,
                null,
                null,
                "Autorização OAuth ORCID revogada e tokens locais removidos."
        );

        return OrcidOAuthConnectionResponse.disconnected(researcherId);
    }

    public String frontendRedirectUri() {
        return oauthClient.frontendRedirectUri();
    }

    public void recordCallbackFailure(
            String errorCode,
            String ipAddress,
            String userAgent
    ) {
        auditLogService.record(
                null,
                "OAUTH_CALLBACK",
                "ORCID_OAUTH_CONNECT",
                "ORCID",
                "GET",
                "/api/v1/orcid/oauth/callback",
                "ORCID_RESOURCE",
                null,
                AuditEventStatus.CLIENT_ERROR,
                303,
                ipAddress,
                userAgent,
                "Falha no callback OAuth ORCID: " + safeCode(errorCode) + "."
        );
    }

    private boolean synchronizeProfileSafely(UUID researcherId) {
        try {
            profileSyncService.syncProfile(researcherId);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean isConfigured() {
        return oauthClient.isConfigured()
                && stateService.isConfigured()
                && tokenCipher.isConfigured();
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new OrcidOAuthException(
                    "oauth_not_configured",
                    "A integração OAuth ORCID ainda não está configurada neste ambiente."
            );
        }
    }

    private String safeCode(String value) {
        if (value == null || value.isBlank()) {
            return "oauth_failed";
        }

        return value.replaceAll("[^a-zA-Z0-9_-]", "");
    }
}
