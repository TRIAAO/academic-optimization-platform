package com.triacompany.academic.openalex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OpenAlexServiceAbstractTest {

    @Test
    void reconstructsAbstractFromOpenAlexInvertedIndexInPositionOrder() throws Exception {
        OpenAlexService service = new OpenAlexService(
                mock(com.triacompany.academic.researcher.ResearcherRepository.class),
                mock(OpenAlexClient.class),
                mock(OpenAlexWorkRepository.class)
        );
        var invertedIndex = new ObjectMapper().readTree("""
                {
                  "A": [0],
                  "educação": [1],
                  "transforma": [2],
                  "vidas": [3],
                  ".": [4]
                }
                """);

        assertThat(service.reconstructAbstract(invertedIndex))
                .isEqualTo("A educação transforma vidas.");
    }

    @Test
    void syncsImportedWorkDirectlyWithoutLookingUpAuthorByOrcid() throws Exception {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        OpenAlexClient openAlexClient = mock(OpenAlexClient.class);
        OpenAlexWorkRepository openAlexWorkRepository = mock(OpenAlexWorkRepository.class);
        OpenAlexService service = new OpenAlexService(
                researcherRepository,
                openAlexClient,
                openAlexWorkRepository
        );
        UUID researcherId = UUID.randomUUID();
        Researcher researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Pesquisador importado por candidato")
                .orcidId("0000-0000-0000-0000")
                .build();
        OpenAlexWork importedWork = OpenAlexWork.builder()
                .id(UUID.randomUUID())
                .researcher(researcher)
                .openAlexId("https://openalex.org/W123456789")
                .title("Educação e transformação")
                .reviewStatus(PublicationReviewStatus.CONFIRMED)
                .build();
        var openAlexWork = new ObjectMapper().readTree("""
                {
                  "id": "https://openalex.org/W123456789",
                  "language": "pt",
                  "abstract_inverted_index": {
                    "Educação": [0],
                    "transforma": [1],
                    "vidas": [2],
                    ".": [3]
                  }
                }
                """);

        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
        when(openAlexWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId))
                .thenReturn(List.of(importedWork));
        when(openAlexClient.fetchWorkById(importedWork.getOpenAlexId())).thenReturn(openAlexWork);
        when(openAlexWorkRepository.save(any(OpenAlexWork.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OpenAlexAbstractSyncResponse response = service.syncAbstracts(researcherId);

        assertThat(response.totalFound()).isEqualTo(1);
        assertThat(response.matchedImportedWorks()).isEqualTo(1);
        assertThat(response.updatedWorks()).isEqualTo(1);
        assertThat(response.worksWithAbstract()).isEqualTo(1);
        assertThat(importedWork.getAbstractText()).isEqualTo("Educação transforma vidas.");
        assertThat(importedWork.getAbstractLanguage()).isEqualTo("pt");
        verify(openAlexClient, never()).fetchAuthorByOrcid(any());
    }

    @Test
    void skipsExternalLookupWhenResearcherHasNoImportedWorks() {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        OpenAlexClient openAlexClient = mock(OpenAlexClient.class);
        OpenAlexWorkRepository openAlexWorkRepository = mock(OpenAlexWorkRepository.class);
        OpenAlexService service = new OpenAlexService(
                researcherRepository,
                openAlexClient,
                openAlexWorkRepository
        );
        UUID researcherId = UUID.randomUUID();
        Researcher researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Pesquisador sem obras")
                .build();

        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
        when(openAlexWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId))
                .thenReturn(List.of());

        OpenAlexAbstractSyncResponse response = service.syncAbstracts(researcherId);

        assertThat(response.totalFound()).isZero();
        assertThat(response.updatedWorks()).isZero();
        assertThat(response.worksWithAbstract()).isZero();
        verifyNoInteractions(openAlexClient);
    }
}
