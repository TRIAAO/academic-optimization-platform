package com.triacompany.academic.openalex;

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
public class OpenAlexClient {

    @Value("${app.openalex.base-url}")
    private String openAlexBaseUrl;

    @Value("${app.openalex.mailto}")
    private String mailto;

    public JsonNode fetchAuthorById(String openAlexAuthorId) {
        String normalizedAuthorId = OpenAlexAuthorId.normalize(openAlexAuthorId);

        try {
            URI uri = URI.create(
                    openAlexBaseUrl
                            + "/authors/"
                            + encode(normalizedAuthorId)
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
                    "Autor OpenAlex não encontrado: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar o autor OpenAlex neste momento.");
        }
    }

    public JsonNode fetchAuthorByOrcid(String orcidId) {
        try {
            String encodedOrcidUrl = URLEncoder.encode(
                    "https://orcid.org/" + orcidId,
                    StandardCharsets.UTF_8
            );

            URI uri = URI.create(
                    openAlexBaseUrl
                            + "/authors/"
                            + encodedOrcidUrl
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
                    "Autor não encontrado no OpenAlex para o ORCID informado: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar o autor no OpenAlex neste momento.");
        }
    }

    public JsonNode searchAuthorCandidatesByName(String authorName) {
        try {
            URI uri = URI.create(
                    openAlexBaseUrl
                            + "/authors?search="
                            + encode(authorName)
                            + "&per-page=10"
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
                    "Erro ao buscar candidatos de autor no OpenAlex: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível buscar candidatos de autor no OpenAlex neste momento.");
        }
    }

    public JsonNode searchWorksByAuthorId(String openAlexAuthorShortId) {
        try {
            /*
             * Importante:
             * Aqui montamos a URL manualmente para evitar que o filtro do OpenAlex
             * seja codificado de forma incompatível.
             *
             * Formato esperado:
             * /works?filter=authorships.author.id:A5014082506
             */
            URI uri = URI.create(
                    openAlexBaseUrl
                            + "/works?filter=authorships.author.id:"
                            + openAlexAuthorShortId
                            + "&per-page=100"
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
                    "Erro ao consultar obras do autor no OpenAlex: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar as obras do autor no OpenAlex neste momento.");
        }
    }

    public JsonNode fetchWorkById(String openAlexWorkId) {
        String normalizedWorkId = normalizeOpenAlexWorkId(openAlexWorkId);

        if (normalizedWorkId == null) {
            throw new IllegalArgumentException("O identificador da obra OpenAlex é obrigatório.");
        }

        try {
            URI uri = URI.create(
                    openAlexBaseUrl
                            + "/works/"
                            + encode(normalizedWorkId)
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
                    "Obra não encontrada no OpenAlex: "
                            + exception.getStatusCode()
                            + " - "
                            + exception.getResponseBodyAsString()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException("Não foi possível consultar a obra no OpenAlex neste momento.");
        }
    }

    public JsonNode searchWorksByAuthorName(String authorName) {
        try {
            URI uri = URI.create(
                    openAlexBaseUrl
                            + "/works?search="
                            + encode(authorName)
                            + "&per-page=25"
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
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader(HttpHeaders.USER_AGENT, "TRIA-Company-Academic-Optimization-Platform/1.0")
                .build();
    }

    private String encode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String normalizeOpenAlexWorkId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value
                .replace("https://openalex.org/", "")
                .replace("http://openalex.org/", "")
                .trim();

        return normalized.isBlank() ? null : normalized;
    }
}
