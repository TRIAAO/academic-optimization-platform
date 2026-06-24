package com.triacompany.academic.orcid;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class OrcidClient {

    @Value("${app.orcid.base-url}")
    private String orcidBaseUrl;

    public JsonNode fetchWorks(String orcidId) {
        try {
            return RestClient.builder()
                    .baseUrl(orcidBaseUrl)
                    .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                    .defaultHeader(HttpHeaders.USER_AGENT, "TRIA-Company-Academic-Optimization-Platform/1.0")
                    .build()
                    .get()
                    .uri("/{orcidId}/works", orcidId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(
                    "Erro ao consultar ORCID: " + exception.getStatusCode() + " - " + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar o ORCID neste momento.");
        }
    }
}