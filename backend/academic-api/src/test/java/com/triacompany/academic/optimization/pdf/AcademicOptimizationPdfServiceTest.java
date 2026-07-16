package com.triacompany.academic.optimization.pdf;

import com.lowagie.text.pdf.PdfReader;
import com.triacompany.academic.optimization.AcademicOptimizationReportResponse;
import com.triacompany.academic.optimization.OptimizationRecommendationResponse;
import com.triacompany.academic.optimization.OptimizationScoreItemResponse;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcademicOptimizationPdfServiceTest {

    @Test
    void shouldRenderBrandedInstitutionalReport() throws Exception {
        AcademicOptimizationPdfService service = new AcademicOptimizationPdfService(null);
        byte[] pdf = service.renderReport(sampleReport());

        assertTrue(pdf.length > 50_000);
        assertNotNull(getClass().getResource("/branding/imetro-logo.png"));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertEquals(2, reader.getNumberOfPages());
        } finally {
            reader.close();
        }

        Path output = Path.of("target", "test-output", "academic-optimization-report-sample.pdf");
        Files.createDirectories(output.getParent());
        Files.write(output, pdf);
    }

    private AcademicOptimizationReportResponse sampleReport() {
        return new AcademicOptimizationReportResponse(
                UUID.randomUUID(),
                "Zakeu A. Zengo",
                "zakeu.zengo@universidade.ao",
                "Instituto Superior Politécnico Metropolitano de Angola",
                "Direção / Presidência",
                "Professor Doutor",
                "0009-0003-7543-6466",
                33,
                12,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                36,
                "CRITICO",
                "O pesquisador Zakeu A. Zengo possui pontuação geral de 36/100, classificada como CRÍTICO. "
                        + "Foram identificadas 12 obras no ORCID e ainda não existem obras confirmadas no OpenAlex ou DOIs confirmados no Crossref.",
                List.of(
                        new OptimizationScoreItemResponse("PROFILE_COMPLETION", "Completude do perfil acadêmico", 7, 20, "CRITICO", "Perfil acadêmico com 33% de preenchimento."),
                        new OptimizationScoreItemResponse("ORCID", "Presença e produção no ORCID", 20, 20, "EXCELENTE", "ORCID informado com 12 obras importadas."),
                        new OptimizationScoreItemResponse("OPENALEX", "Presença e curadoria no OpenAlex", 0, 20, "CRITICO", "Nenhuma obra OpenAlex confirmada."),
                        new OptimizationScoreItemResponse("CROSSREF", "Validação de DOI e metadados no Crossref", 0, 25, "CRITICO", "Ainda não existem validações Crossref."),
                        new OptimizationScoreItemResponse("INSTITUTIONAL_VISIBILITY", "Visibilidade institucional", 9, 15, "EM_OTIMIZACAO", "Avaliação de vínculo institucional, links acadêmicos e presença pública.")
                ),
                List.of(
                        new OptimizationRecommendationResponse("HIGH", "Perfil Acadêmico", "Completar biografia, área de pesquisa, palavras-chave, links institucionais e identificadores acadêmicos."),
                        new OptimizationRecommendationResponse("MEDIUM", "OpenAlex", "Buscar candidatos de autor no OpenAlex e importar obras apenas após aprovação institucional."),
                        new OptimizationRecommendationResponse("HIGH", "Crossref", "Validar as obras confirmadas no Crossref para verificar DOI, título, fonte e metadados bibliográficos."),
                        new OptimizationRecommendationResponse("MEDIUM", "Google Acadêmico", "Adicionar o link do Google Acadêmico ao perfil e revisar manualmente as publicações vinculadas.")
                ),
                LocalDateTime.of(2026, 7, 16, 11, 15)
        );
    }
}
