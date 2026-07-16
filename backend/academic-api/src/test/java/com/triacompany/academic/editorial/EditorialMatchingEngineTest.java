package com.triacompany.academic.editorial;

import com.triacompany.academic.editorial.EditorialMatchingEngine.SimilarWork;
import com.triacompany.academic.editorial.EditorialMatchingEngine.TargetWork;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EditorialMatchingEngineTest {

    private final EditorialMatchingEngine engine = new EditorialMatchingEngine();

    @Test
    void shouldAggregateAndRankJournalsWithExplainableEvidence() {
        TargetWork target = new TargetWork(
                "Empreendedorismo e inovação nas universidades angolanas",
                "A investigação analisa inovação, educação superior e empreendedorismo em Angola."
        );

        List<EditorialJournalRecommendationResponse> result = engine.match(
                target,
                List.of(
                        new SimilarWork(
                                "Inovação e empreendedorismo na educação superior",
                                "Revista de Educação e Inovação",
                                "Editora Acadêmica",
                                List.of("1234-5678"),
                                "10.1000/alpha",
                                "https://doi.org/10.1000/alpha",
                                20
                        ),
                        new SimilarWork(
                                "Universidades e inovação em Angola",
                                "Revista de Educação e Inovação",
                                "Editora Acadêmica",
                                List.of("1234-5678"),
                                "10.1000/beta",
                                "https://doi.org/10.1000/beta",
                                8
                        ),
                        new SimilarWork(
                                "Agricultural soil chemistry",
                                "Unrelated Journal",
                                "Other Publisher",
                                List.of(),
                                null,
                                null,
                                2
                        )
                ),
                Set.of("Revista de Educação e Inovação")
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).journalName()).isEqualTo("Revista de Educação e Inovação");
        assertThat(result.get(0).relatedWorks()).isEqualTo(2);
        assertThat(result.get(0).presentInResearcherHistory()).isTrue();
        assertThat(result.get(0).relevanceScore()).isGreaterThanOrEqualTo(75);
        assertThat(result.get(0).rationale()).contains("bibliograficamente próxima");
    }
}
