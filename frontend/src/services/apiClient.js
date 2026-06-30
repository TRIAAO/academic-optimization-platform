import axios from "axios";
import { APP_CONFIG, STORAGE_KEYS } from "../config/app";

function stripHtml(value) {
  if (!value) {
    return "";
  }

  return String(value)
    .replace(/<!doctype[^>]*>/gi, "")
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<style[\s\S]*?>[\s\S]*?<\/style>/gi, "")
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/g, " ")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&amp;/g, "&")
    .replace(/\s+/g, " ")
    .trim();
}

function makeFriendlyMessage(message) {
  const cleanMessage = stripHtml(message);

  if (!cleanMessage) {
    return "Não foi possível concluir a operação.";
  }

  if (
    cleanMessage.toLowerCase().includes("autor não encontrado no openalex") ||
    cleanMessage.toLowerCase().includes("not found")
  ) {
    return "Autor não encontrado no OpenAlex para o ORCID informado. Tente buscar candidatos pelo nome do pesquisador ou use a importação por autor candidato.";
  }

  if (cleanMessage.toLowerCase().includes("network error")) {
    return "Erro de rede. Verifique se a API está online e se o CORS está liberado.";
  }

  return cleanMessage;
}

export function normalizeApiError(error) {
  const status = error?.response?.status;
  const data = error?.response?.data;

  const rawMessage =
    data?.message ||
    data?.error ||
    data?.detail ||
    data?.title ||
    error?.message ||
    "Não foi possível concluir a operação.";

  return {
    status,
    message: makeFriendlyMessage(rawMessage),
    data,
    originalError: error
  };
}

export const apiClient = axios.create({
  baseURL: APP_CONFIG.apiBaseUrl,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json"
  }
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(STORAGE_KEYS.token);

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const normalizedError = normalizeApiError(error);

    if (
      normalizedError.status === 401 &&
      !window.location.pathname.includes("/login")
    ) {
      localStorage.removeItem(STORAGE_KEYS.token);
      localStorage.removeItem(STORAGE_KEYS.user);
      window.location.href = "/login";
    }

    return Promise.reject(normalizedError);
  }
);