import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  ArrowRight,
  BookOpenCheck,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  ExternalLink,
  FileSearch,
  Languages,
  PencilLine,
  RefreshCw,
  Save,
  ShieldCheck,
  Tags,
  UploadCloud,
  UsersRound,
  X
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { abstractAnalysisService } from "../services/abstractAnalysisService";
import { openAlexService } from "../services/openAlexService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

const EVIDENCE_LEVELS = {
  CONSOLIDADA: { label: "Cobertura consolidada", variant: "green", ring: "stroke-emerald-400" },
  MODERADA: { label: "Cobertura moderada", variant: "amber", ring: "stroke-amber-400" },
  INICIAL: { label: "Cobertura inicial", variant: "red", ring: "stroke-rose-400" },
  "SEM_EVIDÊNCIA": { label: "Sem evidência", variant: "slate", ring: "stroke-slate-500" }
};

const STATUS = {
  COMPLETE: { label: "PT–EN completo", variant: "green" },
  PT_MISSING: { label: "PT pendente", variant: "amber" },
  EN_MISSING: { label: "EN pendente", variant: "amber" },
  PT_AND_EN_MISSING: { label: "PT e EN pendentes", variant: "red" },
  NO_ABSTRACT: { label: "Sem abstract", variant: "slate" }
};

const CONFIDENCE = {
  ALTA: { label: "Alta confiança", variant: "green" },
  "MÉDIA": { label: "Confiança média", variant: "amber" },
  INICIAL: { label: "Evidência inicial", variant: "slate" }
};

function CoverageScore({ value, level }) {
  const normalized = Math.min(100, Math.max(0, Number(value || 0)));
  const config = EVIDENCE_LEVELS[level] || EVIDENCE_LEVELS["SEM_EVIDÊNCIA"];
  const circumference = 2 * Math.PI * 38;
  const offset = circumference - (normalized / 100) * circumference;

  return (
    <div className="flex items-center gap-5">
      <div className="relative h-24 w-24 shrink-0">
        <svg className="h-24 w-24 -rotate-90" viewBox="0 0 92 92">
          <circle cx="46" cy="46" r="38" fill="none" strokeWidth="7" className="stroke-slate-700" />
          <circle
            cx="46"
            cy="46"
            r="38"
            fill="none"
            strokeWidth="7"
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={offset}
            className={config.ring}
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-2xl font-black text-white">{normalized}%</span>
          <span className="text-[9px] font-bold uppercase text-slate-400">abstracts</span>
        </div>
      </div>
      <div>
        <p className="text-[11px] font-black uppercase tracking-[0.22em] text-blue-300">
          Cobertura documental
        </p>
        <p className="mt-2 text-lg font-black text-white">{config.label}</p>
        <p className="mt-1 text-sm leading-6 text-slate-300">
          Calculada apenas sobre obras confirmadas.
        </p>
      </div>
    </div>
  );
}

function MetricCard({ label, value, detail, icon: Icon, healthy = true }) {
  return (
    <div className="flex min-w-0 items-center gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${healthy ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-500"}`}>
        <Icon className="h-5 w-5" />
      </div>
      <div className="min-w-0">
        <p className="truncate text-[11px] font-bold uppercase tracking-wide text-slate-500">{label}</p>
        <p className="mt-1 font-black text-slate-950">{value}</p>
        <p className="truncate text-xs text-slate-500">{detail}</p>
      </div>
    </div>
  );
}

function TranslationEditor({ work, saving, onCancel, onSave }) {
  const [abstractPt, setAbstractPt] = useState(work.abstractPt || "");
  const [abstractEn, setAbstractEn] = useState(work.abstractEn || "");

  return (
    <div className="mt-4 grid gap-4 border-t border-slate-200 pt-4 lg:grid-cols-2">
      <label className="block">
        <span className="text-xs font-black uppercase tracking-wide text-slate-600">Versão em português</span>
        <textarea
          value={abstractPt}
          onChange={(event) => setAbstractPt(event.target.value)}
          rows={7}
          placeholder="Insira a tradução PT revista manualmente."
          className="mt-2 w-full rounded-2xl border border-slate-200 bg-white p-3 text-sm leading-6 text-slate-800 outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
        />
      </label>
      <label className="block">
        <span className="text-xs font-black uppercase tracking-wide text-slate-600">English version</span>
        <textarea
          value={abstractEn}
          onChange={(event) => setAbstractEn(event.target.value)}
          rows={7}
          placeholder="Insert the manually reviewed EN translation."
          className="mt-2 w-full rounded-2xl border border-slate-200 bg-white p-3 text-sm leading-6 text-slate-800 outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
        />
      </label>
      <div className="flex flex-wrap gap-3 lg:col-span-2">
        <PrimaryButton icon={Save} loading={saving} onClick={() => onSave({ abstractPt, abstractEn })}>
          Guardar versões
        </PrimaryButton>
        <PrimaryButton variant="light" icon={X} disabled={saving} onClick={onCancel}>
          Cancelar
        </PrimaryButton>
      </div>
    </div>
  );
}

export default function AbstractAnalysis() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [analysis, setAnalysis] = useState(null);
  const [expandedWorkId, setExpandedWorkId] = useState("");
  const [editingWorkId, setEditingWorkId] = useState("");
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

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
      const initialId = data.some((item) => item.id === requestedId) ? requestedId : data[0]?.id || "";
      setResearchers(data);
      setSelectedResearcherId(initialId);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar pesquisadores.");
    } finally {
      setLoadingResearchers(false);
    }
  }

  async function loadAnalysis(researcherId = selectedResearcherId) {
    if (!researcherId) return;
    setLoadingAnalysis(true);
    setError("");
    try {
      setAnalysis(await abstractAnalysisService.analyzeByResearcher(researcherId));
    } catch (apiError) {
      setAnalysis(null);
      setError(apiError?.message || "Não foi possível analisar os abstracts.");
    } finally {
      setLoadingAnalysis(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      setExpandedWorkId("");
      setEditingWorkId("");
      setSuccess("");
      loadAnalysis(selectedResearcherId);
    }
  }, [selectedResearcherId]);

  function handleResearcherChange(event) {
    const researcherId = event.target.value;
    setSelectedResearcherId(researcherId);
    setSearchParams(researcherId ? { researcherId } : {});
  }

  async function handleSyncAbstracts() {
    if (!selectedResearcherId) return;
    setSyncing(true);
    setError("");
    setSuccess("");
    try {
      const result = await openAlexService.syncAbstracts(selectedResearcherId);
      await loadAnalysis(selectedResearcherId);
      setSuccess(`${formatNumber(result.updatedWorks)} obra(s) atualizada(s); ${formatNumber(result.worksWithAbstract)} abstract(s) disponível(is).`);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível sincronizar abstracts do OpenAlex.");
    } finally {
      setSyncing(false);
    }
  }

  async function handleSaveTranslations(workId, translations) {
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      await abstractAnalysisService.updateTranslations(workId, translations);
      await loadAnalysis(selectedResearcherId);
      setEditingWorkId("");
      setExpandedWorkId(workId);
      setSuccess("Versões PT–EN guardadas com rastreabilidade.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível guardar as versões PT–EN.");
    } finally {
      setSaving(false);
    }
  }

  if (loadingResearchers) {
    return <LoadingState message="Preparando a análise de abstracts..." />;
  }

  if (!researchers.length) {
    return (
      <EmptyState
        icon={UsersRound}
        title="Nenhum pesquisador cadastrado"
        description="Cadastre um pesquisador para analisar a cobertura dos abstracts."
        action={<Link to="/admin/researchers"><PrimaryButton icon={ArrowRight}>Cadastrar pesquisador</PrimaryButton></Link>}
      />
    );
  }

  const level = EVIDENCE_LEVELS[analysis?.evidenceLevel] || EVIDENCE_LEVELS["SEM_EVIDÊNCIA"];

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Cobertura científica bilingue"
        title="Análise de Abstracts e Cobertura PT–EN"
        description="Identifique temas recorrentes e acompanhe traduções manuais, preservando o abstract original e a revisão humana."
        actions={
          <div className="flex flex-wrap gap-3">
            <PrimaryButton variant="light" icon={UploadCloud} loading={syncing} onClick={handleSyncAbstracts}>
              Sincronizar OpenAlex
            </PrimaryButton>
            <PrimaryButton variant="light" icon={RefreshCw} loading={loadingAnalysis} onClick={() => loadAnalysis()}>
              Atualizar análise
            </PrimaryButton>
          </div>
        }
      >
        <label className="block max-w-xl">
          <span className="text-sm font-bold text-slate-700">Pesquisador</span>
          <select
            value={selectedResearcherId}
            onChange={handleResearcherChange}
            className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-900 outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
          >
            {researchers.map((researcher) => (
              <option key={researcher.id} value={researcher.id}>
                {researcher.fullName} — {researcher.institution || "Sem instituição"}
              </option>
            ))}
          </select>
        </label>
      </PageHeader>

      {error && <ErrorState title="Atenção na análise de abstracts" message={error} />}
      {success && <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-sm font-bold text-emerald-800">{success}</div>}
      {loadingAnalysis && !analysis && <LoadingState message="Calculando cobertura e temas recorrentes..." />}

      {analysis && (
        <>
          <section className="overflow-hidden rounded-[2rem] bg-slate-950 p-6 shadow-lg sm:p-7">
            <div className="grid gap-6 lg:grid-cols-[auto_1fr] lg:items-center">
              <CoverageScore value={analysis.abstractCoveragePercentage} level={analysis.evidenceLevel} />
              <div className="border-t border-slate-800 pt-5 lg:border-l lg:border-t-0 lg:pl-7 lg:pt-0">
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant={level.variant}>{level.label}</Badge>
                  <span className="text-xs font-semibold text-slate-400">Atualizada em {formatDateTime(analysis.generatedAt)}</span>
                </div>
                <h2 className="mt-3 text-2xl font-black text-white">{analysis.researcherName || selectedResearcher?.fullName}</h2>
                <p className="mt-3 max-w-3xl text-sm leading-7 text-slate-300">
                  A leitura temática usa somente obras confirmadas. Traduções manuais complementam a cobertura, mas nunca substituem o abstract original.
                </p>
              </div>
            </div>
          </section>

          <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-6">
            <MetricCard icon={FileSearch} label="OpenAlex" value={formatNumber(analysis.totalOpenAlexWorks)} detail="obras importadas" healthy={analysis.totalOpenAlexWorks > 0} />
            <MetricCard icon={BookOpenCheck} label="Confirmadas" value={formatNumber(analysis.confirmedWorks)} detail="autoria validada" healthy={analysis.confirmedWorks > 0} />
            <MetricCard icon={CheckCircle2} label="Abstracts" value={formatNumber(analysis.worksWithAbstract)} detail={`${analysis.abstractCoveragePercentage}% de cobertura`} healthy={analysis.worksWithAbstract > 0} />
            <MetricCard icon={Languages} label="Português" value={`${analysis.portugueseCoveragePercentage}%`} detail="original ou versão PT" healthy={analysis.portugueseCoveragePercentage >= 80} />
            <MetricCard icon={Languages} label="Inglês" value={`${analysis.englishCoveragePercentage}%`} detail="original ou versão EN" healthy={analysis.englishCoveragePercentage >= 80} />
            <MetricCard icon={ShieldCheck} label="Pendentes" value={formatNumber(analysis.missingAbstracts)} detail="sem abstract original" healthy={analysis.missingAbstracts === 0} />
          </section>

          <div className="grid gap-6 xl:grid-cols-[1.08fr_0.92fr]">
            <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <div className="mb-4 flex items-start justify-between gap-4">
                <div>
                  <p className="text-[11px] font-black uppercase tracking-[0.22em] text-blue-700">Leitura temática</p>
                  <h3 className="mt-2 text-xl font-black text-slate-950">Temas recorrentes nos abstracts</h3>
                  <p className="mt-1 text-sm leading-6 text-slate-500">Expressões explicáveis, ordenadas pela recorrência observada.</p>
                </div>
                <Tags className="h-6 w-6 shrink-0 text-blue-700" />
              </div>

              {analysis.themes?.length ? (
                <div className="grid gap-3 sm:grid-cols-2">
                  {analysis.themes.map((theme) => {
                    const confidence = CONFIDENCE[theme.confidence] || CONFIDENCE.INICIAL;
                    return (
                      <article key={theme.theme} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <div className="flex items-start justify-between gap-3">
                          <p className="font-black text-slate-950">{theme.theme}</p>
                          <span className="text-lg font-black text-blue-700">{theme.relevanceScore}</span>
                        </div>
                        <p className="mt-2 text-sm leading-6 text-slate-500">{theme.rationale}</p>
                        <div className="mt-3 flex flex-wrap gap-2">
                          <Badge variant={confidence.variant}>{confidence.label}</Badge>
                          <Badge variant="blue">{theme.abstractCount} abstract(s)</Badge>
                        </div>
                      </article>
                    );
                  })}
                </div>
              ) : (
                <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-6 text-center">
                  <Tags className="mx-auto h-6 w-6 text-slate-400" />
                  <p className="mt-3 font-bold text-slate-800">Base temática insuficiente</p>
                  <p className="mt-1 text-sm text-slate-500">Sincronize abstracts e confirme as obras para identificar recorrências.</p>
                </div>
              )}
            </section>

            <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <p className="text-[11px] font-black uppercase tracking-[0.22em] text-blue-700">Plano de cobertura</p>
              <h3 className="mt-2 text-xl font-black text-slate-950">Próximas ações</h3>
              <div className="mt-4 space-y-3">
                {analysis.nextActions?.map((action) => (
                  <article key={`${action.priority}-${action.title}`} className="flex gap-3 rounded-2xl border border-slate-200 p-4">
                    <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-blue-50 text-sm font-black text-blue-700">{action.priority}</div>
                    <div>
                      <p className="font-black text-slate-950">{action.title}</p>
                      <p className="mt-1 text-sm leading-6 text-slate-500">{action.description}</p>
                    </div>
                  </article>
                ))}
              </div>
              <div className="mt-5 rounded-2xl border border-blue-200 bg-blue-50 p-4">
                <p className="text-sm font-black text-blue-950">Tradução manual e auditável</p>
                <p className="mt-1 text-sm leading-6 text-blue-900">{analysis.translationPolicy}</p>
              </div>
            </section>
          </div>

          <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-blue-700">Cobertura por obra</p>
                <h3 className="mt-2 text-xl font-black text-slate-950">Abstracts confirmados e versões PT–EN</h3>
                <p className="mt-1 text-sm leading-6 text-slate-500">Abra uma obra para consultar o original ou registar traduções revistas.</p>
              </div>
              <span className="text-sm font-bold text-slate-500">{formatNumber(analysis.works?.length)} obra(s)</span>
            </div>

            {analysis.works?.length ? (
              <div className="mt-5 space-y-3">
                {analysis.works.map((work) => {
                  const status = STATUS[work.translationStatus] || STATUS.NO_ABSTRACT;
                  const expanded = expandedWorkId === work.workId;
                  const editing = editingWorkId === work.workId;
                  return (
                    <article key={work.workId} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                        <div className="min-w-0">
                          <div className="flex flex-wrap items-center gap-2">
                            <Badge variant={status.variant}>{status.label}</Badge>
                            <Badge variant="slate">{String(work.originalLanguage || "idioma n/d").toUpperCase()}</Badge>
                            {work.publicationYear && <span className="text-xs font-bold text-slate-500">{work.publicationYear}</span>}
                          </div>
                          <h4 className="mt-2 font-black leading-6 text-slate-950">{work.title}</h4>
                          <p className="mt-1 text-sm text-slate-500">{work.sourceName || "Fonte não informada"}</p>
                        </div>
                        <div className="flex shrink-0 flex-wrap gap-2">
                          {work.openAlexUrl && (
                            <a href={work.openAlexUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700 hover:border-blue-300 hover:text-blue-700">
                              OpenAlex <ExternalLink className="h-3.5 w-3.5" />
                            </a>
                          )}
                          <button type="button" onClick={() => setExpandedWorkId(expanded ? "" : work.workId)} className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700 hover:border-blue-300">
                            {expanded ? "Fechar" : "Consultar"} {expanded ? <ChevronUp className="h-3.5 w-3.5" /> : <ChevronDown className="h-3.5 w-3.5" />}
                          </button>
                          <button type="button" disabled={!work.hasOriginalAbstract} onClick={() => { setExpandedWorkId(work.workId); setEditingWorkId(editing ? "" : work.workId); }} className="inline-flex items-center gap-2 rounded-xl bg-blue-700 px-3 py-2 text-xs font-bold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-50">
                            <PencilLine className="h-3.5 w-3.5" /> Versões PT–EN
                          </button>
                        </div>
                      </div>

                      {expanded && (
                        <div className="mt-4 border-t border-slate-200 pt-4">
                          <p className="text-xs font-black uppercase tracking-wide text-slate-600">Abstract original</p>
                          <p className="mt-2 whitespace-pre-wrap text-sm leading-7 text-slate-700">{work.originalAbstract || "Abstract ainda não disponível no OpenAlex."}</p>
                          {work.translationsUpdatedAt && <p className="mt-3 text-xs font-semibold text-slate-500">Versões atualizadas em {formatDateTime(work.translationsUpdatedAt)}</p>}
                        </div>
                      )}

                      {editing && (
                        <TranslationEditor work={work} saving={saving} onCancel={() => setEditingWorkId("")} onSave={(translations) => handleSaveTranslations(work.workId, translations)} />
                      )}
                    </article>
                  );
                })}
              </div>
            ) : (
              <div className="mt-5 rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-7 text-center">
                <BookOpenCheck className="mx-auto h-7 w-7 text-slate-400" />
                <p className="mt-3 font-black text-slate-800">Nenhuma obra confirmada</p>
                <p className="mt-1 text-sm text-slate-500">Conclua a revisão manual antes de analisar abstracts.</p>
                <Link to={`/admin/manual-review?researcherId=${selectedResearcherId}`} className="mt-4 inline-flex items-center gap-2 text-sm font-black text-blue-700">Ir para revisão manual <ArrowRight className="h-4 w-4" /></Link>
              </div>
            )}
          </section>

          <section className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
            <div className="flex gap-3">
              <ShieldCheck className="mt-0.5 h-5 w-5 shrink-0 text-blue-700" />
              <div>
                <p className="font-black text-slate-950">Metodologia transparente</p>
                <p className="mt-1 text-sm leading-6 text-slate-600">{analysis.methodology}</p>
              </div>
            </div>
          </section>
        </>
      )}
    </div>
  );
}
