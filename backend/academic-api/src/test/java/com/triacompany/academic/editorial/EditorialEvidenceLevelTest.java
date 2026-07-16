package com.triacompany.academic.editorial;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EditorialEvidenceLevelTest {

    @Test
    void shouldAlignEvidenceLevelWithDisplayedRelevanceScore() {
        assertThat(EditorialEvidenceLevel.from(75, true)).isEqualTo("FORTE");
        assertThat(EditorialEvidenceLevel.from(51, true)).isEqualTo("MODERADA");
        assertThat(EditorialEvidenceLevel.from(44, true)).isEqualTo("INICIAL");
        assertThat(EditorialEvidenceLevel.from(90, false)).isEqualTo("SEM_EVIDENCIA");
    }
}
