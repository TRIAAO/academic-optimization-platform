package com.triacompany.academic.status;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemStatusService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.name:Academic Optimization Platform}")
    private String applicationName;

    @Value("${app.company:TRIA Company}")
    private String company;

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${spring.profiles.active:production}")
    private String environment;

    @Value("${app.orcid.base-url:https://pub.orcid.org/v3.0}")
    private String orcidBaseUrl;

    @Value("${app.openalex.base-url:https://api.openalex.org}")
    private String openAlexBaseUrl;

    @Value("${app.crossref.base-url:https://api.crossref.org}")
    private String crossrefBaseUrl;

    public SystemStatusResponse getSystemStatus() {
        List<SystemComponentStatusResponse> components = new ArrayList<>();

        components.add(checkApi());
        components.add(checkDatabase());
        components.add(checkExternalService("ORCID", orcidBaseUrl));
        components.add(checkExternalService("OpenAlex", openAlexBaseUrl));
        components.add(checkExternalService("Crossref", crossrefBaseUrl));

        String overallStatus = resolveOverallStatus(components);

        return new SystemStatusResponse(
                applicationName,
                company,
                version,
                environment,
                overallStatus,
                uptimeSeconds(),
                components,
                LocalDateTime.now()
        );
    }

    private SystemComponentStatusResponse checkApi() {
        return new SystemComponentStatusResponse(
                "API",
                "UP",
                "Aplicação Spring Boot em execução.",
                0L
        );
    }

    private SystemComponentStatusResponse checkDatabase() {
        long startedAt = System.currentTimeMillis();

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long duration = System.currentTimeMillis() - startedAt;

            if (result != null && result == 1) {
                return new SystemComponentStatusResponse(
                        "PostgreSQL",
                        "UP",
                        "Banco de dados respondendo corretamente.",
                        duration
                );
            }

            return new SystemComponentStatusResponse(
                    "PostgreSQL",
                    "DEGRADED",
                    "Banco respondeu, mas retornou resultado inesperado.",
                    duration
            );
        } catch (Exception exception) {
            long duration = System.currentTimeMillis() - startedAt;

            return new SystemComponentStatusResponse(
                    "PostgreSQL",
                    "DOWN",
                    "Falha ao consultar o banco de dados.",
                    duration
            );
        }
    }

    private SystemComponentStatusResponse checkExternalService(String name, String baseUrl) {
        long startedAt = System.currentTimeMillis();

        try {
            RestClient.builder()
                    .build()
                    .get()
                    .uri(baseUrl)
                    .retrieve()
                    .toBodilessEntity();

            long duration = System.currentTimeMillis() - startedAt;

            return new SystemComponentStatusResponse(
                    name,
                    "UP",
                    "Serviço externo respondendo.",
                    duration
            );
        } catch (Exception exception) {
            long duration = System.currentTimeMillis() - startedAt;

            return new SystemComponentStatusResponse(
                    name,
                    "DEGRADED",
                    "Não foi possível confirmar resposta do serviço externo neste momento.",
                    duration
            );
        }
    }

    private String resolveOverallStatus(List<SystemComponentStatusResponse> components) {
        boolean hasDown = components.stream()
                .anyMatch(component -> "DOWN".equalsIgnoreCase(component.status()));

        if (hasDown) {
            return "DOWN";
        }

        boolean hasDegraded = components.stream()
                .anyMatch(component -> "DEGRADED".equalsIgnoreCase(component.status()));

        if (hasDegraded) {
            return "DEGRADED";
        }

        return "UP";
    }

    private Long uptimeSeconds() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        return uptimeMillis / 1000;
    }
}