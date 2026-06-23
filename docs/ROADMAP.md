# Roadmap do Projeto

## Fase 1 — MVP Seguro

Objetivo: validar a solução com pesquisadores e uma universidade-piloto.

### Módulo 1 — Base do backend e cadastro de pesquisadores

- Criar projeto Spring Boot.
- Configurar PostgreSQL.
- Configurar Flyway.
- Criar entidade Researcher.
- Criar endpoint de cadastro.
- Criar endpoint de listagem.
- Criar endpoint de busca por ID.

### Módulo 2 — Autenticação e segurança

- Criar entidade User.
- Criar login.
- Criar registro.
- Criptografar senha.
- Criar JWT.
- Criar perfis de acesso.
- Proteger rotas.

### Módulo 3 — Perfil acadêmico

- Criar entidade AcademicProfile.
- Vincular perfil ao pesquisador.
- Criar endpoints de criação e atualização.
- Criar tela futura de perfil acadêmico.

### Módulo 4 — Integração ORCID

- Criar serviço de consulta ORCID.
- Consultar obras por ORCID.
- Salvar logs de importação.
- Permitir importação manual inicial.

### Módulo 5 — Busca OpenAlex

- Criar serviço de busca OpenAlex.
- Buscar publicações por nome.
- Buscar publicações por ORCID.
- Buscar publicações por instituição.
- Sugerir publicações encontradas.

### Módulo 6 — Validação Crossref

- Criar serviço Crossref.
- Validar DOI.
- Validar metadados.
- Atualizar status da publicação.

### Módulo 7 — Gestão de publicações

- Criar entidade Publication.
- Criar estados da publicação.
- Confirmar publicação.
- Rejeitar publicação.
- Corrigir publicação.
- Listar publicações por pesquisador.

### Módulo 8 — Painel básico de métricas

- Total de publicações.
- Total confirmadas.
- Total pendentes.
- Total rejeitadas.
- Total com DOI.
- Total sem DOI.

### Módulo 9 — Relatório básico de otimização

- Gerar recomendações básicas.
- Identificar perfil incompleto.
- Identificar ausência de ORCID.
- Identificar publicações sem DOI.
- Identificar duplicidades.
- Identificar inconsistências.

### Módulo 10 — Exportação em PDF

- Criar template de relatório.
- Gerar PDF.
- Disponibilizar download.

### Módulo 11 — Checklist Google Acadêmico

- Criar checklist.
- Gerar itens automaticamente.
- Permitir marcação manual.
- Orientar atualização segura.

### Módulo 12 — Frontend do MVP

- Criar projeto React.
- Criar layout base.
- Criar login.
- Criar cadastro.
- Criar dashboard.
- Criar perfil acadêmico.
- Criar publicações.
- Criar relatório.
- Criar checklist.

## Fase 2 — IA e análise acadêmica

Objetivo: adicionar inteligência e recomendações avançadas.

Funcionalidades previstas:

- Sugestão de keywords.
- Análise de abstracts.
- Tradução PT-EN.
- Sugestão de periódicos.
- Sugestão de coautores.
- Comparação com pares.
- Alertas.
- Relatórios mensais.

## Fase 3 — SaaS comercial

Objetivo: transformar a solução em produto comercial B2C/B2B.

Funcionalidades previstas:

- Planos gratuitos e pagos.
- Assinaturas.
- Pagamentos.
- Painel institucional avançado.
- Suporte WhatsApp.
- Onboarding universitário.
- White-label.
- Marketplace de revisão e tradução acadêmica.