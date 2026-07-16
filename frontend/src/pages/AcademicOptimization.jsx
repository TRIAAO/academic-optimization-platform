import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  AlertTriangle,
  ArrowRight,
  Award,
  BookOpenCheck,
  CheckCircle2,
  ClipboardCheck,
  Download,
  FileCheck2,
  Gauge,
  GraduationCap,
  Link2,
  Network,
  RefreshCw,
  ShieldCheck,
  Sparkles,
  Target
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { APP_CONFIG } from "../config/app";
import { optimizationService } from "../services/optimizationService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

const STATUS_CONFIG = {
  EXCELENTE: { label: "Excelente", variant: "green", tone: "bg-emerald-500" },
  BOM: { label: "Bom", variant: "blue", tone: "bg-blue-600" },
  EM_OTIMIZACAO: {
    label: "Em otimização",
    variant: "amber",
    tone: "bg-amber-500"
  },
  CRITICO: { label: "Crítico", variant: "red", tone: "bg-red-600" }
};

const PRIORITY_CONFIG = {
  HIGH: { label: "Alta", variant: "red", order: 0 },
  ALTA: { label: "Alta", variant: "red", order: 0 },
  MEDIUM: { label: "Média", variant: "amber", order: 1 },
  MEDIA: { label: "Média", variant: "amber", order: 1 },
  "MÉDIA": { label: "Média", variant: "amber", order: 1 },
  LOW: { label: "Baixa", variant: "blue", order: 2 },
  BAIXA: { label: "Baixa", variant: "blue", order: 2 }
};

const AREA_ROUTES = {
  ORCID: "/admin/orcid",
  OPENALEX: "/admin/openalex",
  CROSSREF: "/admin/crossref",
  DOI: "/admin/crossref",
  "PERFIL ACADÊMICO": "/admin/academic-profiles",
  "PERFIL ACADEMICO": "/admin/academic-profiles",
  "GOOGLE ACADÊMICO": "/admin/google-scholar-checklist",
  "GOOGLE ACADEMICO": "/admin/google-scholar-checklist"
};

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");

  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

function getStatusConfig(status) {
  return STATUS_CONFIG[String(status || "").toUpperCase()] || STATUS_CONFIG.EM_OTIMIZACAO;
}

function getPriorityConfig(priority) {
  return (
    PRIORITY_CONFIG[String(priority || "").toUpperCase()] ||
    PRIORITY_CONFIG.MEDIUM
  );
}

function getAreaRoute(area) {
  return AREA_ROUTES[String(area || "").trim().toUpperCase()] || "/admin/reports";
}

function scorePercentage(score, maxScore) {
  if (!maxScore) return 0;
  return Math.min(100, Math.max(0, Math.round((Number(score || 0) / maxScore) * 100)));
}

function ScoreDimension({ item }) {
  const status = getStatusConfig(item.status);
  const progress = scorePercentage(item.score, item.maxScore);

  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="font-black text-slate-950">{item.label}</p>
          <p className="mt-2 text-sm leading-6 text-slate-500">{item.message}</p>
        </div>

        <Badge variant={status.variant}>{status.label}</Badge>
      </div>

      <div className="mt-5 flex items-center justify-between text-sm">
        <span className="font-semibold text-slate-500">Desempenho</span>
        <span className="font-black text-slate-950">
          {formatNumber(item.score)} / {formatNumber(item.maxScore)}
        </span>
      </div>

      <div className="mt-3 h-2.5 overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full ${status.tone}`}
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
}

function buildIntegrityAlerts(report) {
  const alerts = [];

  if (Number(report.pendingReviewOpenAlexWorks || 0) > 0) {
    alerts.push({
      tone: "red",
      title: "Autoria pendente de validação humana",
      description: `${formatNumber(report.pendingReviewOpenAlexWorks)} obra(s) do OpenAlex aguardam revisão. Não use esses registros como produção confirmada antes da decisão institucional.`,
      href: "/admin/manual-review",
      action: "Revisar obras"
    });
  }

  const bibliographicIssues =
    Number(report.possibleMatchCount || 0) +
    Number(report.doiMissingCount || 0) +
    Number(report.doiNotFoundCount || 0) +
    Number(report.errorCount || 0);

  if (bibliographicIssues > 0) {
    alerts.push({
      tone: "amber",
      title: "Metadados exigem conferência",
      description: `${formatNumber(bibliographicIssues)} validação(ões) possuem correspondência possível, DOI ausente, não encontrado ou erro. A correção deve preservar a fonte oficial.`,
      href: "/admin/crossref",
      action: "Conferir Crossref"
    });
  }

  if (alerts.length === 0) {
    alerts.push({
      tone: "green",
      title: "Sem alerta crítico nos dados consolidados",
      description:
        "A análise atual não identificou pendências críticas de autoria ou DOI. Mantenha a revisão periódica e a rastreabilidade das fontes.",
      href: "/admin/manual-review",
      action: "Ver revisão"
    });
  }

  alerts.push({
    tone: "blue",
    title: "Google Acadêmico permanece manual",
    description: APP_CONFIG.googleScholarPolicy,
    href: "/admin/google-scholar-checklist",
    action: "Abrir checklist"
  });

  return alerts;
}

const ALERT_TONES = {
  red: "border-red-200 bg-red-50 text-red-950",
  amber: "border-amber-200 bg-amber-50 text-amber-950",
  green: "border-emerald-200 bg-emerald-50 text-emerald-950",
  blue: "border-blue-200 bg-blue-50 text-blue-950"
};

export default function AcademicOptimization() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [report, setReport] = useState(null);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingReport, setLoadingReport] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [error, setError] = useState("");

  const selectedResearcher = useMemo(
    () => researchers.find((item) => item.id === selectedResearcherId),
    [researchers, selectedResearcherId]
  );

  const recommendations = useMemo(() => {
    return [...(report?.recommendations || [])].sort((left, right) => {
      return (
        getPriorityConfig(left.priority).order -
        getPriorityConfig(right.priority).order
      );
    });
  }, [report]);

  const integrityAlerts = useMemo(
    () => (report ? buildIntegrityAlerts(report) : []),
    [report]
  );

  async function loadResearchers() {
    setLoadingResearchers(true);
    setError("");

    try {
      setResearchers(await researcherService.findAll());
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar pesquisadores.");
    } finally {
      setLoadingResearchers(false);
    }
  }

  async function loadOptimization(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingReport(true);
    setError("");

    try {
      setReport(await optimizationService.generateByResearcher(researcherId));
    } catch (apiError) {
      setReport(null);
      setError(
        apiError?.message ||
          "Não foi possível gerar a análise de otimização acadêmica."
      );
    } finally {
      setLoadingReport(false);
    }
  }

  async function handleDownloadPdf() {
    if (!selectedResearcherId) return;

    setDownloading(true);
    setError("");

    try {
      const file = await optimizationService.downloadPdf(selectedResearcherId);
      downloadBlob(file.blob, file.filename);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível exportar o relatório em PDF.");
    } finally {
      setDownloading(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      loadOptimization(selectedResearcherId);
    } else {
      setReport(null);
    }
  }, [selectedResearcherId]);

  const overallStatus = getStatusConfig(report?.overallStatus);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Inteligência institucional"
        title="Otimização Acadêmica"
        description="Diagnóstico por pesquisador com score acadêmico, recomendações priorizadas, alertas de integridade e plano de ação baseado nos dados consolidados da plataforma."
        actions={
          <PrimaryButton variant="light" icon={RefreshCw} onClick={loadResearchers}>
            Atualizar pesquisadores
          </PrimaryButton>
        }
      />

      <section className="rounded-3xl border border-blue-200 bg-blue-50 p-5 sm:p-6">
        <div className="flex gap-4">
          <ShieldCheck className="mt-0.5 h-6 w-6 shrink-0 text-blue-700" />
          <div>
            <h3 className="font-black text-blue-950">Análise consultiva e rastreável</h3>
            <p className="mt-2 text-sm leading-7 text-blue-900">
              O score orienta a melhoria do perfil institucional. Nenhuma recomendação altera automaticamente fontes externas ou substitui a validação humana.
            </p>
          </div>
        </div>
      </section>

      {error && <ErrorState title="Atenção" message={error} />}

      {loadingResearchers ? (
        <LoadingState message="Carregando pesquisadores para análise..." />
      ) : (
        <>
          <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="grid gap-4 lg:grid-cols-[1fr_auto] lg:items-end">
              <div>
                <label className="text-sm font-bold text-slate-700">Pesquisador</label>
                <select
                  value={selectedResearcherId}
                  onChange={(event) => setSelectedResearcherId(event.target.value)}
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                >
                  <option value="">Selecione um pesquisador</option>
                  {researchers.map((researcher) => (
                    <option key={researcher.id} value={researcher.id}>
                      {researcher.fullName} — {researcher.email}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex flex-wrap gap-3">
                <PrimaryButton
                  icon={Sparkles}
                  loading={loadingReport}
                  disabled={!selectedResearcherId}
                  onClick={() => loadOptimization()}
                >
                  Atualizar análise
                </PrimaryButton>
                <PrimaryButton
                  variant="dark"
                  icon={Download}
                  loading={downloading}
                  disabled={!report}
                  onClick={handleDownloadPdf}
                >
                  Exportar PDF
                </PrimaryButton>
              </div>
            </div>

            {selectedResearcher && (
              <div className="mt-5 grid gap-3 sm:grid-cols-3">
                <div className="rounded-2xl bg-slate-50 p-4 sm:col-span-2">
                  <p className="text-xs font-black uppercase tracking-wide text-slate-500">Pesquisador selecionado</p>
                  <p className="mt-2 font-black text-slate-950">{selectedResearcher.fullName}</p>
                  <p className="mt-1 text-sm text-slate-500">{selectedResearcher.institution || "Instituição não informada"}</p>
                </div>
                <div className="rounded-2xl bg-slate-50 p-4">
                  <p className="text-xs font-black uppercase tracking-wide text-slate-500">ORCID</p>
                  <p className="mt-2 font-black text-slate-950">{selectedResearcher.orcidId || "Não informado"}</p>
                </div>
              </div>
            )}
          </section>

          {!selectedResearcherId && (
            <EmptyState
              icon={Target}
              title="Selecione um pesquisador"
              description="Escolha um pesquisador para gerar o diagnóstico e o plano de ação acadêmico."
            />
          )}

          {selectedResearcherId && loadingReport && (
            <LoadingState message="Calculando score e recomendações..." />
          )}

          {selectedResearcherId && !loadingReport && report && (
            <>
              <section className="overflow-hidden rounded-[2rem] bg-slate-950 text-white shadow-xl shadow-slate-950/10">
                <div className="grid gap-8 p-6 sm:p-8 xl:grid-cols-[0.65fr_1.35fr] xl:items-center">
                  <div className="flex items-center gap-5">
                    <div className="flex h-28 w-28 shrink-0 flex-col items-center justify-center rounded-full border-8 border-blue-500 bg-white/5">
                      <span className="text-4xl font-black">{formatNumber(report.overallScore)}</span>
                      <span className="text-xs font-bold text-slate-300">de 100</span>
                    </div>
                    <div>
                      <p className="text-xs font-black uppercase tracking-[0.22em] text-blue-300">Score acadêmico</p>
                      <div className="mt-3"><Badge variant={overallStatus.variant}>{overallStatus.label}</Badge></div>
                      <p className="mt-3 text-xs text-slate-400">Atualizado em {formatDateTime(report.generatedAt)}</p>
                    </div>
                  </div>

                  <div>
                    <div className="flex items-center gap-3 text-blue-300">
                      <Award className="h-6 w-6" />
                      <span className="text-xs font-black uppercase tracking-[0.22em]">Resumo executivo</span>
                    </div>
                    <h2 className="mt-4 text-2xl font-black sm:text-3xl">{report.researcherName}</h2>
                    <p className="mt-4 max-w-4xl text-sm leading-7 text-slate-300">{report.executiveSummary}</p>
                  </div>
                </div>
              </section>

              <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                  <GraduationCap className="h-6 w-6 text-violet-600" />
                  <p className="mt-4 text-sm font-semibold text-slate-500">Perfil acadêmico</p>
                  <p className="mt-2 text-3xl font-black text-slate-950">{formatNumber(report.profileCompletionPercentage)}%</p>
                </div>
                <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                  <Link2 className="h-6 w-6 text-emerald-600" />
                  <p className="mt-4 text-sm font-semibold text-slate-500">Obras ORCID</p>
                  <p className="mt-2 text-3xl font-black text-slate-950">{formatNumber(report.totalOrcidWorks)}</p>
                </div>
                <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                  <Network className="h-6 w-6 text-blue-600" />
                  <p className="mt-4 text-sm font-semibold text-slate-500">OpenAlex confirmadas</p>
                  <p className="mt-2 text-3xl font-black text-slate-950">{formatNumber(report.confirmedOpenAlexWorks)} <span className="text-base text-slate-400">/ {formatNumber(report.totalOpenAlexWorks)}</span></p>
                </div>
                <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                  <FileCheck2 className="h-6 w-6 text-amber-600" />
                  <p className="mt-4 text-sm font-semibold text-slate-500">DOIs confirmados</p>
                  <p className="mt-2 text-3xl font-black text-slate-950">{formatNumber(report.doiConfirmedCount)} <span className="text-base text-slate-400">/ {formatNumber(report.totalCrossrefValidations)}</span></p>
                </div>
              </section>

              <section>
                <div className="mb-4 flex items-end justify-between gap-4">
                  <div>
                    <p className="text-xs font-black uppercase tracking-[0.22em] text-blue-700">Composição do score</p>
                    <h3 className="mt-2 text-xl font-black text-slate-950">Dimensões avaliadas</h3>
                  </div>
                  <Gauge className="h-7 w-7 text-slate-400" />
                </div>
                <div className="grid gap-4 lg:grid-cols-2">
                  {(report.scoreItems || []).map((item) => (
                    <ScoreDimension key={item.code} item={item} />
                  ))}
                </div>
              </section>

              <section className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
                <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs font-black uppercase tracking-[0.22em] text-blue-700">Por pesquisador</p>
                      <h3 className="mt-2 text-xl font-black text-slate-950">Plano de ação priorizado</h3>
                      <p className="mt-2 text-sm leading-6 text-slate-500">Execute as recomendações na ordem de prioridade e atualize a análise após concluir cada etapa.</p>
                    </div>
                    <ClipboardCheck className="h-7 w-7 shrink-0 text-blue-700" />
                  </div>

                  <div className="mt-6 space-y-4">
                    {recommendations.map((item, index) => {
                      const priority = getPriorityConfig(item.priority);
                      const href = getAreaRoute(item.area);

                      return (
                        <div key={`${item.area}-${index}`} className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                          <div className="flex gap-4">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-white font-black text-blue-700 shadow-sm">{index + 1}</div>
                            <div className="min-w-0 flex-1">
                              <div className="flex flex-wrap items-center gap-2">
                                <Badge variant={priority.variant}>{priority.label}</Badge>
                                <Badge variant="slate">{item.area || "Otimização"}</Badge>
                              </div>
                              <p className="mt-3 text-sm leading-7 text-slate-700">{item.recommendation}</p>
                              <Link to={href} className="mt-4 inline-flex items-center gap-2 text-sm font-black text-blue-700 hover:text-blue-900">
                                Executar ação <ArrowRight className="h-4 w-4" />
                              </Link>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>

                <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs font-black uppercase tracking-[0.22em] text-red-700">Governança</p>
                      <h3 className="mt-2 text-xl font-black text-slate-950">Alertas de integridade</h3>
                    </div>
                    <AlertTriangle className="h-7 w-7 text-red-600" />
                  </div>

                  <div className="mt-6 space-y-4">
                    {integrityAlerts.map((alert) => (
                      <div key={alert.title} className={`rounded-2xl border p-5 ${ALERT_TONES[alert.tone]}`}>
                        <div className="flex items-start gap-3">
                          {alert.tone === "green" ? <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0" /> : alert.tone === "blue" ? <ShieldCheck className="mt-0.5 h-5 w-5 shrink-0" /> : <AlertTriangle className="mt-0.5 h-5 w-5 shrink-0" />}
                          <div>
                            <p className="font-black">{alert.title}</p>
                            <p className="mt-2 text-sm leading-6 opacity-80">{alert.description}</p>
                            <Link to={alert.href} className="mt-3 inline-flex items-center gap-2 text-sm font-black underline-offset-4 hover:underline">
                              {alert.action} <ArrowRight className="h-4 w-4" />
                            </Link>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </section>

              <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                <Link to="/admin/academic-profiles" className="group rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-blue-300">
                  <GraduationCap className="h-6 w-6 text-blue-700" />
                  <p className="mt-4 font-black text-slate-950">Perfil Acadêmico</p>
                  <p className="mt-2 text-sm text-slate-500">Completar identidade e visibilidade.</p>
                </Link>
                <Link to="/admin/manual-review" className="group rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-blue-300">
                  <BookOpenCheck className="h-6 w-6 text-blue-700" />
                  <p className="mt-4 font-black text-slate-950">Revisão Manual</p>
                  <p className="mt-2 text-sm text-slate-500">Validar autoria das publicações.</p>
                </Link>
                <Link to="/admin/scientometric-metrics" className="group rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-blue-300">
                  <Award className="h-6 w-6 text-blue-700" />
                  <p className="mt-4 font-black text-slate-950">Métricas</p>
                  <p className="mt-2 text-sm text-slate-500">Conferir indicadores cientométricos.</p>
                </Link>
                <Link to="/admin/reports" className="group rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-blue-300">
                  <FileCheck2 className="h-6 w-6 text-blue-700" />
                  <p className="mt-4 font-black text-slate-950">Relatório completo</p>
                  <p className="mt-2 text-sm text-slate-500">Visualizar e exportar a análise.</p>
                </Link>
              </section>
            </>
          )}
        </>
      )}
    </div>
  );
}
