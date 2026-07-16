package com.triacompany.academic.openalex;

import com.fasterxml.jackson.databind.JsonNode;
import com.triacompany.academic.orcid.OrcidId;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OpenAlexAuthorIdentityService {

    private final ResearcherRepository researcherRepository;
    private final OpenAlexAuthorIdentityRepository identityRepository;
    private final OpenAlexClient openAlexClient;
    private final OpenAlexWorkRepository openAlexWorkRepository;

    @Transactional(readOnly = true)
    public Optional<OpenAlexAuthorIdentityResponse> findByResearcher(UUID researcherId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }

        return identityRepository.findByResearcherId(researcherId)
                .map(OpenAlexAuthorIdentityResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<OpenAlexAuthorIdentity> findEntityByResearcher(UUID researcherId) {
        return identityRepository.findByResearcherId(researcherId);
    }

    @Transactional(readOnly = true)
    public OpenAlexAuthorIdentity requireEntityByResearcher(UUID researcherId) {
        return identityRepository.findByResearcherId(researcherId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Confirme a identidade OpenAlex do pesquisador antes de consultar ou importar obras."
                ));
    }

    @Transactional
    public OpenAlexAuthorIdentityResponse confirm(
            UUID researcherId,
            OpenAlexAuthorIdentityRequest request
    ) {
        Researcher researcher = findResearcher(researcherId);
        String authorId = OpenAlexAuthorId.normalize(request.openAlexAuthorId());
        AuthorSnapshot snapshot = fetchSnapshot(authorId);

        validateOrcidCompatibility(researcher, snapshot.orcid());
        validateExclusiveLink(researcherId, authorId);

        OpenAlexAuthorIdentity identity = identityRepository.findByResearcherId(researcherId)
                .orElseGet(() -> OpenAlexAuthorIdentity.builder()
                        .researcher(researcher)
                        .build());

        boolean changedAuthor = identity.getOpenAlexAuthorId() != null
                && !identity.getOpenAlexAuthorId().equals(authorId);

        if (changedAuthor && openAlexWorkRepository.existsByResearcherId(researcherId)) {
            throw new IllegalArgumentException(
                    "Remova as obras OpenAlex importadas antes de trocar a identidade confirmada."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        applySnapshot(identity, snapshot);
        identity.setVerificationSource(resolveVerificationSource(researcher, snapshot.orcid()));
        identity.setLastSyncedAt(now);

        if (identity.getConfirmedAt() == null || changedAuthor) {
            identity.setConfirmedAt(now);
        }

        return OpenAlexAuthorIdentityResponse.fromEntity(identityRepository.save(identity));
    }

    @Transactional
    public OpenAlexAuthorIdentityResponse sync(UUID researcherId) {
        Researcher researcher = findResearcher(researcherId);
        OpenAlexAuthorIdentity identity = requireEntityByResearcher(researcherId);
        AuthorSnapshot snapshot = fetchSnapshot(identity.getOpenAlexAuthorId());

        validateOrcidCompatibility(researcher, snapshot.orcid());
        applySnapshot(identity, snapshot);
        identity.setLastSyncedAt(LocalDateTime.now());

        return OpenAlexAuthorIdentityResponse.fromEntity(identityRepository.save(identity));
    }

    private AuthorSnapshot fetchSnapshot(String authorId) {
        JsonNode author = openAlexClient.fetchAuthorById(authorId);
        String returnedId = OpenAlexAuthorId.normalize(text(author, "id"));

        if (!authorId.equals(returnedId)) {
            throw new IllegalArgumentException("O autor retornado pelo OpenAlex não corresponde ao ID informado.");
        }

        return new AuthorSnapshot(
                returnedId,
                text(author, "display_name"),
                normalizeExternalOrcid(text(author, "orcid")),
                text(author, "last_known_institutions", "0", "display_name"),
                text(author, "last_known_institutions", "0", "country_code"),
                integerOrZero(author, "works_count"),
                integerOrZero(author, "cited_by_count")
        );
    }

    private void applySnapshot(OpenAlexAuthorIdentity identity, AuthorSnapshot snapshot) {
        identity.setOpenAlexAuthorId(snapshot.authorId());
        identity.setDisplayName(snapshot.displayName());
        identity.setOrcidId(snapshot.orcid());
        identity.setLastKnownInstitution(snapshot.institution());
        identity.setLastKnownCountryCode(snapshot.countryCode());
        identity.setWorksCount(snapshot.worksCount());
        identity.setCitedByCount(snapshot.citedByCount());
    }

    private void validateExclusiveLink(UUID researcherId, String authorId) {
        identityRepository.findByOpenAlexAuthorId(authorId)
                .filter(existing -> !existing.getResearcher().getId().equals(researcherId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Este autor OpenAlex já está vinculado a outro pesquisador."
                    );
                });
    }

    private void validateOrcidCompatibility(Researcher researcher, String authorOrcid) {
        String researcherOrcid = normalizeExternalOrcid(researcher.getOrcidId());

        if (researcherOrcid != null && authorOrcid != null && !researcherOrcid.equals(authorOrcid)) {
            throw new IllegalArgumentException(
                    "O ORCID do autor OpenAlex não corresponde ao ORCID cadastrado para o pesquisador."
            );
        }
    }

    private OpenAlexIdentityVerificationSource resolveVerificationSource(
            Researcher researcher,
            String authorOrcid
    ) {
        String researcherOrcid = normalizeExternalOrcid(researcher.getOrcidId());
        return researcherOrcid != null && researcherOrcid.equals(authorOrcid)
                ? OpenAlexIdentityVerificationSource.ORCID
                : OpenAlexIdentityVerificationSource.MANUAL;
    }

    private Researcher findResearcher(UUID researcherId) {
        return researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
    }

    private String normalizeExternalOrcid(String value) {
        try {
            return OrcidId.normalize(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String text(JsonNode node, String... path) {
        JsonNode current = node;

        for (String part : path) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return null;
            }

            if (current.isArray()) {
                try {
                    current = current.path(Integer.parseInt(part));
                } catch (NumberFormatException exception) {
                    return null;
                }
            } else {
                current = current.path(part);
            }
        }

        if (current.isMissingNode() || current.isNull()) {
            return null;
        }

        String value = current.asText(null);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int integerOrZero(JsonNode node, String... path) {
        String value = text(node, path);

        if (value == null) {
            return 0;
        }

        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private record AuthorSnapshot(
            String authorId,
            String displayName,
            String orcid,
            String institution,
            String countryCode,
            int worksCount,
            int citedByCount
    ) {
    }
}
