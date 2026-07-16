import { apiClient } from "./apiClient";

const REPORT_BASE_PATH = "/api/v1/optimization-reports/researchers";

function extractFilename(contentDisposition) {
  if (!contentDisposition) {
    return "relatorio-otimizacao-academica.pdf";
  }

  const utf8Match = String(contentDisposition).match(/filename\*=UTF-8''([^;]+)/i);

  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1]);
  }

  const match = String(contentDisposition).match(/filename="?([^";]+)"?/i);
  return match?.[1] || "relatorio-otimizacao-academica.pdf";
}

export const optimizationService = {
  async generateByResearcher(researcherId) {
    const response = await apiClient.get(
      `${REPORT_BASE_PATH}/${encodeURIComponent(researcherId)}`
    );

    return response.data;
  },

  async downloadPdf(researcherId) {
    const response = await apiClient.get(
      `${REPORT_BASE_PATH}/${encodeURIComponent(researcherId)}/pdf`,
      { responseType: "blob" }
    );

    return {
      blob: response.data,
      filename: extractFilename(response.headers?.["content-disposition"])
    };
  }
};
