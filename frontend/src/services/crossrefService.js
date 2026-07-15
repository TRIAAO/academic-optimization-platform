import { apiClient } from "./apiClient";

function unwrapList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.items)) return data.items;
  return [];
}

export const crossrefService = {
  async validateOpenAlexWork(workId) {
    const response = await apiClient.post(
      `/api/v1/crossref/openalex-works/${workId}/validate`
    );

    return response.data;
  },

  async findLatestValidationByOpenAlexWork(workId) {
    const response = await apiClient.get(
      `/api/v1/crossref/openalex-works/${workId}/validation`
    );

    return response.data;
  },

  async findValidationsByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/crossref/researchers/${researcherId}/validations`
    );

    return unwrapList(response.data);
  }
};