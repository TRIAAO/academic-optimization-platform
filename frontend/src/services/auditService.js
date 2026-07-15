import { requestByOpenApi } from "./endpointResolver";

function unwrapList(data) {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.items)) return data.items;
  if (Array.isArray(data?.events)) return data.events;
  if (Array.isArray(data?.logs)) return data.logs;
  return [];
}

export const auditService = {
  async findAll() {
    const result = await requestByOpenApi({
      method: "get",
      preferred: [
        "/api/v1/audit",
        "/api/v1/audit/events",
        "/api/v1/audit-logs",
        "/api/v1/audits"
      ],
      keywords: ["audit"]
    });

    return unwrapList(result.data);
  }
};