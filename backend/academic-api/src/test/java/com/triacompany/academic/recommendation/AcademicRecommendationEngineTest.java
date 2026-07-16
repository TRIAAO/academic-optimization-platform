package com.triacompany.academic.recommendation;

import com.triacompany.academic.recommendation.AcademicRecommendationEngine.CollaboratorEvidence;
import com.triacompany.academic.recommendation.AcademicRecommendationEngine.RecommendationContext;
import com.triacompany.academic.recommendation.AcademicRecommendationEngine.WorkEvidence;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AcademicRecommendationEngineTest {

    private final AcademicRecommendationEngine engine = new AcademicRecommendationEngine();

    @Test
    void generatesAuditableRecommendationsFromConsolidatedEvidence() {
        UUID researcherId = UUID.randomUUID();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 7, 16, 10, 30);

        RecommendationContext context = new RecommendationContext(
                researcherId,
                "Zakeu A. Zengo",
                "Inteligência Artificial na Educação",
                "aprendizagem adaptativa; ciência de dados",
                "tecnologia educacional, ensino superior",
                100,
                12,
                10,
                10,
                5,
                true,
                true,
                List.of(
                        new WorkEvidence(
                                "Inteligência artificial e aprendizagem adaptativa no ensino superior",
                                "Journal of Educational Technology",
                                36,
                                true,
                                true
                        ),
                        new WorkEvidence(
                                "Aprendizagem adaptativa baseada em inteligência artificial",
                                "Journal of Educational Technology",
                                18,
                                true,
                                true
                        ),
                        new WorkEvidence(
                                "Ciência de dados aplicada à avaliação educacional",
                                "African Journal of Higher Education",
                                7,
                                false,
                                false
                        )
                ),
                List.of(
                        new CollaboratorEvidence(
                                "Ana Manuel",
                                "https://openalex.org/A123",
                                "https://orcid.org/0000-0001-2345-6789",
                                "IMETRO",
                                3,
                                42,
                                2026
                        )
                ),
                generatedAt
        );

        AcademicRecommendationResponse response = engine.generate(context);

        assertThat(response.researcherId()).isEqualTo(researcherId);
        assertThat(response.evidenceLevel()).isEqualTo("STRONG");
        assertThat(response.generatedAt()).isEqualTo(generatedAt);
        assertThat(response.keywords())
                .extracting(KeywordRecommendationResponse::keyword)
                .anyMatch(keyword -> keyword.toLowerCase().contains("aprendizagem adaptativa"));
        assertThat(response.collaborators())
                .extracting(CollaboratorRecommendationResponse::displayName)
                .containsExactly("Ana Manuel");
        assertThat(response.journals().getFirst().journalName())
                .isEqualTo("Journal of Educational Technology");
        assertThat(response.methodology()).contains("determinística", "decisão humana");
        assertThat(response.googleScholarPolicy()).contains("exclusivamente manual");
    }

    @Test
    void returnsImprovementActionsWhenEvidenceIsInitial() {
        RecommendationContext context = new RecommendationContext(
                UUID.randomUUID(),
                "Pesquisador sem consolidação",
                null,
                null,
                null,
                20,
                0,
                0,
                0,
                0,
                false,
                false,
                List.of(),
                List.of(),
                LocalDateTime.now()
        );

        AcademicRecommendationResponse response = engine.generate(context);

        assertThat(response.evidenceLevel()).isEqualTo("INITIAL");
        assertThat(response.keywords()).isEmpty();
        assertThat(response.collaborators()).isEmpty();
        assertThat(response.journals()).isEmpty();
        assertThat(response.nextActions())
                .extracting(AcademicRecommendationActionResponse::area)
                .contains("PERFIL_ACADEMICO", "ORCID", "OPENALEX", "METRICAS");
    }

    @Test
    void excludesResearcherNameFromKeywordRecommendations() {
        RecommendationContext context = new RecommendationContext(
                UUID.randomUUID(),
                "Zakeu A. Zengo",
                null,
                null,
                null,
                40,
                2,
                0,
                0,
                0,
                false,
                false,
                List.of(
                        new WorkEvidence(
                                "ZENGO Zakeu: inteligência artificial aplicada à educação",
                                null,
                                0,
                                false,
                                false
                        ),
                        new WorkEvidence(
                                "ZENGO Zakeu: inovação tecnológica no ensino superior",
                                null,
                                0,
                                false,
                                false
                        )
                ),
                List.of(),
                LocalDateTime.now()
        );

        AcademicRecommendationResponse response = engine.generate(context);

        assertThat(response.keywords())
                .extracting(KeywordRecommendationResponse::keyword)
                .allSatisfy(keyword -> assertThat(keyword.toLowerCase())
                        .doesNotContain("zakeu", "zengo"));
    }
}
