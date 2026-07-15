import { apiClient } from "./apiClient";
import { STORAGE_KEYS } from "../config/app";

function parseJwtPayload(token) {
  try {
    const base64Payload = token.split(".")[1];
    const normalizedPayload = base64Payload.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(normalizedPayload)
        .split("")
        .map((character) => {
          return `%${`00${character.charCodeAt(0).toString(16)}`.slice(-2)}`;
        })
        .join("")
    );

    return JSON.parse(jsonPayload);
  } catch {
    return {};
  }
}

function extractToken(data) {
  return (
    data?.token ||
    data?.accessToken ||
    data?.access_token ||
    data?.jwt ||
    data?.bearerToken ||
    null
  );
}

function extractUser(data, email, token) {
  const jwtPayload = token ? parseJwtPayload(token) : {};

  return (
    data?.user ||
    data?.profile ||
    data?.account || {
      name:
        data?.name ||
        jwtPayload?.name ||
        jwtPayload?.fullName ||
        jwtPayload?.sub ||
        "Administrador",
      email: data?.email || jwtPayload?.email || email,
      role:
        data?.role ||
        jwtPayload?.role ||
        jwtPayload?.authorities?.[0] ||
        jwtPayload?.roles?.[0] ||
        "ADMIN"
    }
  );
}

export const authService = {
  async login({ email, password }) {
    const payload = {
      email,
      username: email,
      password
    };

    const response = await apiClient.post("/api/v1/auth/login", payload);
    const token = extractToken(response.data);

    if (!token) {
      throw {
        status: 500,
        message:
          "Login realizado, mas a API não retornou token JWT em um campo reconhecido."
      };
    }

    const user = extractUser(response.data, email, token);

    localStorage.setItem(STORAGE_KEYS.token, token);
    localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(user));

    return {
      token,
      user
    };
  },

  logout() {
    localStorage.removeItem(STORAGE_KEYS.token);
    localStorage.removeItem(STORAGE_KEYS.user);
  },

  getStoredSession() {
    const token = localStorage.getItem(STORAGE_KEYS.token);
    const storedUser = localStorage.getItem(STORAGE_KEYS.user);

    if (!token) {
      return {
        token: null,
        user: null
      };
    }

    try {
      return {
        token,
        user: storedUser ? JSON.parse(storedUser) : extractUser({}, "", token)
      };
    } catch {
      return {
        token,
        user: extractUser({}, "", token)
      };
    }
  }
};