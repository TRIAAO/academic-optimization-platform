import { apiClient } from "./apiClient";

const BASE_PATH = "/api/v1/editorial-recommendations";
const DECISION_BASE_PATH = "/api/v1/editorial-decisions";

export const editorialRecommendationService = {
  async generateByResearcher(researcherId, workId) {
    const response = await apiClient.get(
      `${BASE_PATH}/researchers/${encodeURIComponent(researcherId)}`,
      {
        params: workId ? { workId } : undefined
      }
    );

    return response.data;
  },

  async findDecisionByWork(researcherId, workId) {
    const response = await apiClient.get(
      `${DECISION_BASE_PATH}/researchers/${encodeURIComponent(researcherId)}/works/${encodeURIComponent(workId)}`
    );

    return response.status === 204 ? null : response.data;
  },

  async saveDecision(researcherId, workId, decision) {
    const response = await apiClient.put(
      `${DECISION_BASE_PATH}/researchers/${encodeURIComponent(researcherId)}/works/${encodeURIComponent(workId)}`,
      decision
    );

    return response.data;
  }
};
