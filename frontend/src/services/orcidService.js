import { apiClient } from "./apiClient";

function unwrapList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.items)) return data.items;
  return [];
}

export const orcidService = {
  async importWorks(researcherId) {
    const response = await apiClient.post(
      `/api/v1/orcid/researchers/${researcherId}/import-works`
    );

    return response.data;
  },

  async findWorksByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/orcid/researchers/${researcherId}/works`
    );

    return unwrapList(response.data);
  },

  async findImportLogsByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/orcid/researchers/${researcherId}/logs`
    );

    return unwrapList(response.data);
  },

  async findSummaryByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/orcid/researchers/${researcherId}/summary`
    );

    return response.data;
  },

  async syncProfile(researcherId) {
    const response = await apiClient.post(
      `/api/v1/orcid/researchers/${researcherId}/sync-profile`
    );

    return response.data;
  },

  async findSummaryByOrcidId(orcidId) {
    const response = await apiClient.get(`/api/v1/orcid/${orcidId}/summary`);
    return response.data;
  }
};