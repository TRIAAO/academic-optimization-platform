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
    const response = await apiClient.get("/api/v1/orcid/lookup", {
      params: { orcid: orcidId }
    });
    return response.data;
  },

  async getOAuthConfiguration() {
    const response = await apiClient.get(
      "/api/v1/orcid/oauth/configuration"
    );

    return response.data;
  },

  async getOAuthConnection(researcherId) {
    const response = await apiClient.get(
      `/api/v1/orcid/oauth/researchers/${researcherId}/connection`
    );

    return response.data;
  },

  async createOAuthAuthorization(researcherId) {
    const response = await apiClient.post(
      `/api/v1/orcid/oauth/researchers/${researcherId}/authorization-url`
    );

    return response.data;
  },

  async disconnectOAuth(researcherId) {
    const response = await apiClient.delete(
      `/api/v1/orcid/oauth/researchers/${researcherId}/connection`
    );

    return response.data;
  }
};
