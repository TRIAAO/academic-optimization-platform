import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  ArrowRight,
  BookMarked,
  BookOpenCheck,
  CheckCircle2,
  ClipboardCheck,
  CircleGauge,
  ExternalLink,
  FileSearch,
  RefreshCw,
  Save,
  Scale,
  ShieldCheck,
  Sparkles,
  UsersRound
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import MetricCard from "../components/ui/MetricCard";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { editorialRecommendationService } from "../services/editorialRecommendationService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

const EVIDENCE_LEVELS = {
  FORTE: { label: "Evidência forte", variant: "green", ring: "stroke-emerald-400" },
  MODERADA: { label: "Evidência moderada", variant: "amber", ring: "stroke-amber-400" },
  INICIAL: { label: "Evidência inicial", variant: "red", ring: "stroke-rose-400" },
  SEM_EVIDENCIA: { label: "Sem evidência", variant: "slate", ring: "stroke-slate-500" }
};

const CONFIDENCE = {
  HIGH: { label: "Alta confiança", variant: "green" },
  MEDIUM: { label: "Confiança média", variant: "amber" },
  INITIAL: { label: "Evidência inicial", variant: "slate" }
};

const DECISION_STATUSES = {
  UNDER_REVIEW: { label: "Em análise", variant: "amber" },
  APPROVED: { label: "Aprovado", variant: "green" },
  REJECTED: { label: "Rejeitado", variant: "red" },
  SUBMISSION_PLANNED: { label: "Submissão planejada", variant: "blue" },
  SUBMITTED: { label: "Submetido", variant: "green" }
};

const DECISION_CRITERIA = [
  { field: "scopeConfirmed", label: "Escopo e público-alvo" },
  { field: "peerReviewConfirmed", label: "Revisão por pares" },
  { field: "indexingConfirmed", label: "Indexação e reputação" },
  { field: "feesConfirmed", label: "Custos e taxas" },
  { field: "languageConfirmed", label: "Idioma e diretrizes" },
  { field: "deadlinesConfirmed", label: "Prazos editoriais" }
];

function decisionFromJournal(journal) {
  return {
    journalName: journal.journalName,
    publisher: journal.publisher || "",
    issns: journal.issns || [],
    relevanceScore: Number(journal.relevanceScore || 0),
    officialUrl: "",
    status: "UNDER_REVIEW",
    scopeConfirmed: false,
    peerReviewConfirmed: false,
    indexingConfirmed: false,
    feesConfirmed: false,
    languageConfirmed: false,
    deadlinesConfirmed: false,
    notes: ""
  };
}

function EvidenceScore({ value, level }) {
  const normalized = Math.min(100, Math.max(0, Number(value || 0)));
  const config = EVIDENCE_LEVELS[level] || EVIDENCE_LEVELS.SEM_EVIDENCIA;
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
          <span className="text-2xl font-black text-white">{normalized}</span>
          <span className="text-[9px] font-bold uppercase text-slate-400">de 100</span>
        </div>
      </div>
      <div>
        <p className="text-[11px] font-black uppercase tracking-[0.22em] text-blue-300">
          Compatibilidade editorial
        </p>
        <p className="mt-2 text-lg font-black text-white">{config.label}</p>
        <p className="mt-1 text-sm leading-6 text-slate-300">
          Maior pontuação entre os candidatos encontrados.
        </p>
      </div>
    </div>
  );
}

export default function EditorialRecommendations() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [selectedWorkId, setSelectedWorkId] = useState("");
  const [analysis, setAnalysis] = useState(null);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);
  const [decision, setDecision] = useState(null);
  const [decisionDraft, setDecisionDraft] = useState(null);
  const [loadingDecision, setLoadingDecision] = useState(false);
  const [savingDecision, setSavingDecision] = useState(false);
  const [decisionError, setDecisionError] = useState("");
  const [decisionMessage, setDecisionMessage] = useState("");
  const [error, setError] = useState("");

  const selectedResearcher = useMemo(
    () => researchers.find((item) => item.id === selectedResearcherId),
    [researchers, selectedResearcherId]
  );

  const topScore = useMemo(
    () => Math.max(0, ...(analysis?.journals || []).map((item) => Number(item.relevanceScore || 0))),
    [analysis]
  );

  const confirmedCriteria = useMemo(
    () => DECISION_CRITERIA.filter(({ field }) => Boolean(decisionDraft?.[field])).length,
    [decisionDraft]
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

  async function loadAnalysis(researcherId = selectedResearcherId, workId = "") {
    if (!researcherId) return;
    setLoadingAnalysis(true);
    setError("");
    try {
      const result = await editorialRecommendationService.generateByResearcher(researcherId, workId);
      setAnalysis(result);
      setSelectedWorkId(result.selectedWorkId || "");
      if (result.selectedWorkId) {
        await loadDecision(researcherId, result.selectedWorkId);
      } else {
        setDecision(null);
        setDecisionDraft(null);
      }
    } catch (apiError) {
      setAnalysis(null);
      setError(apiError?.message || "Não foi possível gerar o direcionamento editorial.");
    } finally {
      setLoadingAnalysis(false);
    }
  }

  async function loadDecision(researcherId, workId) {
    setLoadingDecision(true);
    setDecisionError("");
    setDecisionMessage("");
    try {
      const result = await editorialRecommendationService.findDecisionByWork(researcherId, workId);
      setDecision(result);
      setDecisionDraft(result);
    } catch (apiError) {
      setDecision(null);
      setDecisionDraft(null);
      setDecisionError(apiError?.message || "Não foi possível carregar a decisão editorial.");
    } finally {
      setLoadingDecision(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      setSelectedWorkId("");
      setDecision(null);
      setDecisionDraft(null);
      loadAnalysis(selectedResearcherId);
    }
  }, [selectedResearcherId]);

  function handleResearcherChange(event) {
    const researcherId = event.target.value;
    setSelectedResearcherId(researcherId);
    setSearchParams(researcherId ? { researcherId } : {});
  }

  function handleWorkChange(event) {
    const workId = event.target.value;
    setSelectedWorkId(workId);
    setDecision(null);
    setDecisionDraft(null);
    loadAnalysis(selectedResearcherId, workId);
  }

  function selectJournal(journal) {
    if (decision?.journalName === journal.journalName) {
      setDecisionDraft(decision);
    } else {
      setDecisionDraft(decisionFromJournal(journal));
    }
    setDecisionError("");
    setDecisionMessage("");
    window.setTimeout(() => {
      document.getElementById("editorial-decision")?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 0);
  }

  function updateDecisionField(field, value) {
    setDecisionDraft((current) => ({ ...current, [field]: value }));
    setDecisionError("");
    setDecisionMessage("");
  }

  async function saveDecision() {
    if (!decisionDraft || !selectedResearcherId || !selectedWorkId) return;
    setSavingDecision(true);
    setDecisionError("");
    setDecisionMessage("");
    try {
      const payload = {
        journalName: decisionDraft.journalName,
        publisher: decisionDraft.publisher || null,
        issns: decisionDraft.issns || [],
        relevanceScore: Number(decisionDraft.relevanceScore || 0),
        officialUrl: decisionDraft.officialUrl || null,
        status: decisionDraft.status,
        scopeConfirmed: Boolean(decisionDraft.scopeConfirmed),
        peerReviewConfirmed: Boolean(decisionDraft.peerReviewConfirmed),
        indexingConfirmed: Boolean(decisionDraft.indexingConfirmed),
        feesConfirmed: Boolean(decisionDraft.feesConfirmed),
        languageConfirmed: Boolean(decisionDraft.languageConfirmed),
        deadlinesConfirmed: Boolean(decisionDraft.deadlinesConfirmed),
        notes: decisionDraft.notes || null
      };
      const saved = await editorialRecommendationService.saveDecision(
        selectedResearcherId,
        selectedWorkId,
        payload
      );
      setDecision(saved);
      setDecisionDraft(saved);
      setDecisionMessage("Decisão editorial registrada com rastreabilidade.");
    } catch (apiError) {
      setDecisionError(apiError?.message || "Não foi possível salvar a decisão editorial.");
    } finally {
      setSavingDecision(false);
    }
  }

  if (loadingResearchers) {
    return <LoadingState message="Preparando o direcionamento editorial..." />;
  }

  if (!researchers.length) {
    return (
      <EmptyState
        icon={UsersRound}
        title="Nenhum pesquisador cadastrado"
        description="Cadastre um pesquisador e consolide as obras antes de analisar periódicos candidatos."
        action={<Link to="/admin/researchers"><PrimaryButton icon={ArrowRight}>Cadastrar pesquisador</PrimaryButton></Link>}
      />
    );
  }

  const evidence = EVIDENCE_LEVELS[analysis?.evidenceLevel] || EVIDENCE_LEVELS.SEM_EVIDENCIA;
  const decisionStatus = DECISION_STATUSES[decisionDraft?.status] || DECISION_STATUSES.UNDER_REVIEW;

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Abstract → periódico"
        title="Direcionamento Editorial"
        description="Encontre periódicos associados a publicações bibliograficamente próximas da obra selecionada, com evidências rastreáveis e decisão humana."
        actions={
          <PrimaryButton
            variant="light"
            icon={RefreshCw}
            loading={loadingAnalysis}
            onClick={() => loadAnalysis(selectedResearcherId, selectedWorkId)}
          >
            Atualizar análise
          </PrimaryButton>
        }
      >
        <div className="grid gap-4 lg:grid-cols-2">
          <label className="block">
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

          <label className="block">
            <span className="text-sm font-bold text-slate-700">Obra confirmada com abstract</span>
            <select
              value={selectedWorkId}
              onChange={handleWorkChange}
              disabled={!analysis?.eligibleWorks?.length || loadingAnalysis}
              className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-900 outline-none disabled:cursor-not-allowed disabled:opacity-60 focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
            >
              {!analysis?.eligibleWorks?.length && <option value="">Nenhuma obra disponível</option>}
              {(analysis?.eligibleWorks || []).map((work) => (
                <option key={work.workId} value={work.workId}>
                  {work.title} {work.publicationYear ? `(${work.publicationYear})` : ""}
                </option>
              ))}
            </select>
          </label>
        </div>
      </PageHeader>

      {error && <ErrorState title="Atenção no direcionamento editorial" message={error} />}
      {loadingAnalysis && !analysis && <LoadingState message="Comparando a obra com publicações do Crossref..." />}

      {analysis && !analysis.eligibleWorks?.length && (
        <EmptyState
          icon={FileSearch}
          title="Nenhuma obra confirmada com abstract"
          description="Confirme a autoria e sincronize os abstracts do OpenAlex antes de buscar periódicos candidatos."
          action={
            <Link to={`/admin/abstract-analysis?researcherId=${encodeURIComponent(selectedResearcherId)}`}>
              <PrimaryButton icon={ArrowRight}>Abrir Análise de Abstracts</PrimaryButton>
            </Link>
          }
        />
      )}

      {analysis && analysis.eligibleWorks?.length > 0 && (
        <>
          {!analysis.crossrefAvailable && (
            <ErrorState title="Crossref temporariamente indisponível" message={analysis.statusMessage} />
          )}

          <section className="overflow-hidden rounded-[2rem] bg-slate-950 p-6 shadow-lg sm:p-7">
            <div className="grid gap-6 lg:grid-cols-[auto_1fr] lg:items-center">
              <EvidenceScore value={topScore} level={analysis.evidenceLevel} />
              <div className="border-t border-slate-800 pt-5 lg:border-l lg:border-t-0 lg:pl-7 lg:pt-0">
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant={evidence.variant}>{evidence.label}</Badge>
                  <span className="text-xs font-semibold text-slate-400">
                    Atualizada em {formatDateTime(analysis.generatedAt)}
                  </span>
                </div>
                <h2 className="mt-3 text-2xl font-black text-white">
                  {analysis.selectedWorkTitle}
                </h2>
                <p className="mt-2 text-sm font-semibold text-blue-300">
                  {analysis.researcherName || selectedResearcher?.fullName}
                </p>
                <p className="mt-3 max-w-3xl text-sm leading-7 text-slate-300">
                  {analysis.statusMessage}
                </p>
              </div>
            </div>
          </section>

          <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <MetricCard icon={BookOpenCheck} title="Confirmadas" value={formatNumber(analysis.confirmedWorks)} description="obras validadas" healthy={analysis.confirmedWorks > 0} />
            <MetricCard icon={FileSearch} title="Com abstract" value={formatNumber(analysis.worksWithAbstract)} description="elegíveis para análise" healthy={analysis.worksWithAbstract > 0} />
            <MetricCard icon={BookMarked} title="Candidatos" value={formatNumber(analysis.candidateJournalCount)} description="periódicos para revisão" healthy={analysis.candidateJournalCount > 0} />
            <MetricCard icon={CheckCircle2} title="Fonte" value={analysis.crossrefAvailable ? "Crossref" : "Indisponível"} valueSize="sm" healthy={analysis.crossrefAvailable} />
          </section>

          <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
            <div className="mb-5 flex items-start justify-between gap-4">
              <div>
                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-blue-700">Candidatos editoriais</p>
                <h3 className="mt-2 text-xl font-black text-slate-950">Periódicos relacionados à obra</h3>
                <p className="mt-1 text-sm leading-6 text-slate-500">Ordenados por evidência bibliográfica; valide sempre o escopo no site oficial.</p>
              </div>
              <Sparkles className="h-6 w-6 shrink-0 text-blue-700" />
            </div>

            {analysis.journals?.length ? (
              <div className="grid gap-4 lg:grid-cols-2">
                {analysis.journals.map((journal, index) => {
                  const confidence = CONFIDENCE[journal.confidence] || CONFIDENCE.INITIAL;
                  const selected = decisionDraft?.journalName === journal.journalName;
                  return (
                    <article
                      key={journal.journalName}
                      className={`rounded-2xl border p-5 transition ${
                        selected
                          ? "border-blue-400 bg-blue-50 shadow-sm ring-2 ring-blue-100"
                          : "border-slate-200 bg-slate-50"
                      }`}
                    >
                      <div className="flex items-start gap-4">
                        <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-white text-sm font-black text-blue-700 shadow-sm">
                          {index + 1}
                        </span>
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-start justify-between gap-3">
                            <div className="min-w-0">
                              <h4 className="font-black text-slate-950">{journal.journalName}</h4>
                              <p className="mt-1 text-sm text-slate-500">{journal.publisher || "Editora não informada"}</p>
                            </div>
                            <span className="text-2xl font-black text-blue-700">{formatNumber(journal.relevanceScore)}</span>
                          </div>

                          <div className="mt-3 flex flex-wrap gap-2">
                            <Badge variant={confidence.variant}>{confidence.label}</Badge>
                            {journal.presentInResearcherHistory && <Badge variant="blue">Já presente no histórico</Badge>}
                            {journal.issns?.slice(0, 2).map((issn) => <Badge key={issn} variant="slate">ISSN {issn}</Badge>)}
                          </div>

                          <p className="mt-3 text-sm leading-6 text-slate-600">{journal.rationale}</p>

                          <div className="mt-4 grid gap-2 text-xs font-bold text-slate-600 sm:grid-cols-3">
                            <span>{formatNumber(journal.relatedWorks)} obra(s) próxima(s)</span>
                            <span>{formatNumber(journal.relatedCitations)} citações</span>
                            <span>{formatNumber(journal.maximumTitleSimilarityPercentage)}% similaridade</span>
                          </div>

                          {journal.sampleTitle && (
                            <div className="mt-4 rounded-xl border border-slate-200 bg-white p-3">
                              <p className="text-[10px] font-black uppercase tracking-wide text-slate-400">Exemplo de evidência</p>
                              <p className="mt-1 text-sm font-semibold leading-5 text-slate-700">{journal.sampleTitle}</p>
                              {journal.sampleUrl && (
                                <a href={journal.sampleUrl} target="_blank" rel="noreferrer" className="mt-2 inline-flex items-center gap-1 text-xs font-black text-blue-700 hover:text-blue-900">
                                  Consultar registro <ExternalLink className="h-3.5 w-3.5" />
                                </a>
                              )}
                            </div>
                          )}

                          <div className="mt-4 flex justify-end border-t border-slate-200 pt-4">
                            <PrimaryButton
                              variant={selected ? "primary" : "light"}
                              icon={ClipboardCheck}
                              onClick={() => selectJournal(journal)}
                            >
                              {decision?.journalName === journal.journalName
                                ? "Decisão registrada"
                                : selected
                                  ? "Selecionado para validar"
                                  : "Selecionar para validação"}
                            </PrimaryButton>
                          </div>
                        </div>
                      </div>
                    </article>
                  );
                })}
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-7 text-center">
                <BookMarked className="mx-auto h-7 w-7 text-slate-400" />
                <p className="mt-3 font-black text-slate-800">Nenhum candidato com evidência mínima</p>
                <p className="mt-1 text-sm leading-6 text-slate-500">Revise o abstract ou tente outra obra confirmada.</p>
              </div>
            )}
          </section>

          <section
            id="editorial-decision"
            className="scroll-mt-24 rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6"
          >
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <p className="text-[11px] font-black uppercase tracking-[0.22em] text-blue-700">
                  Governança editorial
                </p>
                <h3 className="mt-2 text-xl font-black text-slate-950">Decisão editorial humana</h3>
                <p className="mt-1 text-sm leading-6 text-slate-500">
                  Selecione um candidato, confira os dados no site oficial e documente a decisão da instituição.
                </p>
              </div>
              {decisionDraft && <Badge variant={decisionStatus.variant}>{decisionStatus.label}</Badge>}
            </div>

            {decisionError && !decisionDraft && (
              <div className="mt-5">
                <ErrorState title="Atenção na decisão editorial" message={decisionError} />
              </div>
            )}

            {loadingDecision ? (
              <div className="mt-5"><LoadingState message="Carregando decisão editorial..." /></div>
            ) : decisionDraft ? (
              <div className="mt-5 space-y-5">
                <div className="grid gap-4 rounded-2xl border border-blue-200 bg-blue-50 p-4 lg:grid-cols-[1fr_auto] lg:items-center">
                  <div>
                    <p className="text-[10px] font-black uppercase tracking-[0.2em] text-blue-700">
                      Periódico selecionado
                    </p>
                    <p className="mt-2 font-black text-blue-950">{decisionDraft.journalName}</p>
                    <p className="mt-1 text-sm text-blue-900/70">
                      {decisionDraft.publisher || "Editora não informada"}
                      {decisionDraft.issns?.length ? ` · ISSN ${decisionDraft.issns.join(", ")}` : ""}
                    </p>
                  </div>
                  <div className="rounded-2xl bg-white px-4 py-3 text-center shadow-sm">
                    <p className="text-[10px] font-black uppercase tracking-wide text-slate-400">Score de origem</p>
                    <p className="mt-1 text-2xl font-black text-blue-700">{formatNumber(decisionDraft.relevanceScore)}</p>
                  </div>
                </div>

                {decisionError && <ErrorState title="Atenção na decisão editorial" message={decisionError} />}
                {decisionMessage && (
                  <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-bold text-emerald-800">
                    {decisionMessage}
                  </div>
                )}

                <div className="grid gap-4 lg:grid-cols-2">
                  <label className="block">
                    <span className="text-sm font-bold text-slate-700">Status da decisão</span>
                    <select
                      value={decisionDraft.status}
                      onChange={(event) => updateDecisionField("status", event.target.value)}
                      className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-900 outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    >
                      {Object.entries(DECISION_STATUSES).map(([value, config]) => (
                        <option key={value} value={value}>{config.label}</option>
                      ))}
                    </select>
                  </label>

                  <label className="block">
                    <span className="text-sm font-bold text-slate-700">URL oficial do periódico</span>
                    <input
                      type="url"
                      value={decisionDraft.officialUrl || ""}
                      onChange={(event) => updateDecisionField("officialUrl", event.target.value)}
                      placeholder="https://periodico.exemplo.org"
                      maxLength={1000}
                      className="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-900 outline-none placeholder:text-slate-400 focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                    />
                  </label>
                </div>

                <div>
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <p className="text-sm font-black text-slate-900">Checklist de validação</p>
                      <p className="mt-1 text-xs text-slate-500">Confirme cada critério consultando a fonte oficial.</p>
                    </div>
                    <Badge variant={confirmedCriteria === 6 ? "green" : "slate"}>
                      {confirmedCriteria}/6 confirmados
                    </Badge>
                  </div>
                  <div className="mt-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                    {DECISION_CRITERIA.map(({ field, label }) => (
                      <label
                        key={field}
                        className={`flex cursor-pointer items-center gap-3 rounded-2xl border p-4 text-sm font-bold transition ${
                          decisionDraft[field]
                            ? "border-emerald-300 bg-emerald-50 text-emerald-900"
                            : "border-slate-200 bg-slate-50 text-slate-700 hover:border-blue-300"
                        }`}
                      >
                        <input
                          type="checkbox"
                          checked={Boolean(decisionDraft[field])}
                          onChange={(event) => updateDecisionField(field, event.target.checked)}
                          className="h-4 w-4 rounded border-slate-300 text-blue-700 focus:ring-blue-500"
                        />
                        {label}
                      </label>
                    ))}
                  </div>
                </div>

                <label className="block">
                  <span className="text-sm font-bold text-slate-700">Observações da decisão</span>
                  <textarea
                    value={decisionDraft.notes || ""}
                    onChange={(event) => updateDecisionField("notes", event.target.value)}
                    placeholder="Registre justificativas, restrições ou próximos passos. Obrigatório em caso de rejeição."
                    rows={4}
                    maxLength={2000}
                    className="mt-2 w-full resize-y rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm leading-6 text-slate-900 outline-none placeholder:text-slate-400 focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                  />
                </label>

                <div className="flex flex-wrap items-center justify-between gap-4 border-t border-slate-200 pt-4">
                  <p className="text-xs leading-5 text-slate-500">
                    {decision?.updatedAt
                      ? `Última atualização: ${formatDateTime(decision.updatedAt)} por ${decision.reviewedBy}.`
                      : "Esta escolha só será registrada após clicar em salvar."}
                  </p>
                  <PrimaryButton icon={Save} loading={savingDecision} onClick={saveDecision}>
                    Salvar decisão editorial
                  </PrimaryButton>
                </div>
              </div>
            ) : (
              <div className="mt-5 rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-7 text-center">
                <ClipboardCheck className="mx-auto h-7 w-7 text-slate-400" />
                <p className="mt-3 font-black text-slate-800">Nenhum periódico selecionado</p>
                <p className="mt-1 text-sm leading-6 text-slate-500">
                  Use “Selecionar para validação” em um candidato para iniciar a decisão humana.
                </p>
              </div>
            )}
          </section>

          <section className="grid gap-4 rounded-3xl border border-blue-200 bg-blue-50 p-5 sm:p-6 lg:grid-cols-2">
            <div className="flex gap-4">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white text-blue-700"><CircleGauge className="h-5 w-5" /></div>
              <div>
                <p className="font-black text-blue-950">Metodologia explicável</p>
                <p className="mt-2 text-sm leading-6 text-blue-900/75">{analysis.methodology}</p>
              </div>
            </div>
            <div className="flex gap-4 border-t border-blue-200 pt-4 lg:border-l lg:border-t-0 lg:pl-6 lg:pt-0">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-white text-emerald-700"><Scale className="h-5 w-5" /></div>
              <div>
                <p className="font-black text-blue-950">Decisão editorial humana</p>
                <p className="mt-2 text-sm leading-6 text-blue-900/75">{analysis.decisionPolicy}</p>
              </div>
            </div>
          </section>

          <div className="flex items-center gap-2 px-2 text-xs text-slate-500">
            <ShieldCheck className="h-4 w-4" />
            <span>A plataforma não submete manuscritos nem garante publicação.</span>
          </div>
        </>
      )}
    </div>
  );
}
