package com.triacompany.academic.deduplication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BibliographicSimilarityEngineTest {

    private final BibliographicSimilarityEngine engine = new BibliographicSimilarityEngine();

    @Test
    void shouldReturnExactCandidateWhenDoiMatches() {
        BibliographicSimilarityEngine.Result result = engine.compare(
                "Ciência e inovação em Angola",
                "https://doi.org/10.1234/ABC.2026.1",
                2026,
                "Science and innovation in Angola",
                "10.1234/abc.2026.1",
                2026
        );

        assertTrue(result.candidate());
        assertTrue(result.doiExactMatch());
        assertEquals(100, result.similarityScore());
    }

    @Test
    void shouldDetectApproximateTitleCandidateWithCompatibleYear() {
        BibliographicSimilarityEngine.Result result = engine.compare(
                "Educação superior e transformação digital em Angola",
                null,
                2025,
                "Transformação digital na educação superior de Angola",
                null,
                2025
        );

        assertTrue(result.candidate());
        assertFalse(result.doiExactMatch());
        assertTrue(result.publicationYearCompatible());
        assertTrue(result.titleSimilarity() >= 65);
        assertTrue(result.similarityScore() >= 72);
    }

    @Test
    void shouldPenalizeDifferentDoiAndDistantPublicationYear() {
        BibliographicSimilarityEngine.Result result = engine.compare(
                "Gestão universitária e qualidade acadêmica",
                "10.1000/one",
                2014,
                "Gestão universitária e qualidade académica",
                "10.1000/two",
                2026
        );

        assertFalse(result.doiExactMatch());
        assertFalse(result.publicationYearCompatible());
        assertFalse(result.candidate());
        assertTrue(result.similarityScore() < 72);
    }

    @Test
    void shouldNormalizeAccentsAndDoiPrefixes() {
        assertEquals(
                "producao cientifica em angola",
                engine.normalizeTitle("Produção Científica em Angola")
        );
        assertEquals(
                "10.5555/example",
                engine.normalizeDoi("DOI: https://doi.org/10.5555/EXAMPLE")
        );
    }
}
