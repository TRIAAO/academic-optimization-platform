# Regras determinísticas da Auditoria Cientométrica

Este módulo transforma as medições cientométricas registadas manualmente em um diagnóstico explicável. Nenhuma regra altera automaticamente dados do Google Acadêmico, ORCID, OpenAlex, Crossref ou qualquer perfil externo.

## 1. Coerência H-index × D-index

A diferença percentual é calculada com o H-index total como referência:

```text
desvio = |H-index - D-index| / max(H-index, 1) × 100
```

Classificação:

- `ALIGNED`: até 15%;
- `ATTENTION`: acima de 15% e até 30%;
- `CRITICAL`: acima de 30%;
- `NOT_AVAILABLE`: H-index total ou D-index não informado.

Desvios acima de 30% geram alerta para revisão das áreas de interesse, classificação disciplinar e possíveis obras fora do escopo.

## 2. Vitalidade dos últimos 6 anos

Para cada indicador disponível, calcula-se a proporção recente:

```text
proporção recente = indicador dos últimos 6 anos / indicador total × 100
```

O score de vitalidade usa pesos fixos e transparentes:

- citações: 50%;
- H-index: 30%;
- i10-index: 20%.

Quando algum indicador não está disponível, os pesos restantes são normalizados.

Classificação:

- `ACTIVE`: 60 a 100;
- `RECOVERING`: 35 a 59;
- `STAGNANT`: 0 a 34;
- `NOT_AVAILABLE`: sem pares total/recente suficientes.

A comparação com a medição anterior também identifica:

- `GROWING`: pelo menos um indicador acumulado cresceu e nenhum diminuiu;
- `STABLE`: indicadores acumulados sem alteração;
- `REVIEW`: pelo menos um indicador acumulado diminuiu;
- `NO_HISTORY`: apenas uma medição disponível.

## 3. E-mail institucional

O domínio do e-mail verificado é comparado com uma lista institucional configurada no ambiente.

Variável de produção:

```env
INSTITUTIONAL_EMAIL_DOMAINS=universidade.ao,imetroangola.com
```

O serviço também aceita subdomínios dos domínios oficiais.

Classificação:

- `VERIFIED`: domínio reconhecido e verificação humana confirmada;
- `DOMAIN_MATCH_PENDING`: domínio reconhecido, mas confirmação ainda pendente;
- `NON_INSTITUTIONAL`: domínio fora da lista oficial;
- `INCONSISTENT`: marcado como institucional, porém com domínio não reconhecido;
- `NOT_INFORMED`: e-mail não informado.

## 4. Governança

- todos os alertas incluem código, severidade, explicação e ação recomendada;
- os resultados são calculados sob demanda a partir do histórico existente;
- não há nova tabela ou migração nesta etapa;
- a decisão final continua humana e institucional;
- a lista de domínios deve ser mantida pela instituição responsável.
