package com.triacompany.academic.crossref;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CrossrefClient {

    @Value("${app.crossref.base-url}")
    private String crossrefBaseUrl;

    @Value("${app.crossref.mailto}")
    private String mailto;

    public JsonNode fetchWorkByDoi(String doi) {
        try {
            URI uri = URI.create(
                    crossrefBaseUrl
                            + "/works/"
                            + encode(doi)
                            + "?mailto="
                            + encode(mailto)
            );

            return restClient()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "DOI não encontrado no Crossref: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar o Crossref pelo DOI neste momento.");
        }
    }

    public JsonNode searchWorkByBibliographicTitle(String title) {
        try {
            URI uri = URI.create(
                    crossrefBaseUrl
                            + "/works?query.bibliographic="
                            + encode(title)
                            + "&rows=1"
                            + "&mailto="
                            + encode(mailto)
            );

            return restClient()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "Erro ao buscar metadados no Crossref: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível buscar metadados no Crossref neste momento.");
        }
    }

    public JsonNode searchSimilarJournalArticles(String bibliographicQuery, int rows) {
        if (bibliographicQuery == null || bibliographicQuery.isBlank()) {
            throw new IllegalArgumentException("O texto bibliográfico para busca é obrigatório.");
        }

        int normalizedRows = Math.max(1, Math.min(rows, 50));

        try {
            URI uri = URI.create(
                    crossrefBaseUrl
                            + "/works?query.bibliographic="
                            + encode(bibliographicQuery)
                            + "&filter=type:journal-article"
                            + "&rows="
                            + normalizedRows
                            + "&mailto="
                            + encode(mailto)
            );

            return restClient()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "Erro ao buscar publicações relacionadas no Crossref: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Não foi possível buscar publicações relacionadas no Crossref neste momento."
            );
        }
    }

    private RestClient restClient() {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader(
                        HttpHeaders.USER_AGENT,
                        "TRIA-Company-Academic-Optimization-Platform/1.0 (mailto:" + mailto + ")"
                )
                .build();
    }

    private String encode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
