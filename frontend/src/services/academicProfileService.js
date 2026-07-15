import { apiClient } from "./apiClient";

function unwrapList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.items)) return data.items;
  return [];
}

function normalizeText(value) {
  if (value === undefined || value === null) return null;
  const text = String(value).trim();
  return text === "" ? null : text;
}

function buildCreatePayload(formData) {
  return {
    researcherId: formData.researcherId,
    researchArea: normalizeText(formData.researchArea),
    biography: normalizeText(formData.biography),
    keywords: normalizeText(formData.keywords),
    googleScholarUrl: normalizeText(formData.googleScholarUrl),
    orcidUrl: normalizeText(formData.orcidUrl),
    scopusAuthorId: normalizeText(formData.scopusAuthorId),
    webOfScienceId: normalizeText(formData.webOfScienceId),
    lattesUrl: normalizeText(formData.lattesUrl),
    institutionalProfileUrl: normalizeText(formData.institutionalProfileUrl)
  };
}

function buildUpdatePayload(formData) {
  return {
    researchArea: normalizeText(formData.researchArea),
    biography: normalizeText(formData.biography),
    keywords: normalizeText(formData.keywords),
    googleScholarUrl: normalizeText(formData.googleScholarUrl),
    orcidUrl: normalizeText(formData.orcidUrl),
    scopusAuthorId: normalizeText(formData.scopusAuthorId),
    webOfScienceId: normalizeText(formData.webOfScienceId),
    lattesUrl: normalizeText(formData.lattesUrl),
    institutionalProfileUrl: normalizeText(formData.institutionalProfileUrl)
  };
}

export const academicProfileService = {
  async findAll() {
    const response = await apiClient.get("/api/v1/academic-profiles");
    return unwrapList(response.data);
  },

  async findById(id) {
    const response = await apiClient.get(`/api/v1/academic-profiles/${id}`);
    return response.data;
  },

  async findByResearcherId(researcherId) {
    const response = await apiClient.get(
      `/api/v1/academic-profiles/researcher/${researcherId}`
    );

    return response.data;
  },

  async create(formData) {
    const response = await apiClient.post(
      "/api/v1/academic-profiles",
      buildCreatePayload(formData)
    );

    return response.data;
  },

  async update(id, formData) {
    const response = await apiClient.put(
      `/api/v1/academic-profiles/${id}`,
      buildUpdatePayload(formData)
    );

    return response.data;
  }
};