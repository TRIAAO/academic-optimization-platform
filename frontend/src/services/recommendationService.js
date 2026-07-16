import { apiClient } from "./apiClient";

const BASE_PATH = "/api/v1/academic-recommendations/researchers";

export const recommendationService = {
  async generateByResearcher(researcherId) {
    const response = await apiClient.get(
      `${BASE_PATH}/${encodeURIComponent(researcherId)}`
    );

    return response.data;
  }
};
