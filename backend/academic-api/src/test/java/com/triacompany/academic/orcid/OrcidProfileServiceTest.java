package com.triacompany.academic.orcid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triacompany.academic.researcher.ResearcherRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrcidProfileServiceTest {

    @Test
    void resolvesAnAuthorFromADigitsOnlyOrcid() throws Exception {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        OrcidClient orcidClient = mock(OrcidClient.class);
        OrcidProfileService service = new OrcidProfileService(researcherRepository, orcidClient);
        var record = new ObjectMapper().readTree("""
                {
                  "person": {
                    "name": {
                      "given-names": { "value": "Ana" },
                      "family-name": { "value": "Silva" }
                    },
                    "keywords": {
                      "keyword": [
                        { "content": "Educação" },
                        { "content": "Tecnologia" }
                      ]
                    }
                  },
                  "activities-summary": {
                    "works": {
                      "group": [{}, {}, {}]
                    }
                  }
                }
                """);

        when(orcidClient.fetchRecord("0000-0002-1825-0097")).thenReturn(record);

        OrcidProfileSummaryResponse response = service.findSummaryByOrcidId("0000000218250097");

        assertThat(response.orcidId()).isEqualTo("0000-0002-1825-0097");
        assertThat(response.displayName()).isEqualTo("Ana Silva");
        assertThat(response.keywords()).containsExactly("Educação", "Tecnologia");
        assertThat(response.worksCount()).isEqualTo(3);
        verify(orcidClient).fetchRecord("0000-0002-1825-0097");
    }
}
