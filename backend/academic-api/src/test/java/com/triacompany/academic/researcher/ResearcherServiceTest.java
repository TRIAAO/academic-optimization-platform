package com.triacompany.academic.researcher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResearcherServiceTest {

    @Test
    void storesTheOrcidInItsCanonicalFormat() {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        ResearcherService service = new ResearcherService(researcherRepository);
        CreateResearcherRequest request = new CreateResearcherRequest(
                "Ana Silva",
                "ana.silva@universidade.ao",
                null,
                "Universidade Metropolitana de Angola",
                null,
                null,
                "0000000218250097"
        );

        when(researcherRepository.existsByEmailIgnoreCase("ana.silva@universidade.ao"))
                .thenReturn(false);
        when(researcherRepository.save(any(Researcher.class))).thenAnswer(invocation -> {
            Researcher researcher = invocation.getArgument(0);
            researcher.prePersist();
            return researcher;
        });

        ResearcherResponse response = service.create(request);

        assertThat(response.orcidId()).isEqualTo("0000-0002-1825-0097");
    }
}
