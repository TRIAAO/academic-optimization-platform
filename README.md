# Plataforma de Otimização Acadêmica

Plataforma de Otimização Acadêmica para Pesquisadores e Universidades Angolanas.

Projeto executado pela **TRIA Company**, com foco em organização, otimização e aumento da visibilidade acadêmica de professores, pesquisadores e instituições de ensino superior.

## Objetivo

Criar uma plataforma digital segura para permitir que professores e pesquisadores cadastrem seus dados acadêmicos, importem publicações de fontes confiáveis, validem metadados, acompanhem indicadores e gerem relatórios de otimização acadêmica.

A plataforma não realiza automação irregular no Google Acadêmico. O sistema prepara, organiza e orienta o pesquisador por meio de relatórios e checklist guiado, mantendo o controle final com o próprio usuário.

## Fase atual

**Fase 1 — MVP Seguro**

Esta fase tem como objetivo validar o produto com professores, pesquisadores e uma universidade-piloto.

## Módulos da Fase 1

1. Cadastro de professores/pesquisadores.
2. Perfil acadêmico.
3. Importação via ORCID.
4. Busca de publicações via OpenAlex.
5. Validação de publicações e DOI via Crossref.
6. Painel com publicações encontradas.
7. Confirmação manual de publicações.
8. Relatório básico de otimização.
9. Exportação em PDF.
10. Checklist para Google Acadêmico.
11. Painel administrativo básico.

## Stack prevista

### Backend

- Java 21
- Spring Boot
- PostgreSQL
- Spring Security
- JWT
- Flyway
- Maven

### Frontend

- React
- Vite
- Tailwind CSS

### Banco de dados

- PostgreSQL

### Integrações

- ORCID
- OpenAlex
- Crossref

## Estrutura do projeto

```text
academic-optimization-platform/
├── backend/
├── frontend/
├── docs/
├── infra/
├── README.md
└── .gitignore