package com.triacompany.academic.abstractanalysis;

import com.triacompany.academic.abstractanalysis.AbstractAnalysisEngine.AbstractEvidence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractAnalysisEngineTest {

    private final AbstractAnalysisEngine engine = new AbstractAnalysisEngine();

    @Test
    void extractsRecurringMultiwordThemesFromConfirmedAbstractEvidence() {
        List<AbstractThemeResponse> themes = engine.extractThemes(
                List.of(
                        new AbstractEvidence(
                                UUID.randomUUID(),
                                "A inteligência artificial apoia a qualidade da educação e a inovação pedagógica."
                        ),
                        new AbstractEvidence(
                                UUID.randomUUID(),
                                "Este estudo avalia inteligência artificial aplicada à qualidade da educação superior."
                        ),
                        new AbstractEvidence(
                                UUID.randomUUID(),
                                "A qualidade da educação depende também da formação de professores."
                        )
                ),
                "Tecnologias Educacionais",
                "inteligência artificial; qualidade da educação",
                "Zakeu A. Zengo"
        );

        assertThat(themes)
                .extracting(AbstractThemeResponse::theme)
                .contains("Inteligencia artificial", "Qualidade da educacao");
        assertThat(themes.getFirst().relevanceScore()).isBetween(0, 100);
    }

    @Test
    void ignoresResearcherNameAndGenericMetadataNoise() {
        List<AbstractThemeResponse> themes = engine.extractThemes(
                List.of(
                        new AbstractEvidence(
                                UUID.randomUUID(),
                                "Zakeu Zengo apresenta o artigo e os resultados da educação inclusiva."
                        ),
                        new AbstractEvidence(
                                UUID.randomUUID(),
                                "Zakeu Zengo analisa práticas de educação inclusiva em Angola."
                        )
                ),
                null,
                null,
                "Zakeu Zengo"
        );

        assertThat(themes)
                .extracting(AbstractThemeResponse::theme)
                .doesNotContain("Zakeu zengo");
    }

    @Test
    void returnsEmptyThemesWhenNoAbstractEvidenceExists() {
        assertThat(engine.extractThemes(List.of(), null, null, null)).isEmpty();
    }
}
