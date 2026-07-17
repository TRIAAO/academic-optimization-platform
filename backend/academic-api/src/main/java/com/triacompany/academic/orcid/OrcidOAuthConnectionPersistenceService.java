package com.triacompany.academic.orcid;

import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrcidOAuthConnectionPersistenceService {

    private final ResearcherRepository researcherRepository;
    private final OrcidOAuthConnectionRepository connectionRepository;
    private final OrcidTokenCipher tokenCipher;

    @Transactional
    public OrcidOAuthConnection saveAuthenticatedConnection(
            UUID researcherId,
            OrcidOAuthTokenResponse tokenResponse
    ) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new OrcidOAuthException(
                        "researcher_not_found",
                        "Pesquisador não encontrado."
                ));

        String authenticatedOrcid = OrcidId.normalize(tokenResponse.orcid());
        String currentOrcid = normalizeExistingOrcid(researcher.getOrcidId());

        if (currentOrcid != null && !currentOrcid.equals(authenticatedOrcid)) {
            throw new OrcidOAuthException(
                    "orcid_link_conflict",
                    "O pesquisador já está associado a outro ORCID. Revise o cadastro antes de conectar um novo identificador."
            );
        }

        OrcidOAuthConnection connectionForOrcid = connectionRepository
                .findByOrcidId(authenticatedOrcid)
                .orElse(null);

        if (connectionForOrcid != null
                && !connectionForOrcid.getResearcher().getId().equals(researcherId)) {
            throw new OrcidOAuthException(
                    "orcid_already_linked",
                    "Este ORCID autenticado já está vinculado a outro pesquisador."
            );
        }

        OrcidOAuthConnection connection = connectionRepository
                .findByResearcherId(researcherId)
                .orElseGet(() -> connectionForOrcid != null
                        ? connectionForOrcid
                        : OrcidOAuthConnection.builder()
                                .researcher(researcher)
                                .build());

        LocalDateTime now = LocalDateTime.now();
        connection.setResearcher(researcher);
        connection.setOrcidId(authenticatedOrcid);
        connection.setAuthenticatedName(normalize(tokenResponse.name()));
        connection.setTokenType(normalize(tokenResponse.tokenType()));
        connection.setScope(defaultScope(tokenResponse.scope()));
        connection.setEncryptedAccessToken(tokenCipher.encrypt(tokenResponse.accessToken()));
        connection.setEncryptedRefreshToken(tokenCipher.encrypt(tokenResponse.refreshToken()));
        connection.setExpiresAt(resolveExpiresAt(tokenResponse.expiresIn(), now));
        connection.setConnectedAt(now);
        connection.setRevokedAt(null);

        researcher.setOrcidId(authenticatedOrcid);
        researcherRepository.save(researcher);
        return connectionRepository.save(connection);
    }

    @Transactional
    public void markRevoked(UUID researcherId) {
        OrcidOAuthConnection connection = connectionRepository
                .findByResearcherIdAndRevokedAtIsNull(researcherId)
                .orElseThrow(() -> new OrcidOAuthException(
                        "oauth_connection_not_found",
                        "Nenhuma conexão OAuth ORCID ativa foi encontrada."
                ));

        connection.setEncryptedAccessToken("revoked");
        connection.setEncryptedRefreshToken(null);
        connection.setRevokedAt(LocalDateTime.now());
        connectionRepository.save(connection);
    }

    private String normalizeExistingOrcid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return OrcidId.normalize(value);
    }

    private String defaultScope(String value) {
        String normalized = normalize(value);
        return normalized == null ? "/authenticate" : normalized;
    }

    private LocalDateTime resolveExpiresAt(Long expiresIn, LocalDateTime now) {
        if (expiresIn == null || expiresIn <= 0) {
            return null;
        }

        return now.plusSeconds(expiresIn);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
