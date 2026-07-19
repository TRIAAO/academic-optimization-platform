import assert from "node:assert/strict";
import test from "node:test";
import {
  formatDateTime,
  formatNumber,
  formatStatus
} from "../src/utils/formatters.js";

test("formatNumber preserva o valor numérico no padrão pt-AO", () => {
  assert.equal(formatNumber(null), "0");
  assert.equal(formatNumber(1234).replace(/\D/g, ""), "1234");
});

test("formatDateTime preserva valores ausentes ou inválidos", () => {
  assert.equal(formatDateTime(null), "Não informado");
  assert.equal(formatDateTime("Não informado"), "Não informado");
  assert.equal(formatDateTime("data-inválida"), "data-inválida");
});

test("formatStatus normaliza estados operacionais conhecidos", () => {
  assert.equal(formatStatus("UP"), "Online");
  assert.equal(formatStatus("healthy"), "Online");
  assert.equal(formatStatus("FAILED"), "Indisponível");
  assert.equal(formatStatus("offline"), "Indisponível");
  assert.equal(formatStatus("DEGRADED"), "DEGRADED");
  assert.equal(formatStatus(null), "Não informado");
});
