import { apiClient } from "./apiClient";

function unwrapList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.items)) return data.items;
  return [];
}

function buildReviewPayload(reviewNote) {
  const note = reviewNote === undefined || reviewNote === null ? "" : String(reviewNote).trim();

  if (!note) {
    return {};
  }

  return {
    reviewNote: note
  };
}

export const openAlexService = {
  async findVerifiedAuthor(researcherId) {
    const response = await apiClient.get(
      `/api/v1/openalex/researchers/${researcherId}/author`
    );

    return response.data;
  },

  async findAuthorCandidates(researcherId) {
    const response = await apiClient.get(
      `/api/v1/openalex/researchers/${researcherId}/author-candidates`
    );

    return unwrapList(response.data);
  },

  async searchWorks(researcherId) {
    const response = await apiClient.get(
      `/api/v1/openalex/researchers/${researcherId}/search-works`
    );

    return response.data;
  },

  async importWorks(researcherId) {
    const response = await apiClient.post(
      `/api/v1/openalex/researchers/${researcherId}/import-works`
    );

    return response.data;
  },

  async importWorksByApprovedAuthor(researcherId, openAlexAuthorShortId) {
    const response = await apiClient.post(
      `/api/v1/openalex/researchers/${researcherId}/import-works-by-author/${openAlexAuthorShortId}`
    );

    return response.data;
  },

  async findWorksByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/openalex/researchers/${researcherId}/works`
    );

    return unwrapList(response.data);
  },

  async findWorksByResearcherAndStatus(researcherId, reviewStatus) {
    const response = await apiClient.get(
      `/api/v1/openalex/researchers/${researcherId}/works/status/${reviewStatus}`
    );

    return unwrapList(response.data);
  },

  async findPendingReviewWorks(researcherId) {
    const response = await apiClient.get(
      `/api/v1/openalex/researchers/${researcherId}/works/pending-review`
    );

    return unwrapList(response.data);
  },

  async confirmWork(workId, reviewNote = "") {
    const response = await apiClient.patch(
      `/api/v1/openalex/works/${workId}/confirm`,
      buildReviewPayload(reviewNote)
    );

    return response.data;
  },

  async rejectWork(workId, reviewNote = "") {
    const response = await apiClient.patch(
      `/api/v1/openalex/works/${workId}/reject`,
      buildReviewPayload(reviewNote)
    );

    return response.data;
  },

  async markWorkAsPendingReview(workId, reviewNote = "") {
    const response = await apiClient.patch(
      `/api/v1/openalex/works/${workId}/pending-review`,
      buildReviewPayload(reviewNote)
    );

    return response.data;
  },

  async deleteWorksByResearcher(researcherId) {
    const response = await apiClient.delete(
      `/api/v1/openalex/researchers/${researcherId}/works`
    );

    return response.data;
  }
};