import assert from "node:assert/strict";
import test from "node:test";
import {
  extractToken,
  extractUser,
  isTokenExpired,
  parseJwtPayload
} from "../src/utils/jwt.js";

function createToken(payload) {
  const header = Buffer.from(JSON.stringify({ alg: "HS256", typ: "JWT" }))
    .toString("base64url");
  const body = Buffer.from(JSON.stringify(payload)).toString("base64url");
  return `${header}.${body}.signature`;
}

test("parseJwtPayload decodifica payload base64url com caracteres Unicode", () => {
  const token = createToken({
    sub: "wilson.admin@triacompany.com",
    name: "Administrador Académico",
    role: "ADMIN"
  });

  assert.deepEqual(parseJwtPayload(token), {
    sub: "wilson.admin@triacompany.com",
    name: "Administrador Académico",
    role: "ADMIN"
  });
});

test("parseJwtPayload retorna objeto vazio para token malformado", () => {
  assert.deepEqual(parseJwtPayload("token-invalido"), {});
  assert.deepEqual(parseJwtPayload(null), {});
});

test("isTokenExpired utiliza o exp do token de forma determinística", () => {
  const futureToken = createToken({ exp: 2_000 });
  const expiredToken = createToken({ exp: 1_000 });

  assert.equal(isTokenExpired(futureToken, 1_500_000), false);
  assert.equal(isTokenExpired(expiredToken, 1_500_000), true);
  assert.equal(isTokenExpired(createToken({}), 1_500_000), true);
});

test("extractToken reconhece os formatos suportados pela API", () => {
  assert.equal(extractToken({ token: "token-a" }), "token-a");
  assert.equal(extractToken({ accessToken: "token-b" }), "token-b");
  assert.equal(extractToken({ access_token: "token-c" }), "token-c");
  assert.equal(extractToken({ jwt: "token-d" }), "token-d");
  assert.equal(extractToken({ bearerToken: "token-e" }), "token-e");
  assert.equal(extractToken({}), null);
});

test("extractUser prioriza dados explícitos e usa o JWT como fallback", () => {
  const token = createToken({
    name: "Gestor Institucional",
    email: "gestor@imetroangola.com",
    role: "INSTITUTION"
  });

  assert.deepEqual(extractUser({}, "fallback@universidade.ao", token), {
    name: "Gestor Institucional",
    email: "gestor@imetroangola.com",
    role: "INSTITUTION"
  });

  const explicitUser = { name: "Wilson Dala", role: "ADMIN" };
  assert.equal(extractUser({ user: explicitUser }, "", token), explicitUser);
});
