import { apiClient } from "./apiClient";

const BASE_PATH = "/api/v1/abstract-analysis";

export const abstractAnalysisService = {
  async analyzeByResearcher(researcherId) {
    const response = await apiClient.get(
      `${BASE_PATH}/researchers/${encodeURIComponent(researcherId)}`
    );

    return response.data;
  },

  async updateTranslations(workId, translations) {
    const response = await apiClient.patch(
      `${BASE_PATH}/works/${encodeURIComponent(workId)}/translations`,
      {
        abstractPt: translations?.abstractPt || null,
        abstractEn: translations?.abstractEn || null
      }
    );

    return response.data;
  }
};
