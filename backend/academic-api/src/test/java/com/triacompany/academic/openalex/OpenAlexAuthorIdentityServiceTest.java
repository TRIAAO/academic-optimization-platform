package com.triacompany.academic.openalex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAlexAuthorIdentityServiceTest {

    @Test
    void confirmsAndPersistsAnOrcidMatchedIdentity() throws Exception {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        OpenAlexAuthorIdentityRepository identityRepository = mock(OpenAlexAuthorIdentityRepository.class);
        OpenAlexClient openAlexClient = mock(OpenAlexClient.class);
        OpenAlexWorkRepository openAlexWorkRepository = mock(OpenAlexWorkRepository.class);
        OpenAlexAuthorIdentityService service = new OpenAlexAuthorIdentityService(
                researcherRepository,
                identityRepository,
                openAlexClient,
                openAlexWorkRepository
        );
        UUID researcherId = UUID.randomUUID();
        Researcher researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Ana Silva")
                .orcidId("0000-0002-1825-0097")
                .build();
        var author = new ObjectMapper().readTree("""
                {
                  "id": "https://openalex.org/A5014082506",
                  "display_name": "Ana Silva",
                  "orcid": "https://orcid.org/0000-0002-1825-0097",
                  "works_count": 14,
                  "cited_by_count": 35,
                  "last_known_institutions": [
                    {
                      "display_name": "Universidade Metropolitana de Angola",
                      "country_code": "AO"
                    }
                  ]
                }
                """);

        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
        when(openAlexClient.fetchAuthorById("A5014082506")).thenReturn(author);
        when(identityRepository.findByOpenAlexAuthorId("A5014082506")).thenReturn(Optional.empty());
        when(identityRepository.findByResearcherId(researcherId)).thenReturn(Optional.empty());
        when(identityRepository.save(any(OpenAlexAuthorIdentity.class))).thenAnswer(invocation -> {
            OpenAlexAuthorIdentity identity = invocation.getArgument(0);
            identity.prePersist();
            return identity;
        });

        OpenAlexAuthorIdentityResponse response = service.confirm(
                researcherId,
                new OpenAlexAuthorIdentityRequest("https://openalex.org/A5014082506")
        );

        assertThat(response.openAlexAuthorId()).isEqualTo("A5014082506");
        assertThat(response.displayName()).isEqualTo("Ana Silva");
        assertThat(response.verificationSource()).isEqualTo(OpenAlexIdentityVerificationSource.ORCID);
        assertThat(response.worksCount()).isEqualTo(14);
        assertThat(response.citedByCount()).isEqualTo(35);
        assertThat(response.lastKnownCountryCode()).isEqualTo("AO");
        assertThat(response.confirmedAt()).isNotNull();
        verify(identityRepository).save(any(OpenAlexAuthorIdentity.class));
    }

    @Test
    void rejectsAnIdentityWithADifferentPublicOrcid() throws Exception {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        OpenAlexAuthorIdentityRepository identityRepository = mock(OpenAlexAuthorIdentityRepository.class);
        OpenAlexClient openAlexClient = mock(OpenAlexClient.class);
        OpenAlexWorkRepository openAlexWorkRepository = mock(OpenAlexWorkRepository.class);
        OpenAlexAuthorIdentityService service = new OpenAlexAuthorIdentityService(
                researcherRepository,
                identityRepository,
                openAlexClient,
                openAlexWorkRepository
        );
        UUID researcherId = UUID.randomUUID();
        Researcher researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Ana Silva")
                .orcidId("0000-0002-1825-0097")
                .build();
        var author = new ObjectMapper().readTree("""
                {
                  "id": "https://openalex.org/A5014082506",
                  "display_name": "Outra Pessoa",
                  "orcid": "https://orcid.org/0000-0001-5109-3700"
                }
                """);

        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
        when(openAlexClient.fetchAuthorById("A5014082506")).thenReturn(author);

        assertThatThrownBy(() -> service.confirm(
                researcherId,
                new OpenAlexAuthorIdentityRequest("A5014082506")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não corresponde");
    }

    @Test
    void blocksChangingIdentityWhileImportedWorksExist() throws Exception {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        OpenAlexAuthorIdentityRepository identityRepository = mock(OpenAlexAuthorIdentityRepository.class);
        OpenAlexClient openAlexClient = mock(OpenAlexClient.class);
        OpenAlexWorkRepository openAlexWorkRepository = mock(OpenAlexWorkRepository.class);
        OpenAlexAuthorIdentityService service = new OpenAlexAuthorIdentityService(
                researcherRepository,
                identityRepository,
                openAlexClient,
                openAlexWorkRepository
        );
        UUID researcherId = UUID.randomUUID();
        Researcher researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Ana Silva")
                .build();
        OpenAlexAuthorIdentity currentIdentity = OpenAlexAuthorIdentity.builder()
                .id(UUID.randomUUID())
                .researcher(researcher)
                .openAlexAuthorId("A111111111")
                .build();
        var replacementAuthor = new ObjectMapper().readTree("""
                {
                  "id": "https://openalex.org/A222222222",
                  "display_name": "Ana Silva"
                }
                """);

        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
        when(openAlexClient.fetchAuthorById("A222222222")).thenReturn(replacementAuthor);
        when(identityRepository.findByOpenAlexAuthorId("A222222222")).thenReturn(Optional.empty());
        when(identityRepository.findByResearcherId(researcherId)).thenReturn(Optional.of(currentIdentity));
        when(openAlexWorkRepository.existsByResearcherId(researcherId)).thenReturn(true);

        assertThatThrownBy(() -> service.confirm(
                researcherId,
                new OpenAlexAuthorIdentityRequest("A222222222")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Remova as obras OpenAlex");
    }
}
