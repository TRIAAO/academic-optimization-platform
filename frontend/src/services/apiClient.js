import axios from "axios";
import { APP_CONFIG, STORAGE_KEYS } from "../config/app";

export function normalizeApiError(error) {
  const status = error?.response?.status;
  const data = error?.response?.data;

  const message =
    data?.message ||
    data?.error ||
    data?.detail ||
    error?.message ||
    "Não foi possível concluir a operação.";

  return {
    status,
    message,
    data,
    originalError: error
  };
}

export const apiClient = axios.create({
  baseURL: APP_CONFIG.apiBaseUrl,
  timeout: 20000,
  headers: {
    "Content-Type": "application/json"
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