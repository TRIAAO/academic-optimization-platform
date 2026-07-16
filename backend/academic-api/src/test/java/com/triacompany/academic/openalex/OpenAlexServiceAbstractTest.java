package com.triacompany.academic.openalex;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
}
