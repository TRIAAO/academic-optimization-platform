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
                    "Erro ao consultar OpenAlex: " + exception.getStatusCode() + " - " + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar o OpenAlex neste momento.");
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