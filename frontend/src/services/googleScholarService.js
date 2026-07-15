import { academicProfileService } from "./academicProfileService";
import { crossrefService } from "./crossrefService";
import { openAlexService } from "./openAlexService";
import { orcidService } from "./orcidService";
import { researcherService } from "./researcherService";

function safeArray(value) {
  return Array.isArray(value) ? value : [];
}

function hasText(value) {
  return typeof value === "string" && value.trim().length > 0;
}

function countByStatus(works, status) {
  return safeArray(works).filter(
    (work) => String(work.reviewStatus || "").toUpperCase() === status
  ).length;
}

function makeItem({
  title,
  description,
  status = "PENDING",
  priority = "MÉDIA",
  evidence,
  action
}) {
  return {
    title,
    description,
    status,
    priority,
    evidence,
    action
  };
}

function buildPersonalizedChecklist({
  researcher,
  profile,
  orcidWorks,
  openAlexWorks,
  crossrefValidations
}) {
  const items = [];

  const confirmedWorks = countByStatus(openAlexWorks, "CONFIRMED");
  const pendingWorks = countByStatus(openAlexWorks, "PENDING_REVIEW");
  const rejectedWorks = countByStatus(openAlexWorks, "REJECTED");

  items.push(
    makeItem({
      title: "Conferir nome acadêmico",
      description: hasText(researcher?.fullName)
        ? `Usar no Google Acadêmico o mesmo nome acadêmico cadastrado na plataforma: ${researcher.fullName}.`
        : "O pesquisador ainda não tem nome completo cadastrado corretamente.",
      status: hasText(researcher?.fullName) ? "OK" : "ACTION_REQUIRED",
      priority: hasText(researcher?.fullName) ? "BAIXA" : "ALTA",
      evidence: researcher?.fullName || "Nome não informado",
      action: hasText(researcher?.fullName)
        ? "Confirmar manualmente se o nome no Google Acadêmico está igual."
        : "Atualizar o cadastro do pesquisador antes da revisão manual."
    })
  );

  items.push(
    makeItem({
      title: "Conferir e-mail institucional",
      description: hasText(researcher?.email)
        ? `Verificar se o Google Acadêmico usa ou permite confirmar o e-mail ${researcher.email}.`
        : "O pesquisador não possui e-mail cadastrado.",
      status: hasText(researcher?.email) ? "OK" : "ACTION_REQUIRED",
      priority: hasText(researcher?.email) ? "MÉDIA" : "ALTA",
      evidence: researcher?.email || "E-mail não informado",
      action: hasText(researcher?.email)
        ? "Confirmar manualmente o e-mail no perfil Google Acadêmico."
        : "Cadastrar e-mail institucional do pesquisador."
    })
  );

  items.push(
    makeItem({
      title: "Padronizar afiliação institucional",
      description: hasText(researcher?.institution)
        ? `A afiliação cadastrada é: ${researcher.institution}. Verificar manualmente se o Google Acadêmico apresenta a mesma instituição ou a afiliação correta do pesquisador.`
        : "A instituição do pesquisador não está preenchida.",
      status: hasText(researcher?.institution) ? "OK" : "ACTION_REQUIRED",
      priority: hasText(researcher?.institution) ? "MÉDIA" : "ALTA",
      evidence: researcher?.institution || "Instituição não informada",
      action: hasText(researcher?.institution)
        ? "Conferir manualmente a afiliação no Google Acadêmico."
        : "Preencher instituição no cadastro do pesquisador."
    })
  );

  items.push(
    makeItem({
      title: "Validar área de pesquisa",
      description: hasText(profile?.researchArea)
        ? `Área cadastrada no perfil acadêmico: ${profile.researchArea}.`
        : "O perfil acadêmico ainda não possui área de pesquisa definida.",
      status: hasText(profile?.researchArea) ? "OK" : "ACTION_REQUIRED",
      priority: hasText(profile?.researchArea) ? "BAIXA" : "MÉDIA",
      evidence: profile?.researchArea || "Área de pesquisa não informada",
      action: hasText(profile?.researchArea)
        ? "Usar essa área como referência para revisar áreas de interesse no Google Acadêmico."
        : "Preencher área de pesquisa no Perfil Acadêmico."
    })
  );

  items.push(
    makeItem({
      title: "Conferir palavras-chave científicas",
      description: hasText(profile?.keywords)
        ? `Palavras-chave cadastradas: ${profile.keywords}.`
        : "O perfil acadêmico ainda não possui palavras-chave.",
      status: hasText(profile?.keywords) ? "OK" : "ACTION_REQUIRED",
      priority: hasText(profile?.keywords) ? "BAIXA" : "MÉDIA",
      evidence: profile?.keywords || "Palavras-chave não informadas",
      action: hasText(profile?.keywords)
        ? "Usar essas palavras-chave como base para revisar interesses no Google Acadêmico."
        : "Adicionar palavras-chave no Perfil Acadêmico."
    })
  );

  items.push(
    makeItem({
      title: "Verificar ORCID do pesquisador",
      description: hasText(researcher?.orcidId)
        ? `ORCID cadastrado: ${researcher.orcidId}.`
        : "O pesquisador não possui ORCID cadastrado.",
      status: hasText(researcher?.orcidId) ? "OK" : "ACTION_REQUIRED",
      priority: hasText(researcher?.orcidId) ? "MÉDIA" : "ALTA",
      evidence: researcher?.orcidId || "ORCID não informado",
      action: hasText(researcher?.orcidId)
        ? "Conferir manualmente se o ORCID está visível ou coerente no perfil acadêmico público."
        : "Cadastrar ORCID no cadastro do pesquisador."
    })
  );

  items.push(
    makeItem({
      title: "Comparar publicações com ORCID",
      description:
        safeArray(orcidWorks).length > 0
          ? `Foram encontradas ${orcidWorks.length} obras vinculadas ao ORCID.`
          : "Nenhuma obra ORCID foi encontrada para comparação.",
      status: safeArray(orcidWorks).length > 0 ? "OK" : "PENDING",
      priority: safeArray(orcidWorks).length > 0 ? "MÉDIA" : "ALTA",
      evidence: `${orcidWorks.length} obra(s) ORCID`,
      action:
        safeArray(orcidWorks).length > 0
          ? "Comparar manualmente se as principais obras aparecem corretamente no Google Acadêmico."
          : "Importar obras ORCID ou verificar se o ORCID do pesquisador possui obras públicas."
    })
  );

  items.push(
    makeItem({
      title: "Comparar publicações com OpenAlex",
      description:
        safeArray(openAlexWorks).length > 0
          ? `Foram encontradas ${openAlexWorks.length} obras OpenAlex: ${confirmedWorks} confirmadas, ${pendingWorks} pendentes e ${rejectedWorks} rejeitadas.`
          : "Nenhuma obra OpenAlex foi encontrada para este pesquisador.",
      status:
        safeArray(openAlexWorks).length > 0 && pendingWorks === 0
          ? "OK"
          : "PENDING",
      priority: pendingWorks > 0 ? "ALTA" : "MÉDIA",
      evidence: `${openAlexWorks.length} obra(s) OpenAlex`,
      action:
        pendingWorks > 0
          ? "Concluir revisão manual das obras pendentes antes de orientar o pesquisador."
          : "Usar as obras confirmadas como referência para conferência manual no Google Acadêmico."
    })
  );

  items.push(
    makeItem({
      title: "Validar DOI e metadados no Crossref",
      description:
        safeArray(crossrefValidations).length > 0
          ? `Existem ${crossrefValidations.length} validações Crossref registradas.`
          : "Ainda não existem validações Crossref para este pesquisador.",
      status: safeArray(crossrefValidations).length > 0 ? "OK" : "PENDING",
      priority: safeArray(crossrefValidations).length > 0 ? "BAIXA" : "MÉDIA",
      evidence: `${crossrefValidations.length} validação(ões) Crossref`,
      action:
        safeArray(crossrefValidations).length > 0
          ? "Usar os metadados validados como referência para conferir publicações no Google Acadêmico."
          : "Validar DOIs das obras com DOI no módulo Crossref."
    })
  );

  items.push(
    makeItem({
      title: "Verificar URL do perfil Google Acadêmico",
      description: hasText(profile?.googleScholarUrl)
        ? `URL cadastrada manualmente: ${profile.googleScholarUrl}.`
        : "A URL do perfil Google Acadêmico ainda não foi cadastrada no Perfil Acadêmico.",
      status: hasText(profile?.googleScholarUrl) ? "OK" : "ACTION_REQUIRED",
      priority: hasText(profile?.googleScholarUrl) ? "BAIXA" : "ALTA",
      evidence: profile?.googleScholarUrl || "URL não cadastrada",
      action: hasText(profile?.googleScholarUrl)
        ? "Abrir manualmente a URL e conferir dados públicos do pesquisador."
        : "Cadastrar manualmente a URL do Google Acadêmico no Perfil Acadêmico."
    })
  );

  items.push(
    makeItem({
      title: "Remover ou ignorar obras de homônimos",
      description:
        pendingWorks > 0 || rejectedWorks > 0
          ? `Existem ${pendingWorks} obras pendentes e ${rejectedWorks} obras rejeitadas no OpenAlex. Isso indica necessidade de atenção com possíveis homônimos.`
          : "Não há indicação atual de obras pendentes ou rejeitadas na revisão OpenAlex.",
      status: pendingWorks > 0 || rejectedWorks > 0 ? "PENDING" : "OK",
      priority: pendingWorks > 0 ? "ALTA" : "MÉDIA",
      evidence: `${pendingWorks} pendente(s), ${rejectedWorks} rejeitada(s)`,
      action:
        pendingWorks > 0 || rejectedWorks > 0
          ? "Orientar o pesquisador a conferir manualmente obras atribuídas incorretamente no Google Acadêmico."
          : "Manter revisão periódica para evitar associação incorreta de publicações."
    })
  );

  items.push(
    makeItem({
      title: "Não automatizar Google Acadêmico",
      description:
        "A plataforma não acessa, não altera, não automatiza e não coleta dados diretamente do Google Acadêmico.",
      status: "SECURITY",
      priority: "ALTA",
      evidence: "Regra fixa de segurança institucional",
      action:
        "Toda conferência no Google Acadêmico deve ser feita manualmente pelo pesquisador ou pela equipa autorizada."
    })
  );

  return items;
}

export const googleScholarService = {
  async buildChecklistByResearcher(researcherId) {
    const researcher = await researcherService.findById(researcherId);

    let profile = null;
    let orcidWorks = [];
    let openAlexWorks = [];
    let crossrefValidations = [];

    try {
      profile = await academicProfileService.findByResearcherId(researcherId);
    } catch {
      profile = null;
    }

    try {
      orcidWorks = await orcidService.findWorksByResearcher(researcherId);
    } catch {
      orcidWorks = [];
    }

    try {
      openAlexWorks = await openAlexService.findWorksByResearcher(researcherId);
    } catch {
      openAlexWorks = [];
    }

    try {
      crossrefValidations =
        await crossrefService.findValidationsByResearcher(researcherId);
    } catch {
      crossrefValidations = [];
    }

    const checklist = buildPersonalizedChecklist({
      researcher,
      profile,
      orcidWorks,
      openAlexWorks,
      crossrefValidations
    });

    return {
      generatedAt: new Date().toISOString(),
      source: "FRONTEND_PERSONALIZED_GOOGLE_SCHOLAR_CHECKLIST",
      researcher,
      academicProfile: profile,
      summary: {
        totalItems: checklist.length,
        totalOk: checklist.filter((item) => item.status === "OK").length,
        totalPending: checklist.filter((item) => item.status === "PENDING")
          .length,
        totalActionRequired: checklist.filter(
          (item) => item.status === "ACTION_REQUIRED"
        ).length,
        totalSecurity: checklist.filter((item) => item.status === "SECURITY")
          .length,
        totalOrcidWorks: orcidWorks.length,
        totalOpenAlexWorks: openAlexWorks.length,
        totalCrossrefValidations: crossrefValidations.length
      },
      checklist,
      policy:
        "Google Acadêmico é apenas checklist e orientação manual. A plataforma não automatiza, não acessa, não altera e não coleta dados diretamente do Google Acadêmico."
    };
  }
};