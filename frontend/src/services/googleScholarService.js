import { requestByOpenApi } from "./endpointResolver";

export const DEFAULT_GOOGLE_SCHOLAR_CHECKLIST = [
  {
    title: "Criar ou revisar perfil manualmente",
    description:
      "O pesquisador deve acessar o Google Acadêmico manualmente e revisar nome, afiliação, área e e-mail institucional.",
    completed: false
  },
  {
    title: "Adicionar e-mail institucional",
    description:
      "Usar e-mail institucional quando disponível para reforçar vínculo acadêmico.",
    completed: false
  },
  {
    title: "Conferir publicações",
    description:
      "Confirmar manualmente publicações corretas e remover obras de homônimos.",
    completed: false
  },
  {
    title: "Padronizar nome acadêmico",
    description:
      "Usar a mesma variação de nome presente em ORCID, OpenAlex, Crossref e publicações.",
    completed: false
  },
  {
    title: "Adicionar áreas de interesse",
    description:
      "Inserir palavras-chave científicas coerentes com o perfil acadêmico.",
    completed: false
  },
  {
    title: "Não automatizar Google Acadêmico",
    description:
      "A plataforma apenas orienta. Não faz scraping, não acessa, não altera e não coleta dados diretamente do Google Acadêmico.",
    completed: false
  }
];

export const googleScholarService = {
  async getChecklistByResearcher(researcherId) {
    const result = await requestByOpenApi({
      method: "get",
      preferred: [
        "/api/v1/google-scholar/researchers/{researcherId}/checklist",
        "/api/v1/google-scholar-checklist/researchers/{researcherId}",
        "/api/v1/google-scholar/researchers/{researcherId}"
      ],
      keywords: ["google"],
      requiredParams: ["researcherId"],
      params: {
        researcherId
      }
    });

    return result.data;
  },

  async generateChecklistByResearcher(researcherId) {
    const result = await requestByOpenApi({
      method: "post",
      preferred: [
        "/api/v1/google-scholar/researchers/{researcherId}/checklist",
        "/api/v1/google-scholar-checklist/researchers/{researcherId}/generate",
        "/api/v1/google-scholar/researchers/{researcherId}/generate"
      ],
      keywords: ["google"],
      requiredParams: ["researcherId"],
      params: {
        researcherId
      }
    });

    return result.data;
  }
};