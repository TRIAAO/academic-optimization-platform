package com.triacompany.academic.openalex;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAlexAuthorIdTest {

    @Test
    void normalizesAFullAuthorUrl() {
        assertThat(OpenAlexAuthorId.normalize("https://openalex.org/A5014082506"))
                .isEqualTo("A5014082506");
    }

    @Test
    void acceptsALowercaseShortIdentifier() {
        assertThat(OpenAlexAuthorId.normalize("a5014082506"))
                .isEqualTo("A5014082506");
    }

    @Test
    void rejectsAnInvalidIdentifier() {
        assertThatThrownBy(() -> OpenAlexAuthorId.normalize("W5014082506"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }
}
