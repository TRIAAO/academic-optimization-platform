import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  ArrowRight,
  BookOpenCheck,
  BrainCircuit,
  CircleGauge,
  FileSearch,
  GraduationCap,
  Info,
  Lightbulb,
  Network,
  RefreshCw,
  ShieldCheck,
  Sparkles,
  Tags,
  UsersRound
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { recommendationService } from "../services/recommendationService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

const LEVELS = {
  STRONG: {
    label: "Evidência forte",
    variant: "green",
    ring: "stroke-emerald-400",
    text: "text-emerald-300"
  },
  MODERATE: {
    label: "Evidência moderada",
    variant: "amber",
    ring: "stroke-amber-400",
    text: "text-amber-300"
  },
  INITIAL: {
    label: "Evidência inicial",
    variant: "red",
    ring: "stroke-rose-400",
    text: "text-rose-300"
  }
};

const CONFIDENCE = {
  HIGH: { label: "Alta confiança", variant: "green" },
  MEDIUM: { label: "Confiança média", variant: "amber" },
  LOW: { label: "Baixa confiança", variant: "slate" }
};

const PRIORITIES = {
  HIGH: { label: "Alta", variant: "red" },
  MEDIUM: { label: "Média", variant: "amber" },
  LOW: { label: "Baixa", variant: "blue" }
};

const SOURCES = {
  PERFIL_E_PRODUCAO: "Perfil + produção",
  PERFIL: "Perfil acadêmico",
  PRODUCAO_CIENTIFICA: "Produção científica"
};

function confidenceConfig(value) {
  return CONFIDENCE[String(value || "").toUpperCase()] || CONFIDENCE.LOW;
}

function priorityConfig(value) {
  return PRIORITIES[String(value || "").toUpperCase()] || PRIORITIES.LOW;
}

function EvidenceScore({ score, level }) {
  const config = LEVELS[level] || LEVELS.INITIAL;
  const normalized = Math.min(100, Math.max(0, Number(score || 0)));
  const circumference = 2 * Math.PI * 42;
  const offset = circumference - (normalized / 100) * circumference;

  return (
    <div className="flex items-center gap-5">
      <div className="relative h-28 w-28 shrink-0">
        <svg className="h-28 w-28 -rotate-90" viewBox="0 0 100 100">
          <circle
            cx="50"
            cy="50"
            r="42"
            fill="none"
            strokeWidth="7"
            className="stroke-slate-700"
          />
          <circle
            cx="50"
            cy="50"
            r="42"
            fill="none"
            strokeWidth="7"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={offset}
            className={config.ring}
          />
        </svg>

        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-3xl font-black text-white">{normalized}</span>
          <span className="text-[10px] font-bold uppercase text-slate-400">
            de 100
          </span>
        </div>
      </div>

      <div>
        <p className="text-[11px] font-black uppercase tracking-[0.24em] text-blue-300">
          Base da recomendação
        </p>
        <p className={`mt-2 text-lg font-black ${config.text}`}>
          {config.label}
        </p>
        <p className="mt-2 max-w-sm text-sm leading-6 text-slate-300">
          Quanto mais fontes confirmadas, mais confiável fica a priorização.
        </p>
      </div>
    </div>
  );
}

function EvidenceItem({ icon: Icon, label, value, detail, healthy = true }) {
  return (
    <div className="flex min-w-0 items-center gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <div
        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${
          healthy
            ? "bg-emerald-50 text-emerald-700"
            : "bg-slate-100 text-slate-500"
        }`}
      >
        <Icon className="h-5 w-5" />
      </div>

      <div className="min-w-0">
        <p className="truncate text-xs font-bold uppercase tracking-wide text-slate-500">
          {label}
        </p>
        <p className="mt-1 font-black text-slate-950">{value}</p>
        {detail && <p className="mt-0.5 truncate text-xs text-slate-500">{detail}</p>}
      </div>
    </div>
  );
}

function SectionTitle({ eyebrow, title, description, icon: Icon }) {
  return (
    <div className="mb-4 flex items-start justify-between gap-4">
      <div>
        <p className="text-[11px] font-black uppercase tracking-[0.24em] text-blue-700">
          {eyebrow}
        </p>
        <h3 className="mt-2 text-xl font-black text-slate-950">{title}</h3>
        {description && (
          <p className="mt-1 text-sm leading-6 text-slate-500">{description}</p>
        )}
      </div>

      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-700">
        <Icon className="h-5 w-5" />
      </div>
    </div>
  );
}

function CompactEmpty({ icon, title, description }) {
  const Icon = icon;

  return (
    <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-6 text-center">
      <Icon className="mx-auto h-6 w-6 text-slate-400" />
      <p className="mt-3 font-bold text-slate-800">{title}</p>
      <p className="mx-auto mt-1 max-w-lg text-sm leading-6 text-slate-500">
        {description}
      </p>
    </div>
  );
}

export default function AcademicRecommendations() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [recommendations, setRecommendations] = useState(null);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingRecommendations, setLoadingRecommendations] = useState(false);
  const [error, setError] = useState("");

  const selectedResearcher = useMemo(
    () => researchers.find((item) => item.id === selectedResearcherId),
    [researchers, selectedResearcherId]
  );

  async function loadResearchers() {
    setLoadingResearchers(true);
    setError("");

    try {
      const data = await researcherService.findAll();
      const requestedId = searchParams.get("researcherId");
      const initialId = data.some((item) => item.id === requestedId)
        ? requestedId
        : data[0]?.id || "";

      setResearchers(data);
      setSelectedResearcherId(initialId);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar pesquisadores.");
    } finally {
      setLoadingResearchers(false);
    }
  }

  async function loadRecommendations(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingRecommendations(true);
    setError("");

    try {
      setRecommendations(
        await recommendationService.generateByResearcher(researcherId)
      );
    } catch (apiError) {
      setRecommendations(null);
      setError(
        apiError?.message ||
          "Não foi possível gerar as recomendações acadêmicas."
      );
    } finally {
      setLoadingRecommendations(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      loadRecommendations(selectedResearcherId);
    }
  }, [selectedResearcherId]);

  function handleResearcherChange(event) {
    const researcherId = event.target.value;
    setSelectedResearcherId(researcherId);
    setSearchParams(researcherId ? { researcherId } : {});
  }

  if (loadingResearchers) {
    return <LoadingState message="Preparando recomendações acadêmicas..." />;
  }

  if (!loadingResearchers && researchers.length === 0) {
    return (
      <EmptyState
        icon={UsersRound}
        title="Nenhum pesquisador cadastrado"
        description="Cadastre um pesquisador e consolide o perfil acadêmico para gerar recomendações baseadas em evidências."
        action={
          <Link to="/admin/researchers">
            <PrimaryButton icon={ArrowRight}>Cadastrar pesquisador</PrimaryButton>
          </Link>
        }
      />
    );
  }

  const evidence = recommendations?.evidence;

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Inteligência acadêmica responsável"
        title="Recomendações Acadêmicas Inteligentes"
        description="Priorize temas, relações de colaboração e veículos científicos com base nos dados acadêmicos consolidados e rastreáveis."
        actions={
          <PrimaryButton
            variant="light"
            icon={RefreshCw}
            loading={loadingRecommendations}
            onClick={() => loadRecommendations()}
          >
            Atualizar análise
          </PrimaryButton>
        }
      >
        <label className="block max-w-xl">
          <span className="text-sm font-bold text-slate-700">Pesquisador</span>
          <select
            value={selectedResearcherId}
            onChange={handleResearcherChange}
            className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
          >
            {researchers.map((researcher) => (
              <option key={researcher.id} value={researcher.id}>
                {researcher.fullName} — {researcher.institution || "Sem instituição"}
              </option>
            ))}
          </select>
        </label>
      </PageHeader>

      {error && (
        <ErrorState title="Atenção ao gerar recomendações" message={error} />
      )}

      {loadingRecommendations && !recommendations && (
        <LoadingState message="Analisando perfil, produção e fontes confirmadas..." />
      )}

      {recommendations && (
        <>
          <section className="overflow-hidden rounded-[2rem] bg-slate-950 p-6 shadow-lg sm:p-8">
            <div className="grid gap-7 lg:grid-cols-[auto_1fr] lg:items-center">
              <EvidenceScore
                score={recommendations.evidenceScore}
                level={recommendations.evidenceLevel}
              />

              <div className="border-t border-slate-800 pt-6 lg:border-l lg:border-t-0 lg:pl-8 lg:pt-0">
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant={LEVELS[recommendations.evidenceLevel]?.variant || "slate"}>
                    Análise explicável
                  </Badge>
                  <span className="text-xs font-semibold text-slate-400">
                    Atualizada em {formatDateTime(recommendations.generatedAt)}
                  </span>
                </div>

                <h2 className="mt-4 text-2xl font-black text-white sm:text-3xl">
                  {recommendations.researcherName || selectedResearcher?.fullName}
                </h2>
                <p className="mt-2 text-sm font-semibold text-blue-300">
                  {recommendations.researchArea}
                </p>
                <p className="mt-4 max-w-3xl text-sm leading-7 text-slate-300">
                  As sugestões abaixo são ordenadas por relevância observada. A decisão final deve considerar estratégia institucional, escopo editorial e validação humana.
                </p>
              </div>
            </div>
          </section>

          <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-6">
            <EvidenceItem
              icon={GraduationCap}
              label="Perfil"
              value={`${formatNumber(evidence?.profileCompletionPercentage)}%`}
              detail="preenchimento"
              healthy={Number(evidence?.profileCompletionPercentage) >= 70}
            />
            <EvidenceItem
              icon={BookOpenCheck}
              label="ORCID"
              value={formatNumber(evidence?.orcidWorks)}
              detail="obras"
              healthy={Number(evidence?.orcidWorks) > 0}
            />
            <EvidenceItem
              icon={Network}
              label="OpenAlex"
              value={`${formatNumber(evidence?.confirmedOpenAlexWorks)} / ${formatNumber(evidence?.openAlexWorks)}`}
              detail="confirmadas"
              healthy={Number(evidence?.confirmedOpenAlexWorks) > 0}
            />
            <EvidenceItem
              icon={FileSearch}
              label="Crossref"
              value={formatNumber(evidence?.crossrefValidations)}
              detail="validações"
              healthy={Number(evidence?.crossrefValidations) > 0}
            />
            <EvidenceItem
              icon={CircleGauge}
              label="Métricas"
              value={evidence?.scientometricSnapshotAvailable ? "Registradas" : "Pendentes"}
              healthy={evidence?.scientometricSnapshotAvailable}
            />
            <EvidenceItem
              icon={UsersRound}
              label="Relações"
              value={evidence?.openAlexEnrichmentAvailable ? "Atualizadas" : "Local"}
              detail={evidence?.openAlexEnrichmentAvailable ? "via OpenAlex" : "sem enriquecimento"}
              healthy={evidence?.openAlexEnrichmentAvailable}
            />
          </section>

          <div className="grid gap-6 xl:grid-cols-[1.08fr_0.92fr]">
            <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <SectionTitle
                eyebrow="Direção temática"
                title="Palavras-chave sugeridas"
                description="Temas do perfil e da produção consolidada, ordenados pela evidência encontrada."
                icon={Tags}
              />

              {recommendations.keywords?.length ? (
                <div className="grid gap-3 sm:grid-cols-2">
                  {recommendations.keywords.map((item) => {
                    const confidence = confidenceConfig(item.confidence);

                    return (
                      <article
                        key={`${item.keyword}-${item.source}`}
                        className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                      >
                        <div className="flex items-start justify-between gap-3">
                          <p className="font-black text-slate-950">{item.keyword}</p>
                          <span className="shrink-0 text-lg font-black text-blue-700">
                            {formatNumber(item.relevanceScore)}
                          </span>
                        </div>
                        <p className="mt-2 text-sm leading-6 text-slate-500">
                          {item.rationale}
                        </p>
                        <div className="mt-3 flex flex-wrap gap-2">
                          <Badge variant={confidence.variant}>{confidence.label}</Badge>
                          <Badge variant="blue">{SOURCES[item.source] || item.source}</Badge>
                        </div>
                      </article>
                    );
                  })}
                </div>
              ) : (
                <CompactEmpty
                  icon={Tags}
                  title="Base temática insuficiente"
                  description="Complete palavras-chave e consolide obras para identificar temas recorrentes."
                />
              )}
            </section>

            <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <SectionTitle
                eyebrow="Rede científica"
                title="Colaboradores relacionados"
                description="Coautorias públicas identificadas no OpenAlex; confirme o vínculo antes de qualquer contato."
                icon={UsersRound}
              />

              {recommendations.collaborators?.length ? (
                <div className="space-y-3">
                  {recommendations.collaborators.map((item) => {
                    const confidence = confidenceConfig(item.confidence);

                    return (
                      <article
                        key={item.openAlexAuthorId || item.displayName}
                        className="rounded-2xl border border-slate-200 p-4"
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div className="min-w-0">
                            <p className="truncate font-black text-slate-950">
                              {item.displayName}
                            </p>
                            <p className="mt-1 truncate text-sm text-slate-500">
                              {item.institution}
                            </p>
                          </div>
                          <Badge variant={confidence.variant}>{confidence.label}</Badge>
                        </div>

                        <div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-xs font-bold text-slate-600">
                          <span>{formatNumber(item.sharedWorks)} obra(s) em comum</span>
                          <span>{formatNumber(item.sharedCitations)} citações</span>
                          <span>{item.latestCollaborationYear || "Ano não informado"}</span>
                        </div>
                      </article>
                    );
                  })}
                </div>
              ) : (
                <CompactEmpty
                  icon={Network}
                  title="Sem relações confirmadas nesta análise"
                  description={
                    evidence?.openAlexEnrichmentAvailable
                      ? "O OpenAlex não retornou coautorias relacionadas para este pesquisador."
                      : "A análise local continua disponível; revalide ORCID e OpenAlex para enriquecer a rede científica."
                  }
                />
              )}
            </section>
          </div>

          <div className="grid gap-6 xl:grid-cols-[1fr_0.92fr]">
            <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <SectionTitle
                eyebrow="Histórico editorial"
                title="Veículos científicos relacionados"
                description="Fontes recorrentes no histórico; não é previsão de aceite nem ranking de qualidade."
                icon={BookOpenCheck}
              />

              {recommendations.journals?.length ? (
                <div className="overflow-hidden rounded-2xl border border-slate-200">
                  {recommendations.journals.map((item, index) => {
                    const confidence = confidenceConfig(item.confidence);

                    return (
                      <article
                        key={item.journalName}
                        className={`p-4 ${index ? "border-t border-slate-200" : ""}`}
                      >
                        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                          <div>
                            <p className="font-black text-slate-950">{item.journalName}</p>
                            <p className="mt-1 text-sm leading-6 text-slate-500">
                              {item.rationale}
                            </p>
                          </div>
                          <div className="flex shrink-0 items-center gap-2">
                            <Badge variant={confidence.variant}>{confidence.label}</Badge>
                            <span className="text-lg font-black text-blue-700">
                              {formatNumber(item.relevanceScore)}
                            </span>
                          </div>
                        </div>

                        <div className="mt-3 flex flex-wrap gap-x-4 gap-y-2 text-xs font-bold text-slate-600">
                          <span>{formatNumber(item.relatedWorks)} obra(s)</span>
                          <span>{formatNumber(item.totalCitations)} citações</span>
                          <span>{formatNumber(item.validatedDoiWorks)} DOI validado(s)</span>
                          <span>{formatNumber(item.openAccessWorks)} acesso aberto</span>
                        </div>
                      </article>
                    );
                  })}
                </div>
              ) : (
                <CompactEmpty
                  icon={BookOpenCheck}
                  title="Fontes ainda não consolidadas"
                  description="Complete os metadados das obras via ORCID, OpenAlex e Crossref."
                />
              )}
            </section>

            <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <SectionTitle
                eyebrow="Próximos passos"
                title="Como elevar a confiança"
                description="Ações ordenadas pelas lacunas observadas nesta análise."
                icon={Lightbulb}
              />

              <div className="space-y-3">
                {(recommendations.nextActions || []).map((item, index) => {
                  const priority = priorityConfig(item.priority);

                  return (
                    <article
                      key={`${item.area}-${item.title}`}
                      className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                    >
                      <div className="flex items-start gap-3">
                        <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-white text-sm font-black text-blue-700 shadow-sm">
                          {index + 1}
                        </span>
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <Badge variant={priority.variant}>{priority.label}</Badge>
                            <Badge variant="slate">{item.area?.replaceAll("_", " ")}</Badge>
                          </div>
                          <p className="mt-3 font-black text-slate-950">{item.title}</p>
                          <p className="mt-1 text-sm leading-6 text-slate-500">
                            {item.description}
                          </p>
                          <Link
                            to={item.targetModule || "/admin/optimization"}
                            className="mt-3 inline-flex items-center gap-2 text-sm font-black text-blue-700 hover:text-blue-900"
                          >
                            Executar ação <ArrowRight className="h-4 w-4" />
                          </Link>
                        </div>
                      </div>
                    </article>
                  );
                })}
              </div>
            </section>
          </div>

          <section className="grid gap-4 rounded-3xl border border-blue-200 bg-blue-50 p-5 sm:p-6 lg:grid-cols-2">
            <div className="flex gap-4">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white text-blue-700">
                <BrainCircuit className="h-5 w-5" />
              </div>
              <div>
                <p className="font-black text-blue-950">Metodologia transparente</p>
                <p className="mt-2 text-sm leading-6 text-blue-900/75">
                  {recommendations.methodology}
                </p>
              </div>
            </div>

            <div className="flex gap-4 border-t border-blue-200 pt-4 lg:border-l lg:border-t-0 lg:pl-6 lg:pt-0">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white text-emerald-700">
                <ShieldCheck className="h-5 w-5" />
              </div>
              <div>
                <p className="font-black text-blue-950">Governança e segurança</p>
                <p className="mt-2 text-sm leading-6 text-blue-900/75">
                  {recommendations.googleScholarPolicy}
                </p>
              </div>
            </div>
          </section>

          <div className="flex items-center gap-2 px-2 text-xs text-slate-500">
            <Info className="h-4 w-4" />
            <span>
              A pontuação expressa cobertura da evidência disponível, não qualidade individual do pesquisador.
            </span>
            <Sparkles className="ml-auto hidden h-4 w-4 text-blue-500 sm:block" />
          </div>
        </>
      )}
    </div>
  );
}
