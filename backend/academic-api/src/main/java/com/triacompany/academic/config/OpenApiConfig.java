package com.triacompany.academic.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI academicPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Academic Optimization Platform API")
                        .description("""
                                API institucional da Plataforma de Otimização Acadêmica da TRIA Company.

                                Esta API permite cadastro de pesquisadores, perfis acadêmicos, autenticação JWT,
                                importação ORCID, curadoria OpenAlex, validação Crossref, relatórios PDF,
                                checklist Google Acadêmico e dashboard institucional.

                                Observação importante:
                                a plataforma não automatiza, não acessa e não altera dados diretamente no Google Acadêmico.
                                O módulo Google Acadêmico fornece apenas checklist e orientações manuais.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TRIA Company")
                                .email("geral@triacompany.com")
                                .url("https://triacompany.com"))
                        .license(new License()
                                .name("Proprietary - TRIA Company")))
                .servers(List.of(
                        new Server()
                                .url("https://academic-api.triacompany.com")
                                .description("Produção"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Ambiente local")
                ))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}