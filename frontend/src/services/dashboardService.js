import { apiClient } from "./apiClient";

const DASHBOARD_ENDPOINTS = [
  "/api/v1/dashboard/institutional",
  "/api/v1/dashboard/institucional",
  "/api/v1/institutional-dashboard",
  "/api/v1/dashboard"
];

const STATUS_ENDPOINTS = [
  "/api/v1/status/operational",
  "/api/v1/status/operacional",
  "/api/v1/operational-status",
  "/api/v1/system/status",
  "/api/v1/health",
  "/actuator/health",
  "/health"
];

function pickNumber(source, keys, fallback = 0) {
  for (const key of keys) {
    const value = source?.[key];

    if (typeof value === "number") {
      return value;
    }

    if (typeof value === "string" && value.trim() !== "" && !Number.isNaN(Number(value))) {
      return Number(value);
    }
  }

  return fallback;
}

function pickText(source, keys, fallback = "Não informado") {
  for (const key of keys) {
    const value = source?.[key];

    if (typeof value === "string" && value.trim() !== "") {
      return value;
    }
  }

  return fallback;
}

async function requestFirstAvailable(endpoints) {
  let lastError = null;

  for (const endpoint of endpoints) {
    try {
      const response = await apiClient.get(endpoint);
      return {
        endpoint,
        data: response.data
      };
    } catch (error) {
      lastError = error;

      if (![404, 405].includes(error?.status)) {
        throw error;
      }
    }
  }

  throw (
    lastError || {
      status: 404,
      message: "Nenhum endpoint compatível foi encontrado."
    }
  );
}

function normalizeDashboardData(response) {
  const raw = response?.data?.data || response?.data || {};
  const metrics = raw?.metrics || raw?.indicadores || raw?.summary || raw;

  return {
    sourceEndpoint: response?.endpoint,
    totalResearchers: pickNumber(metrics, [
      "totalResearchers",
      "researchers",
      "totalPesquisadores",
      "pesquisadores"
    ]),
    totalAcademicProfiles: pickNumber(metrics, [
      "totalAcademicProfiles",
      "academicProfiles",
      "totalPerfisAcademicos",
      "perfisAcademicos"
    ]),
    researchersWithOrcid: pickNumber(metrics, [
      "researchersWithOrcid",
      "withOrcid",
      "pesquisadoresComOrcid",
      "comOrcid"
    ]),
    openAlexCandidates: pickNumber(metrics, [
      "openAlexCandidates",
      "candidatesOpenAlex",
      "candidatosOpenAlex",
      "obrasCandidatas"
    ]),
    reviewedWorks: pickNumber(metrics, [
      "reviewedWorks",
      "obrasRevisadas",
      "manualReviews",
      "revisoesManuais"
    ]),
    validatedDois: pickNumber(metrics, [
      "validatedDois",
      "validDois",
      "doisValidados",
      "crossrefValidations"
    ]),
    optimizationReports: pickNumber(metrics, [
      "optimizationReports",
      "reports",
      "relatorios",
      "relatoriosOtimizacao"
    ]),
    pdfExports: pickNumber(metrics, [
      "pdfExports",
      "exportsPdf",
      "pdfsGerados",
      "relatoriosPdf"
    ]),
    auditEvents: pickNumber(metrics, [
      "auditEvents",
      "audits",
      "eventosAuditoria",
      "rastreabilidade"
    ]),
    lastSync: pickText(metrics, [
      "lastSync",
      "lastUpdated",
      "ultimaSincronizacao",
      "updatedAt"
    ]),
    raw
  };
}

function normalizeStatusData(response) {
  const raw = response?.data?.data || response?.data || {};

  return {
    sourceEndpoint: response?.endpoint,
    status: raw?.status || raw?.state || "ONLINE",
    api: raw?.api || raw?.application || raw?.service || "Academic API",
    database: raw?.database || raw?.db || raw?.postgresql || "Não informado",
    security: raw?.security || raw?.auth || "JWT ativo",
    openApi: raw?.openApi || raw?.docs || "/v3/api-docs",
    generatedAt: raw?.generatedAt || raw?.timestamp || raw?.checkedAt || null,
    raw
  };
}

export const dashboardService = {
  async getInstitutionalDashboard() {
    const response = await requestFirstAvailable(DASHBOARD_ENDPOINTS);
    return normalizeDashboardData(response);
  },

  async getOperationalStatus() {
    const response = await requestFirstAvailable(STATUS_ENDPOINTS);
    return normalizeStatusData(response);
  },

  async getOpenApiSummary() {
    const response = await apiClient.get("/v3/api-docs");
    const paths = response.data?.paths || {};
    const tags = response.data?.tags || [];

    return {
      title: response.data?.info?.title || "OpenAPI JSON",
      version: response.data?.info?.version || "Não informada",
      totalPaths: Object.keys(paths).length,
      totalTags: tags.length,
      paths,
      tags
    };
  }
};