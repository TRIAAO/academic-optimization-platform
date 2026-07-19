import { apiClient } from "./apiClient";
import { STORAGE_KEYS } from "../config/app";
import {
  extractToken,
  extractUser,
  isTokenExpired
} from "../utils/jwt";

function clearStoredSession() {
  localStorage.removeItem(STORAGE_KEYS.token);
  localStorage.removeItem(STORAGE_KEYS.user);
}

export const authService = {
  async login({ email, password }) {
    clearStoredSession();

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
    clearStoredSession();
  },

  getStoredSession() {
    const token = localStorage.getItem(STORAGE_KEYS.token);
    const storedUser = localStorage.getItem(STORAGE_KEYS.user);

    if (!token || isTokenExpired(token)) {
      clearStoredSession();

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
