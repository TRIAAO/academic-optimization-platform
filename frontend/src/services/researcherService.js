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
    fullName: normalizeText(formData.fullName),
    email: normalizeText(formData.email),
    phone: normalizeText(formData.phone),
    institution: normalizeText(formData.institution),
    department: normalizeText(formData.department),
    academicTitle: normalizeText(formData.academicTitle),
    orcidId: normalizeText(formData.orcidId)
  };
}

function buildUpdatePayload(formData) {
  return {
    fullName: normalizeText(formData.fullName),
    phone: normalizeText(formData.phone),
    institution: normalizeText(formData.institution),
    department: normalizeText(formData.department),
    academicTitle: normalizeText(formData.academicTitle),
    orcidId: normalizeText(formData.orcidId)
  };
}

export const researcherService = {
  async findAll() {
    const response = await apiClient.get("/api/v1/researchers");
    return unwrapList(response.data);
  },

  async findById(id) {
    const response = await apiClient.get(`/api/v1/researchers/${id}`);
    return response.data;
  },

  async create(formData) {
    const response = await apiClient.post(
      "/api/v1/researchers",
      buildCreatePayload(formData)
    );

    return response.data;
  },

  async update(id, formData) {
    const response = await apiClient.put(
      `/api/v1/researchers/${id}`,
      buildUpdatePayload(formData)
    );

    return response.data;
  },

  async deactivate(id) {
    await apiClient.delete(`/api/v1/researchers/${id}`);
    return true;
  }
};