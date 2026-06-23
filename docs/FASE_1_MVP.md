# Fase 1 — MVP Seguro

## Visão geral

A Fase 1 do projeto tem como objetivo construir um MVP seguro, funcional e validável para professores, pesquisadores e universidades angolanas.

O foco desta fase é criar uma plataforma básica capaz de organizar dados acadêmicos, importar publicações de fontes confiáveis, validar informações e gerar um relatório inicial de otimização.

## Objetivo da Fase 1

Validar o produto com uma universidade-piloto e um grupo inicial de professores/pesquisadores.

## Funcionalidades principais

### 1. Cadastro de professores/pesquisadores

Permitir que professores e pesquisadores criem uma conta na plataforma.

Dados iniciais:

- Nome completo.
- E-mail.
- Telefone.
- Instituição.
- Departamento.
- Título acadêmico.
- País.
- ORCID, quando existir.

### 2. Perfil acadêmico

Cada pesquisador terá um perfil acadêmico com informações consolidadas.

O perfil deverá conter:

- Dados pessoais acadêmicos.
- Instituição.
- Departamento.
- Área de pesquisa.
- ORCID.
- Lista de publicações.
- Métricas básicas.
- Recomendações iniciais.

### 3. Importação via ORCID

A plataforma deverá permitir a conexão ou consulta de dados pelo ORCID do pesquisador.

Objetivos:

- Identificar o pesquisador.
- Importar obras vinculadas ao ORCID.
- Reduzir preenchimento manual.
- Melhorar confiabilidade dos dados.

### 4. Busca de publicações via OpenAlex

A plataforma deverá buscar publicações relacionadas ao pesquisador na base OpenAlex.

Objetivos:

- Encontrar artigos, livros, capítulos e demais produções acadêmicas.
- Cruzar nome, instituição e identificadores.
- Sugerir publicações para confirmação manual.

### 5. Validação via Crossref

A plataforma deverá usar o Crossref para validar DOI e metadados das publicações.

Objetivos:

- Verificar título.
- Verificar autores.
- Verificar DOI.
- Verificar ano de publicação.
- Verificar periódico ou editora.

### 6. Painel com publicações encontradas

O pesquisador deverá visualizar as publicações encontradas pelo sistema.

Cada publicação poderá ter os seguintes estados:

- Encontrada.
- Confirmada.
- Rejeitada.
- Necessita correção.

### 7. Confirmação manual

O sistema não deverá assumir automaticamente que toda publicação encontrada pertence ao pesquisador.

O pesquisador deverá confirmar, rejeitar ou corrigir os dados antes de a publicação entrar no perfil acadêmico consolidado.

### 8. Relatório básico de otimização

O sistema deverá gerar um relatório inicial com recomendações.

O relatório poderá incluir:

- Dados incompletos.
- ORCID ausente.
- Publicações sem DOI.
- Possíveis duplicidades.
- Inconsistência de nome.
- Inconsistência de afiliação.
- Sugestões para melhorar o perfil acadêmico.
- Recomendações para Google Acadêmico.

### 9. Exportação em PDF

O pesquisador deverá poder exportar o relatório básico em PDF.

O PDF servirá como documento de apoio para otimização do perfil acadêmico.

### 10. Checklist para Google Acadêmico

A plataforma deverá gerar um checklist guiado para que o pesquisador atualize manualmente o Google Acadêmico.

A plataforma não fará automação direta no Google Acadêmico.

O checklist poderá orientar sobre:

- Nome acadêmico padronizado.
- Afiliação institucional.
- E-mail institucional.
- Áreas de interesse.
- Publicações confirmadas.
- Remoção de duplicidades.
- Atualização de citações.
- Verificação de coautores.

### 11. Painel administrativo básico

A TRIA Company e a instituição-piloto deverão ter um painel administrativo básico.

O painel poderá conter:

- Lista de pesquisadores cadastrados.
- Quantidade de publicações encontradas.
- Quantidade de publicações confirmadas.
- Pesquisadores sem ORCID.
- Pesquisadores com perfil incompleto.
- Exportação básica de dados.

## Resultado esperado

Ao final da Fase 1, a plataforma deverá permitir:

- Cadastro de pesquisadores.
- Criação de perfil acadêmico.
- Consulta/importação via ORCID.
- Busca de publicações via OpenAlex.
- Validação de DOI via Crossref.
- Confirmação manual das publicações.
- Geração de relatório básico.
- Exportação em PDF.
- Geração de checklist para Google Acadêmico.
- Gestão básica pelo painel administrativo.

## Critério de sucesso

A Fase 1 será considerada concluída quando uma universidade-piloto conseguir cadastrar pesquisadores reais, localizar publicações, validar dados acadêmicos e gerar relatórios de otimização.