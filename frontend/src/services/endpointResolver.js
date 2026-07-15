import { apiClient } from "./apiClient";

let cachedOpenApi = null;

async function getOpenApi() {
  if (cachedOpenApi) {
    return cachedOpenApi;
  }

  const response = await apiClient.get("/v3/api-docs");
  cachedOpenApi = response.data;

  return cachedOpenApi;
}

function hasMethod(pathDefinition, method) {
  return Boolean(pathDefinition?.[method.toLowerCase()]);
}

function normalize(value) {
  return String(value || "").toLowerCase();
}

function pathMatches(path, keywords = [], requiredParams = []) {
  const normalizedPath = normalize(path);

  const keywordsMatch = keywords.every((keyword) =>
    normalizedPath.includes(normalize(keyword))
  );

  const paramsMatch = requiredParams.every((param) =>
    normalizedPath.includes(`{${param}}`.toLowerCase())
  );

  return keywordsMatch && paramsMatch;
}

export function buildPath(pathTemplate, params = {}) {
  return Object.entries(params).reduce((path, [key, value]) => {
    return path.replaceAll(`{${key}}`, encodeURIComponent(value));
  }, pathTemplate);
}

export async function findEndpoint({
  method = "get",
  preferred = [],
  keywords = [],
  requiredParams = []
}) {
  const openApi = await getOpenApi();
  const paths = openApi?.paths || {};

  for (const preferredPath of preferred) {
    if (paths[preferredPath] && hasMethod(paths[preferredPath], method)) {
      return preferredPath;
    }
  }

  const found = Object.entries(paths).find(([path, definition]) => {
    return (
      hasMethod(definition, method) &&
      pathMatches(path, keywords, requiredParams)
    );
  });

  if (!found) {
    throw {
      status: 404,
      message: `Endpoint não encontrado no OpenAPI para método ${method.toUpperCase()} com palavras-chave: ${keywords.join(
        ", "
      )}.`
    };
  }

  return found[0];
}

export async function requestByOpenApi({
  method = "get",
  preferred = [],
  keywords = [],
  requiredParams = [],
  params = {},
  data,
  responseType
}) {
  const endpoint = await findEndpoint({
    method,
    preferred,
    keywords,
    requiredParams
  });

  const url = buildPath(endpoint, params);

  const response = await apiClient.request({
    url,
    method,
    data,
    responseType
  });

  return {
    endpoint,
    data: response.data,
    headers: response.headers
  };
}