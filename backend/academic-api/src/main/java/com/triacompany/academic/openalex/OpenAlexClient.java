package com.triacompany.academic.openalex;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class OpenAlexClient {

    @Value("${app.openalex.base-url}")
    private String openAlexBaseUrl;

    @Value("${app.openalex.mailto}")
    private String mailto;

    public JsonNode fetchAuthorByOrcid(String orcidId) {
        try {
            return restClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/authors/https://orcid.org/" + orcidId)
                            .queryParam("mailto", mailto)
                            .build()
                    )
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "Autor não encontrado no OpenAlex para o ORCID informado: "
                            + exception.getStatusCode()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar o autor no OpenAlex neste momento.");
        }
    }

    public JsonNode searchAuthorCandidatesByName(String authorName) {
        try {
            return restClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/authors")
                            .queryParam("search", authorName)
                            .queryParam("per-page", 10)
                            .queryParam("mailto", mailto)
                            .build()
                    )
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "Erro ao buscar candidatos de autor no OpenAlex: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível buscar candidatos de autor no OpenAlex neste momento.");
        }
    }

    public JsonNode searchWorksByAuthorId(String openAlexAuthorId) {
        try {
            return restClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/works")
                            .queryParam("filter", "authorships.author.id:" + openAlexAuthorId)
                            .queryParam("sort", "-publication_date")
                            .queryParam("per-page", 100)
                            .queryParam("mailto", mailto)
                            .build()
                    )
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "Erro ao consultar obras do autor no OpenAlex: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar as obras do autor no OpenAlex neste momento.");
        }
    }

    /**
     * Método antigo mantido apenas como fallback técnico.
     * A importação oficial deve usar Author ID aprovado pelo administrador.
     */
    public JsonNode searchWorksByAuthorName(String authorName) {
        try {
            return restClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/works")
                            .queryParam("search", authorName)
                            .queryParam("per-page", 25)
                            .queryParam("mailto", mailto)
                            .build()
                    )
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "Erro ao consultar OpenAlex por nome: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar o OpenAlex por nome neste momento.");
        }
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl(openAlexBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader(HttpHeaders.USER_AGENT, "TRIA-Company-Academic-Optimization-Platform/1.0")
                .build();
    }
}