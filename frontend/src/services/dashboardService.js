import { apiClient } from "./apiClient";
import { academicProfileService } from "./academicProfileService";
import { auditService } from "./auditService";
import { crossrefService } from "./crossrefService";
import { openAlexService } from "./openAlexService";
import { orcidService } from "./orcidService";
import { researcherService } from "./researcherService";

const STATUS_ENDPOINTS = [
  "/api/v1/system-status",
  "/api/v1/status/operational",
  "/api/v1/status/operacional",
  "/api/v1/operational-status",
  "/api/v1/system/status",
  "/api/v1/health",
  "/actuator/health",
  "/health"
];

function unwrapList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.events)) return data.events;
  if (Array.isArray(data?.logs)) return data.logs;
  return [];
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

function normalizeStatusData(response) {
  const raw = response?.data?.data || response?.data || {};

  return {
    sourceEndpoint: response?.endpoint,
    status: raw?.status || raw?.state || "ONLINE",
    api:
      raw?.api ||
      raw?.application ||
      raw?.service ||
      "Academic Optimization Platform API",
    database: raw?.database || raw?.db || raw?.postgresql || "PostgreSQL",
    security: raw?.security || raw?.auth || "JWT ativo",
    openApi: raw?.openApi || raw?.docs || "/v3/api-docs",
    generatedAt: raw?.generatedAt || raw?.timestamp || raw?.checkedAt || null,
    raw
  };
}

function countWorksByStatus(works, status) {
  return works.filter(
    (work) => String(work.reviewStatus || "").toUpperCase() === status
  ).length;
}

async function settleList(callback) {
  try {
    const data = await callback();
    return unwrapList(data);
  } catch {
    return [];
  }
}

async function settleValue(callback, fallback = null) {
  try {
    return await callback();
  } catch {
    return fallback;
  }
}

export const dashboardService = {
  async getInstitutionalDashboard() {
    const researchers = await settleList(() => researcherService.findAll());
    const academicProfiles = await settleList(() =>
      academicProfileService.findAll()
    );

    const researcherIds = researchers.map((researcher) => researcher.id);

    const openAlexResults = await Promise.allSettled(
      researcherIds.map((researcherId) =>
        openAlexService.findWorksByResearcher(researcherId)
      )
    );

    const orcidResults = await Promise.allSettled(
      researcherIds.map((researcherId) =>
        orcidService.findWorksByResearcher(researcherId)
      )
    );

    const crossrefResults = await Promise.allSettled(
      researcherIds.map((researcherId) =>
        crossrefService.findValidationsByResearcher(researcherId)
      )
    );

    const auditEvents = await settleList(() => auditService.findAll());

    const openAlexWorks = openAlexResults.flatMap((result) =>
      result.status === "fulfilled" ? unwrapList(result.value) : []
    );

    const orcidWorks = orcidResults.flatMap((result) =>
      result.status === "fulfilled" ? unwrapList(result.value) : []
    );

    const crossrefValidations = crossrefResults.flatMap((result) =>
      result.status === "fulfilled" ? unwrapList(result.value) : []
    );

    const researchersWithOrcid = researchers.filter((researcher) =>
      Boolean(researcher.orcidId)
    ).length;

    const latestDates = [
      ...researchers.map((item) => item.updatedAt || item.createdAt),
      ...academicProfiles.map((item) => item.updatedAt || item.createdAt),
      ...openAlexWorks.map((item) => item.updatedAt || item.createdAt),
      ...crossrefValidations.map((item) => item.updatedAt || item.createdAt)
    ].filter(Boolean);

    const lastSync =
      latestDates.length > 0
        ? latestDates.sort((a, b) => new Date(b) - new Date(a))[0]
        : null;

    return {
      sourceEndpoint: "frontend-consolidated",
      totalResearchers: researchers.length,
      totalAcademicProfiles: academicProfiles.length,
      researchersWithOrcid,
      totalOrcidWorks: orcidWorks.length,
      totalOpenAlexWorks: openAlexWorks.length,
      pendingReviewWorks: countWorksByStatus(openAlexWorks, "PENDING_REVIEW"),
      confirmedWorks: countWorksByStatus(openAlexWorks, "CONFIRMED"),
      rejectedWorks: countWorksByStatus(openAlexWorks, "REJECTED"),
      reviewedWorks:
        countWorksByStatus(openAlexWorks, "CONFIRMED") +
        countWorksByStatus(openAlexWorks, "REJECTED"),
      validatedDois: crossrefValidations.length,
      optimizationReports: Number(
        localStorage
          .getItem("academic_optimization_local_reports_count")
          ?.trim() || 0
      ),
      pdfExports: 0,
      auditEvents: auditEvents.length,
      lastSync,
      raw: {
        researchers,
        academicProfiles,
        orcidWorks,
        openAlexWorks,
        crossrefValidations,
        auditEvents
      }
    };
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
  },

  async getSystemOverview() {
    const [dashboard, status, openApi] = await Promise.all([
      this.getInstitutionalDashboard(),
      settleValue(() => this.getOperationalStatus(), null),
      settleValue(() => this.getOpenApiSummary(), null)
    ]);

    return {
      dashboard,
      status,
      openApi
    };
  }
};