# Configuração do OAuth 2.0 do ORCID

Este módulo permite vincular um pesquisador da plataforma a um ORCID autenticado, sem solicitar nem armazenar a senha do investigador.

## Fluxo implementado

1. Um utilizador autenticado seleciona o pesquisador no módulo ORCID.
2. O backend gera um `state` assinado e devolve a URL oficial de autorização.
3. O navegador é redirecionado para o ORCID.
4. O investigador inicia sessão e concede o escopo `/authenticate`.
5. O ORCID redireciona para o callback da API com um código de autorização.
6. O backend valida o `state`, troca o código por tokens e confirma o ORCID autenticado.
7. O identificador é associado ao pesquisador e os tokens são cifrados com AES-256-GCM.
8. O sistema tenta sincronizar os dados públicos do perfil e redireciona novamente para o frontend.

## Redirect URI

Produção:

```text
https://academic-api.triacompany.com/api/v1/orcid/oauth/callback
```

A URI registada no painel de Developer Tools do ORCID deve ser exatamente igual ao valor de `ORCID_OAUTH_REDIRECT_URI`.

## Variáveis de ambiente obrigatórias

```env
ORCID_OAUTH_ENABLED=true
ORCID_OAUTH_CLIENT_ID=APP-XXXXXXXXXXXXXXX
ORCID_OAUTH_CLIENT_SECRET=substituir-pelo-segredo-real
ORCID_OAUTH_REDIRECT_URI=https://academic-api.triacompany.com/api/v1/orcid/oauth/callback
ORCID_OAUTH_FRONTEND_REDIRECT_URI=https://academic-optimization-platform.vercel.app/admin/orcid
ORCID_OAUTH_SCOPE=/authenticate
ORCID_OAUTH_STATE_SECRET=segredo-aleatorio-com-no-minimo-32-caracteres
ORCID_OAUTH_TOKEN_ENCRYPTION_KEY=chave-base64-de-32-bytes
```

## Geração dos segredos

Linux/macOS:

```bash
openssl rand -base64 48
openssl rand -base64 32
```

PowerShell:

```powershell
$stateBytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Fill($stateBytes)
[Convert]::ToBase64String($stateBytes)

$keyBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
[Convert]::ToBase64String($keyBytes)
```

Use o primeiro valor em `ORCID_OAUTH_STATE_SECRET` e o segundo em `ORCID_OAUTH_TOKEN_ENCRYPTION_KEY`.

## Sandbox

Para testes no ambiente sandbox:

```env
ORCID_OAUTH_AUTHORIZATION_URL=https://sandbox.orcid.org/oauth/authorize
ORCID_OAUTH_TOKEN_URL=https://sandbox.orcid.org/oauth/token
ORCID_OAUTH_REVOKE_URL=https://sandbox.orcid.org/oauth/revoke
```

As credenciais de sandbox e produção são diferentes. Não misture Client ID, Client Secret ou redirect URI entre os ambientes.

## Segurança

- O callback é o único endpoint OAuth público.
- O `state` contém o pesquisador e o utilizador iniciador, mas é assinado com HMAC-SHA256.
- Estados expiram em 10 minutos por padrão.
- Tokens nunca são enviados ao frontend.
- Tokens de acesso e atualização são cifrados antes de serem persistidos.
- Ao desconectar, o backend solicita a revogação ao ORCID e remove os tokens locais.
- Um ORCID não pode ser vinculado simultaneamente a dois pesquisadores.
- Um pesquisador que já possui outro ORCID não pode ser substituído silenciosamente pelo callback.

## Ativação gradual

Enquanto `ORCID_OAUTH_ENABLED=false` ou os segredos não estiverem configurados, a interface exibe o recurso como pendente e mantém todas as funções públicas existentes do ORCID operacionais.
