const apiBaseUrl =
  import.meta.env.VITE_API_BASE_URL || "https://academic-api.triacompany.com";

export const APP_CONFIG = {
  name: import.meta.env.VITE_APP_NAME || "Plataforma de Otimização Acadêmica",
  organization:
    import.meta.env.VITE_APP_ORGANIZATION ||
    "Universidade Metropolitana de Angola / IMETRO",
  executor: import.meta.env.VITE_APP_EXECUTOR || "TRIA Company",
  apiBaseUrl,
  openApiUrl: `${apiBaseUrl}/v3/api-docs`,
  googleScholarPolicy:
    "A plataforma não automatiza, não acessa, não altera e não coleta dados diretamente do Google Acadêmico. O módulo Google Acadêmico é apenas checklist e orientação manual."
};

export const STORAGE_KEYS = {
  token: "academic_optimization_token",
  user: "academic_optimization_user"
};