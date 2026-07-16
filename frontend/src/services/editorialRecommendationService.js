import { apiClient } from "./apiClient";

const BASE_PATH = "/api/v1/editorial-recommendations";

export const editorialRecommendationService = {
  async generateByResearcher(researcherId, workId) {
    const response = await apiClient.get(
      `${BASE_PATH}/researchers/${encodeURIComponent(researcherId)}`,
      {
        params: workId ? { workId } : undefined
      }
    );

    return response.data;
  }
};
