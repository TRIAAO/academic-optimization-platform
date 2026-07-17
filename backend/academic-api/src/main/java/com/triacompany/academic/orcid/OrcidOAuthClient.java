package com.triacompany.academic.orcid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Component
public class OrcidOAuthClient {

    private final boolean enabled;
    private final String authorizationUrl;
    private final String tokenUrl;
    private final String revokeUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String frontendRedirectUri;
    private final String scope;
    private final RestClient restClient;

    public OrcidOAuthClient(
            @Value("${app.orcid.oauth.enabled:false}") boolean enabled,
            @Value("${app.orcid.oauth.authorization-url:https://orcid.org/oauth/authorize}") String authorizationUrl,
            @Value("${app.orcid.oauth.token-url:https://orcid.org/oauth/token}") String tokenUrl,
            @Value("${app.orcid.oauth.revoke-url:https://orcid.org/oauth/revoke}") String revokeUrl,
            @Value("${app.orcid.oauth.client-id:}") String clientId,
            @Value("${app.orcid.oauth.client-secret:}") String clientSecret,
            @Value("${app.orcid.oauth.redirect-uri:}") String redirectUri,
            @Value("${app.orcid.oauth.frontend-redirect-uri:}") String frontendRedirectUri,
            @Value("${app.orcid.oauth.scope:/authenticate}") String scope,
            RestClient.Builder restClientBuilder
    ) {
        this.enabled = enabled;
        this.authorizationUrl = normalize(authorizationUrl);
        this.tokenUrl = normalize(tokenUrl);
        this.revokeUrl = normalize(revokeUrl);
        this.clientId = normalize(clientId);
        this.clientSecret = normalize(clientSecret);
        this.redirectUri = normalize(redirectUri);
        this.frontendRedirectUri = normalize(frontendRedirectUri);
        this.scope = normalize(scope);
        this.restClient = restClientBuilder.build();
    }

    public boolean isConfigured() {
        return enabled
                && hasText(authorizationUrl)
                && hasText(tokenUrl)
                && hasText(revokeUrl)
                && hasText(clientId)
                && hasText(clientSecret)
                && hasText(redirectUri)
                && hasText(frontendRedirectUri)
                && hasText(scope);
    }

    public boolean isSandbox() {
        return authorizationUrl.contains("sandbox.orcid.org");
    }

    public String scope() {
        return scope;
    }

    public String frontendRedirectUri() {
        return frontendRedirectUri;
    }

    public String buildAuthorizationUrl(String state) {
        requireConfigured();

        return UriComponentsBuilder.fromUriString(authorizationUrl)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("scope", scope)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    public OrcidOAuthTokenResponse exchangeAuthorizationCode(String code) {
        requireConfigured();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        try {
            OrcidOAuthTokenResponse response = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(OrcidOAuthTokenResponse.class);

            if (response == null
                    || !hasText(response.accessToken())
                    || !hasText(response.orcid())) {
                throw new OrcidOAuthException(
                        "oauth_token_invalid",
                        "O ORCID não devolveu um identificador autenticado válido."
                );
            }

            return response;
        } catch (OrcidOAuthException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw new OrcidOAuthException(
                    "oauth_token_exchange_failed",
                    "O ORCID recusou a troca do código de autorização.",
                    exception
            );
        } catch (RuntimeException exception) {
            throw new OrcidOAuthException(
                    "oauth_token_exchange_failed",
                    "Não foi possível concluir a autenticação com o ORCID.",
                    exception
            );
        }
    }

    public void revoke(String token) {
        if (!isConfigured() || !hasText(token)) {
            return;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("token", token);

        try {
            restClient.post()
                    .uri(revokeUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new OrcidOAuthException(
                    "oauth_revocation_failed",
                    "Não foi possível revogar a autorização no ORCID.",
                    exception
            );
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new OrcidOAuthException(
                    "oauth_not_configured",
                    "A integração OAuth ORCID ainda não está configurada neste ambiente."
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
