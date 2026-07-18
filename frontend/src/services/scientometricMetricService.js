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

function normalizeNumber(value) {
  if (value === undefined || value === null || value === "") return null;

  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function buildPayload(formData) {
  return {
    source: normalizeText(formData.source) || "MANUAL_GOOGLE_SCHOLAR",
    googleScholarAuthorId: normalizeText(formData.googleScholarAuthorId),
    googleScholarProfileUrl: normalizeText(formData.googleScholarProfileUrl),
    hIndexTotal: normalizeNumber(formData.hIndexTotal),
    hIndexLastSixYears: normalizeNumber(formData.hIndexLastSixYears),
    i10IndexTotal: normalizeNumber(formData.i10IndexTotal),
    i10IndexLastSixYears: normalizeNumber(formData.i10IndexLastSixYears),
    citationsTotal: normalizeNumber(formData.citationsTotal),
    citationsLastSixYears: normalizeNumber(formData.citationsLastSixYears),
    dIndex: normalizeNumber(formData.dIndex),
    verifiedEmail: normalizeText(formData.verifiedEmail),
    institutionalEmailVerified: Boolean(formData.institutionalEmailVerified),
    interests: normalizeText(formData.interests),
    notes: normalizeText(formData.notes),
    snapshotDate: normalizeText(formData.snapshotDate)
  };
}

function buildUpdatePayload(formData) {
  const payload = buildPayload(formData);

  return {
    ...payload,
    googleScholarAuthorId: String(formData.googleScholarAuthorId || "").trim(),
    googleScholarProfileUrl: String(
      formData.googleScholarProfileUrl || ""
    ).trim(),
    verifiedEmail: String(formData.verifiedEmail || "").trim(),
    interests: String(formData.interests || "").trim(),
    notes: String(formData.notes || "").trim()
  };
}

export const scientometricMetricService = {
  async findByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/researchers/${researcherId}/scientometric-metrics`
    );

    return unwrapList(response.data);
  },

  async findLatestByResearcher(researcherId) {
    const response = await apiClient.get(
      `/api/v1/researchers/${researcherId}/scientometric-metrics/latest`
    );

    return response.data;
  },

  async analyze(researcherId) {
    const response = await apiClient.get(
      `/api/v1/researchers/${researcherId}/scientometric-analysis`
    );

    return response.data;
  },

  async findInstitutionalHistory(months = 12, staleAfterMonths = 12) {
    const response = await apiClient.get(
      "/api/v1/institutional-scientometrics/history",
      {
        params: {
          months,
          staleAfterMonths
        }
      }
    );

    return response.data;
  },

  async findById(id) {
    const response = await apiClient.get(`/api/v1/scientometric-metrics/${id}`);
    return response.data;
  },

  async create(researcherId, formData) {
    const response = await apiClient.post(
      `/api/v1/researchers/${researcherId}/scientometric-metrics`,
      buildPayload(formData)
    );

    return response.data;
  },

  async update(id, formData) {
    const response = await apiClient.put(
      `/api/v1/scientometric-metrics/${id}`,
      buildUpdatePayload(formData)
    );

    return response.data;
  },

  async delete(id) {
    await apiClient.delete(`/api/v1/scientometric-metrics/${id}`);
    return true;
  }
};
