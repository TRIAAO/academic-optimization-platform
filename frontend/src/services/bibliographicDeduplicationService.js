import { apiClient } from "./apiClient";

export const bibliographicDeduplicationService = {
  async findByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/bibliographic-deduplication/researchers/${researcherId}`
    );
    return response.data;
  },

  async scan(researcherId) {
    const response = await apiClient.post(
      `/api/v1/bibliographic-deduplication/researchers/${researcherId}/scan`
    );
    return response.data;
  },

  async review(researcherId, candidateId, status, note) {
    const response = await apiClient.put(
      `/api/v1/bibliographic-deduplication/researchers/${researcherId}/candidates/${candidateId}`,
      {
        status,
        note: String(note || "").trim() || null
      }
    );
    return response.data;
  }
};
