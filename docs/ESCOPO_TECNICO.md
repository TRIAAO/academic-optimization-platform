# Escopo Técnico

## Projeto

Plataforma de Otimização Acadêmica para Pesquisadores e Universidades Angolanas.

## Empresa executora

TRIA Company.

## Objetivo técnico

Construir uma aplicação web segura, escalável e preparada para evoluir de MVP para produto SaaS.

## Arquitetura inicial

A arquitetura inicial será composta por:

- Frontend web.
- Backend API REST.
- Banco de dados PostgreSQL.
- Integrações externas com APIs acadêmicas.
- Módulo de geração de PDF.
- Painel administrativo.

## Backend

### Tecnologia

- Java 21.
- Spring Boot.
- Maven.
- PostgreSQL.
- Flyway.
- Spring Data JPA.
- Spring Security.
- JWT.

### Responsabilidades

O backend será responsável por:

- Autenticação.
- Cadastro de pesquisadores.
- Gestão de perfis acadêmicos.
- Gestão de publicações.
- Integração com ORCID.
- Integração com OpenAlex.
- Integração com Crossref.
- Geração de relatórios.
- Exportação de PDF.
- Gestão administrativa.

## Frontend

### Tecnologia

- React.
- Vite.
- Tailwind CSS.

### Responsabilidades

O frontend será responsável por:

- Tela inicial.
- Cadastro.
- Login.
- Dashboard do pesquisador.
- Perfil acadêmico.
- Tela de publicações encontradas.
- Tela de confirmação manual.
- Tela de relatório.
- Checklist Google Acadêmico.
- Painel administrativo.

## Banco de dados

### Tecnologia

PostgreSQL.

### Entidades previstas

- User.
- Researcher.
- AcademicProfile.
- Publication.
- PublicationSource.
- OrcidImportLog.
- OpenAlexSearchLog.
- CrossrefValidationLog.
- OptimizationReport.
- GoogleScholarChecklist.
- AdminUser.

## Integrações

### ORCID

Uso previsto:

- Consultar identificador do pesquisador.
- Importar obras associadas.
- Apoiar consolidação do perfil acadêmico.

### OpenAlex

Uso previsto:

- Buscar publicações acadêmicas.
- Encontrar autores.
- Cruzar dados de nome, instituição e áreas de pesquisa.

### Crossref

Uso previsto:

- Validar DOI.
- Validar metadados de publicação.
- Confirmar título, autores, ano e periódico.

## Segurança

A plataforma deverá considerar:

- Autenticação via JWT.
- Senhas criptografadas.
- Controle de acesso por perfil.
- Separação entre pesquisador, administrador e instituição.
- Validação de dados.
- Registro de logs importantes.
- Proteção contra acessos indevidos.

## Perfis de acesso

### Pesquisador

Pode:

- Criar conta.
- Editar perfil acadêmico.
- Conectar ORCID.
- Buscar publicações.
- Confirmar ou rejeitar publicações.
- Gerar relatório.
- Exportar PDF.
- Acessar checklist.

### Administrador TRIA

Pode:

- Visualizar pesquisadores.
- Visualizar instituições.
- Acompanhar estatísticas.
- Apoiar validação e suporte.

### Instituição

Pode:

- Acompanhar pesquisadores vinculados.
- Visualizar métricas agregadas.
- Exportar dados institucionais básicos.

## Princípio importante

A plataforma não deverá automatizar preenchimento direto no Google Acadêmico.

O sistema deverá orientar o pesquisador por meio de checklist, relatório e recomendações.