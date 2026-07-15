import { academicProfileService } from "./academicProfileService";
import { crossrefService } from "./crossrefService";
import { openAlexService } from "./openAlexService";
import { orcidService } from "./orcidService";
import { researcherService } from "./researcherService";
import { requestByOpenApi } from "./endpointResolver";

const LOCAL_REPORTS_KEY = "academic_optimization_local_reports";

function readLocalReports() {
  try {
    return JSON.parse(localStorage.getItem(LOCAL_REPORTS_KEY) || "[]");
  } catch {
    return [];
  }
}

function saveLocalReport(report) {
  const reports = readLocalReports();
  const nextReports = [report, ...reports].slice(0, 50);
  localStorage.setItem(LOCAL_REPORTS_KEY, JSON.stringify(nextReports));
  return report;
}

function getLocalReportsByResearcher(researcherId) {
  return readLocalReports().filter((report) => report.researcherId === researcherId);
}

function getLatestLocalReportByResearcher(researcherId) {
  return getLocalReportsByResearcher(researcherId)[0] || null;
}

function safeArray(value) {
  return Array.isArray(value) ? value : [];
}

function countByStatus(works, status) {
  return safeArray(works).filter(
    (work) => String(work.reviewStatus || "").toUpperCase() === status
  ).length;
}

function calculateScore({ researcher, profile, openAlexWorks, crossrefValidations }) {
  let score = 0;

  if (researcher?.fullName) score += 10;
  if (researcher?.email) score += 10;
  if (researcher?.institution) score += 10;
  if (researcher?.department) score += 5;
  if (researcher?.academicTitle) score += 5;
  if (researcher?.orcidId) score += 15;

  if (profile?.researchArea) score += 10;
  if (profile?.biography) score += 10;
  if (profile?.keywords) score += 10;
  if (profile?.googleScholarUrl) score += 5;
  if (profile?.orcidUrl) score += 5;

  if (safeArray(openAlexWorks).length > 0) score += 10;
  if (safeArray(crossrefValidations).length > 0) score += 10;

  return Math.min(score, 100);
}

function buildRecommendations({ researcher, profile, openAlexWorks, crossrefValidations }) {
  const recommendations = [];

  if (!researcher?.orcidId) {
    recommendations.push({
      priority: "ALTA",
      title: "Cadastrar ORCID",
      description:
        "O pesquisador ainda não possui ORCID informado. O ORCID é essencial para importação, sincronização e validação acadêmica."
    });
  }

  if (!profile?.biography) {
    recommendations.push({
      priority: "MÉDIA",
      title: "Completar biografia acadêmica",
      description:
        "Adicionar uma biografia acadêmica melhora a consistência institucional do perfil."
    });
  }

  if (!profile?.keywords) {
    recommendations.push({
      priority: "MÉDIA",
      title: "Adicionar palavras-chave",
      description:
        "Palavras-chave ajudam a organizar áreas científicas e melhorar a visibilidade acadêmica."
    });
  }

  if (safeArray(openAlexWorks).length === 0) {
    recommendations.push({
      priority: "ALTA",
      title: "Importar obras do OpenAlex",
      description:
        "Nenhuma obra OpenAlex foi encontrada no frontend. Importe ou revise candidatos antes de gerar relatório final."
    });
  }

  if (countByStatus(openAlexWorks, "PENDING_REVIEW") > 0) {
    recommendations.push({
      priority: "ALTA",
      title: "Concluir revisão manual",
      description:
        "Existem obras pendentes de revisão. Confirme ou rejeite para evitar associação incorreta de publicações."
    });
  }

  if (safeArray(crossrefValidations).length === 0) {
    recommendations.push({
      priority: "MÉDIA",
      title: "Validar DOIs no Crossref",
      description:
        "Nenhuma validação Crossref foi encontrada. Valide DOI e metadados das obras com DOI."
    });
  }

  if (!profile?.googleScholarUrl) {
    recommendations.push({
      priority: "BAIXA",
      title: "Adicionar URL do Google Acadêmico manualmente",
      description:
        "O Google Acadêmico deve ser tratado apenas como checklist manual. O pesquisador pode informar a URL manualmente no perfil acadêmico."
    });
  }

  if (recommendations.length === 0) {
    recommendations.push({
      priority: "BAIXA",
      title: "Perfil em bom estado",
      description:
        "O pesquisador possui dados acadêmicos consistentes. Recomenda-se apenas manter revisões periódicas."
    });
  }

  return recommendations;
}

async function generateFrontendReport(researcherId) {
  const researcher = await researcherService.findById(researcherId);

  let profile = null;
  let orcidSummary = null;
  let orcidWorks = [];
  let openAlexWorks = [];
  let crossrefValidations = [];

  try {
    profile = await academicProfileService.findByResearcherId(researcherId);
  } catch {
    profile = null;
  }

  try {
    orcidSummary = await orcidService.findSummaryByResearcher(researcherId);
  } catch {
    orcidSummary = null;
  }

  try {
    orcidWorks = await orcidService.findWorksByResearcher(researcherId);
  } catch {
    orcidWorks = [];
  }

  try {
    openAlexWorks = await openAlexService.findWorksByResearcher(researcherId);
  } catch {
    openAlexWorks = [];
  }

  try {
    crossrefValidations =
      await crossrefService.findValidationsByResearcher(researcherId);
  } catch {
    crossrefValidations = [];
  }

  const score = calculateScore({
    researcher,
    profile,
    openAlexWorks,
    crossrefValidations
  });

  const recommendations = buildRecommendations({
    researcher,
    profile,
    openAlexWorks,
    crossrefValidations
  });

  const report = {
    id: crypto.randomUUID(),
    source: "FRONTEND_CONSOLIDATED_REPORT",
    title: `Relatório de Otimização Acadêmica — ${researcher.fullName}`,
    researcherId,
    researcherName: researcher.fullName,
    generatedAt: new Date().toISOString(),
    score,
    summary: {
      profileCompletionPercentage: profile?.profileCompletionPercentage || 0,
      totalOrcidWorks: safeArray(orcidWorks).length,
      totalOpenAlexWorks: safeArray(openAlexWorks).length,
      confirmedOpenAlexWorks: countByStatus(openAlexWorks, "CONFIRMED"),
      pendingOpenAlexWorks: countByStatus(openAlexWorks, "PENDING_REVIEW"),
      rejectedOpenAlexWorks: countByStatus(openAlexWorks, "REJECTED"),
      totalCrossrefValidations: safeArray(crossrefValidations).length
    },
    researcher,
    academicProfile: profile,
    orcid: {
      summary: orcidSummary,
      works: orcidWorks
    },
    openAlex: {
      works: openAlexWorks
    },
    crossref: {
      validations: crossrefValidations
    },
    recommendations,
    googleScholarPolicy:
      "Google Acadêmico é apenas checklist e orientação manual. A plataforma não automatiza, não acessa, não altera e não coleta dados diretamente do Google Acadêmico."
  };

  return saveLocalReport(report);
}

function extractFilename(contentDisposition) {
  if (!contentDisposition) {
    return "relatorio-otimizacao-academica.pdf";
  }

  const match = String(contentDisposition).match(/filename="?([^"]+)"?/i);
  return match?.[1] || "relatorio-otimizacao-academica.pdf";
}

export const reportService = {
  async generateByResearcher(researcherId) {
    try {
      const result = await requestByOpenApi({
        method: "post",
        preferred: [
          "/api/v1/reports/researchers/{researcherId}/generate",
          "/api/v1/optimization-reports/researchers/{researcherId}/generate",
          "/api/v1/academic-reports/researchers/{researcherId}/generate"
        ],
        keywords: ["report"],
        requiredParams: ["researcherId"],
        params: {
          researcherId
        }
      });

      return result.data;
    } catch {
      return generateFrontendReport(researcherId);
    }
  },

  async findByResearcher(researcherId) {
    try {
      const result = await requestByOpenApi({
        method: "get",
        preferred: [
          "/api/v1/reports/researchers/{researcherId}",
          "/api/v1/optimization-reports/researchers/{researcherId}",
          "/api/v1/academic-reports/researchers/{researcherId}"
        ],
        keywords: ["report"],
        requiredParams: ["researcherId"],
        params: {
          researcherId
        }
      });

      if (Array.isArray(result.data)) return result.data;
      if (Array.isArray(result.data?.content)) return result.data.content;
      if (Array.isArray(result.data?.data)) return result.data.data;
      if (Array.isArray(result.data?.items)) return result.data.items;

      return result.data ? [result.data] : [];
    } catch {
      return getLocalReportsByResearcher(researcherId);
    }
  },

  async findLatestByResearcher(researcherId) {
    try {
      const result = await requestByOpenApi({
        method: "get",
        preferred: [
          "/api/v1/reports/researchers/{researcherId}/latest",
          "/api/v1/optimization-reports/researchers/{researcherId}/latest",
          "/api/v1/academic-reports/researchers/{researcherId}/latest"
        ],
        keywords: ["report", "latest"],
        requiredParams: ["researcherId"],
        params: {
          researcherId
        }
      });

      return result.data;
    } catch {
      return getLatestLocalReportByResearcher(researcherId);
    }
  },

  async downloadPdf({ researcherId, reportId }) {
    const preferred = reportId
      ? [
          "/api/v1/reports/{reportId}/pdf",
          "/api/v1/optimization-reports/{reportId}/pdf",
          "/api/v1/academic-reports/{reportId}/pdf"
        ]
      : [
          "/api/v1/reports/researchers/{researcherId}/pdf",
          "/api/v1/optimization-reports/researchers/{researcherId}/pdf",
          "/api/v1/academic-reports/researchers/{researcherId}/pdf"
        ];

    const result = await requestByOpenApi({
      method: "get",
      preferred,
      keywords: ["pdf"],
      requiredParams: reportId ? ["reportId"] : ["researcherId"],
      params: {
        researcherId,
        reportId
      },
      responseType: "blob"
    });

    return {
      blob: result.data,
      filename: extractFilename(result.headers?.["content-disposition"]),
      endpoint: result.endpoint
    };
  }
};