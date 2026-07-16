package com.triacompany.academic.orcid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcidIdTest {

    @Test
    void formatsAValidDigitsOnlyIdentifier() {
        assertThat(OrcidId.normalize("0000000218250097"))
                .isEqualTo("0000-0002-1825-0097");
    }

    @Test
    void acceptsAFullOrcidUrl() {
        assertThat(OrcidId.normalize("https://orcid.org/0000-0002-1825-0097"))
                .isEqualTo("0000-0002-1825-0097");
    }

    @Test
    void rejectsAnInvalidChecksum() {
        assertThatThrownBy(() -> OrcidId.normalize("0000-0002-1825-0098"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dígito verificador");
    }
}
