import {
  BarChart3,
  CalendarDays,
  CheckCircle2,
  Edit3,
  ExternalLink,
  History,
  MailCheck,
  Plus,
  RefreshCw,
  ShieldCheck,
  Trash2,
  TrendingUp
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import ScientometricMetricForm from "../components/scientometrics/ScientometricMetricForm";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { researcherService } from "../services/researcherService";
import { scientometricMetricService } from "../services/scientometricMetricService";
import { formatDateTime, formatNumber } from "../utils/formatters";

function formatDate(value) {
  if (!value) return "Não informada";

  const date = new Date(`${value}T00:00:00`);

  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("pt-AO", { dateStyle: "medium" }).format(date);
}

function formatSource(source) {
  const sources = {
    MANUAL_GOOGLE_SCHOLAR: "Google Acadêmico — manual",
    MANUAL_INSTITUTIONAL: "Fonte institucional — manual",
    OTHER_MANUAL: "Outra fonte — manual"
  };

  return sources[source] || source || "Registo manual";
}

function percentage(part, total) {
  const normalizedTotal = Number(total || 0);
  const normalizedPart = Number(part || 0);

  if (normalizedTotal <= 0) return 0;
  return Math.min(Math.round((normalizedPart / normalizedTotal) * 100), 100);
}

function MetricCard({ title, value, description, icon: Icon, tone = "blue" }) {
  const tones = {
    blue: "bg-blue-50 text-blue-700 ring-blue-100",
    emerald: "bg-emerald-50 text-emerald-700 ring-emerald-100",
    violet: "bg-violet-50 text-violet-700 ring-violet-100",
    amber: "bg-amber-50 text-amber-700 ring-amber-100"
  };

  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-slate-500">{title}</p>
          <p className="mt-3 text-3xl font-black tracking-tight text-slate-950">
            {formatNumber(value)}
          </p>
        </div>

        <div
          className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl ring-1 ${
            tones[tone] || tones.blue
          }`}
        >
          <Icon className="h-6 w-6" />
        </div>
      </div>

      <p className="mt-4 text-sm leading-6 text-slate-500">{description}</p>
    </div>
  );
}

function ComparisonBar({ label, recent, total, tone = "blue" }) {
  const width = percentage(recent, total);
  const bars = {
    blue: "bg-blue-600",
    emerald: "bg-emerald-600",
    violet: "bg-violet-600"
  };

  return (
    <div>
      <div className="flex items-center justify-between gap-4 text-sm">
        <span className="font-bold text-slate-700">{label}</span>
        <span className="font-black text-slate-950">
          {formatNumber(recent)} / {formatNumber(total)}
        </span>
      </div>
      <div className="mt-2 h-2.5 overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full ${bars[tone] || bars.blue}`}
          style={{ width: `${width}%` }}
        />
      </div>
      <p className="mt-1 text-right text-xs font-bold text-slate-400">
        {width}% nos últimos 6 anos
      </p>
    </div>
  );
}

function HistoryCard({ metric, deleting, onEdit, onDelete }) {
  return (
    <article className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-blue-200 hover:shadow-md">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="blue">{formatSource(metric.source)}</Badge>
            {metric.institutionalEmailVerified && (
              <Badge variant="green">E-mail verificado</Badge>
            )}
          </div>

          <div className="mt-3 flex items-center gap-2 text-sm font-bold text-slate-700">
            <CalendarDays className="h-4 w-4 text-slate-400" />
            {formatDate(metric.snapshotDate)}
          </div>
          <p className="mt-1 text-xs text-slate-400">
            Atualizado em {formatDateTime(metric.updatedAt || metric.createdAt)}
          </p>
        </div>

        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => onEdit(metric)}
            className="inline-flex h-10 w-10 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-600 transition hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
            aria-label="Editar medição"
            title="Editar medição"
          >
            <Edit3 className="h-4 w-4" />
          </button>

          <button
            type="button"
            onClick={() => onDelete(metric)}
            disabled={deleting}
            className="inline-flex h-10 w-10 items-center justify-center rounded-2xl border border-red-200 bg-red-50 text-red-700 transition hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-50"
            aria-label="Excluir medição"
            title="Excluir medição"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </div>

      <div className="mt-5 grid grid-cols-2 gap-3 lg:grid-cols-4">
        <div className="rounded-2xl bg-slate-50 p-3">
          <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
            Citações
          </p>
          <p className="mt-1 text-xl font-black text-slate-950">
            {formatNumber(metric.citationsTotal)}
          </p>
        </div>
        <div className="rounded-2xl bg-slate-50 p-3">
          <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
            H-index
          </p>
          <p className="mt-1 text-xl font-black text-slate-950">
            {formatNumber(metric.hIndexTotal)}
          </p>
        </div>
        <div className="rounded-2xl bg-slate-50 p-3">
          <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
            i10-index
          </p>
          <p className="mt-1 text-xl font-black text-slate-950">
            {formatNumber(metric.i10IndexTotal)}
          </p>
        </div>
        <div className="rounded-2xl bg-slate-50 p-3">
          <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
            D-index
          </p>
          <p className="mt-1 text-xl font-black text-slate-950">
            {formatNumber(metric.dIndex)}
          </p>
        </div>
      </div>

      {(metric.interests || metric.notes) && (
        <div className="mt-4 space-y-2 text-sm leading-6 text-slate-600">
          {metric.interests && (
            <p>
              <span className="font-black text-slate-800">Interesses: </span>
              {metric.interests}
            </p>
          )}
          {metric.notes && (
            <p>
              <span className="font-black text-slate-800">Notas: </span>
              {metric.notes}
            </p>
          )}
        </div>
      )}
    </article>
  );
}

export default function ScientometricMetrics() {
  const [searchParams, setSearchParams] = useSearchParams();
  const researcherFromUrl = searchParams.get("researcherId") || "";

  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState(
    researcherFromUrl
  );
  const [metrics, setMetrics] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingMetric, setEditingMetric] = useState(null);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingMetrics, setLoadingMetrics] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const selectedResearcher = useMemo(() => {
    return researchers.find(
      (researcher) => researcher.id === selectedResearcherId
    );
  }, [researchers, selectedResearcherId]);

  const latestMetric = metrics[0] || null;

  async function loadResearchers() {
    setLoadingResearchers(true);
    setError("");

    try {
      const data = await researcherService.findAll();
      setResearchers(data);

      if (
        researcherFromUrl &&
        !data.some((researcher) => researcher.id === researcherFromUrl)
      ) {
        setSelectedResearcherId("");
        setSearchParams({});
      }
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar pesquisadores.");
    } finally {
      setLoadingResearchers(false);
    }
  }

  async function loadMetrics(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingMetrics(true);
    setError("");

    try {
      const data =
        await scientometricMetricService.findByResearcher(researcherId);
      setMetrics(data);
    } catch (apiError) {
      setMetrics([]);
      setError(
        apiError?.message || "Não foi possível carregar as métricas cientométricas."
      );
    } finally {
      setLoadingMetrics(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    setShowForm(false);
    setEditingMetric(null);
    setSuccess("");

    if (selectedResearcherId) {
      loadMetrics(selectedResearcherId);
    } else {
      setMetrics([]);
    }
  }, [selectedResearcherId]);

  function selectResearcher(event) {
    const researcherId = event.target.value;
    setSelectedResearcherId(researcherId);
    setError("");

    if (researcherId) {
      setSearchParams({ researcherId });
    } else {
      setSearchParams({});
    }
  }

  function startCreate() {
    setEditingMetric(null);
    setShowForm(true);
    setError("");
    setSuccess("");
  }

  function startEdit(metric) {
    setEditingMetric(metric);
    setShowForm(true);
    setError("");
    setSuccess("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function cancelForm() {
    setEditingMetric(null);
    setShowForm(false);
  }

  async function handleSubmit(formData) {
    if (!selectedResearcherId) return;

    setSaving(true);
    setError("");
    setSuccess("");

    try {
      if (editingMetric?.id) {
        await scientometricMetricService.update(editingMetric.id, formData);
        setSuccess("Medição cientométrica atualizada com sucesso.");
      } else {
        await scientometricMetricService.create(
          selectedResearcherId,
          formData
        );
        setSuccess("Medição cientométrica registada com sucesso.");
      }

      cancelForm();
      await loadMetrics(selectedResearcherId);
    } catch (apiError) {
      setError(
        apiError?.message || "Não foi possível guardar a medição cientométrica."
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(metric) {
    const confirmed = window.confirm(
      `Deseja excluir a medição de ${formatDate(metric.snapshotDate)}?`
    );

    if (!confirmed) return;

    setDeletingId(metric.id);
    setError("");
    setSuccess("");

    try {
      await scientometricMetricService.delete(metric.id);
      setSuccess("Medição cientométrica excluída com sucesso.");

      if (editingMetric?.id === metric.id) {
        cancelForm();
      }

      await loadMetrics(selectedResearcherId);
    } catch (apiError) {
      setError(
        apiError?.message || "Não foi possível excluir a medição cientométrica."
      );
    } finally {
      setDeletingId("");
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Impacto e produção científica"
        title="Métricas Cientométricas"
        description="Registo institucional e acompanhamento histórico de citações, H-index, i10-index e D-index dos pesquisadores."
        actions={
          <PrimaryButton variant="light" icon={RefreshCw} onClick={loadResearchers}>
            Atualizar pesquisadores
          </PrimaryButton>
        }
      />

      <section className="rounded-3xl border border-amber-200 bg-amber-50 p-5 sm:p-6">
        <div className="flex items-start gap-4">
          <ShieldCheck className="mt-0.5 h-6 w-6 shrink-0 text-amber-700" />
          <div>
            <h3 className="font-black text-amber-950">
              Registo manual e rastreável
            </h3>
            <p className="mt-2 text-sm leading-7 text-amber-900">
              Os indicadores devem ser conferidos e inseridos manualmente. Este
              módulo não automatiza, não acessa, não coleta e não altera dados
              diretamente no Google Acadêmico.
            </p>
          </div>
        </div>
      </section>

      {error && <ErrorState title="Atenção" message={error} />}

      {success && (
        <div className="rounded-3xl border border-emerald-200 bg-emerald-50 p-4 text-sm font-semibold text-emerald-800">
          {success}
        </div>
      )}

      {loadingResearchers && (
        <LoadingState message="Carregando pesquisadores para métricas..." />
      )}

      {!loadingResearchers && (
        <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
          <div className="grid gap-4 lg:grid-cols-[1fr_auto] lg:items-end">
            <div>
              <label className="text-sm font-bold text-slate-700">
                Pesquisador
              </label>
              <select
                value={selectedResearcherId}
                onChange={selectResearcher}
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
                variant="light"
                icon={RefreshCw}
                disabled={!selectedResearcherId}
                loading={loadingMetrics}
                onClick={() => loadMetrics()}
              >
                Recarregar
              </PrimaryButton>
              <PrimaryButton
                icon={Plus}
                disabled={!selectedResearcherId}
                onClick={startCreate}
              >
                Nova medição
              </PrimaryButton>
            </div>
          </div>

          {selectedResearcher && (
            <div className="mt-5 grid gap-3 md:grid-cols-3">
              <div className="rounded-2xl bg-slate-50 p-4 md:col-span-2">
                <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                  Pesquisador selecionado
                </p>
                <p className="mt-2 font-black text-slate-950">
                  {selectedResearcher.fullName}
                </p>
                <p className="mt-1 text-sm text-slate-500">
                  {selectedResearcher.institution || "Instituição não informada"}
                </p>
              </div>
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                  Medições registadas
                </p>
                <p className="mt-2 text-2xl font-black text-slate-950">
                  {formatNumber(metrics.length)}
                </p>
              </div>
            </div>
          )}
        </section>
      )}

      {!loadingResearchers && !selectedResearcherId && (
        <EmptyState
          icon={BarChart3}
          title="Selecione um pesquisador"
          description="Escolha um pesquisador para consultar o histórico ou registar uma nova medição cientométrica."
        />
      )}

      {selectedResearcherId && showForm && (
        <ScientometricMetricForm
          researcher={selectedResearcher}
          initialData={editingMetric}
          loading={saving}
          onSubmit={handleSubmit}
          onCancel={cancelForm}
        />
      )}

      {selectedResearcherId && loadingMetrics && (
        <LoadingState message="Carregando métricas cientométricas..." />
      )}

      {selectedResearcherId && !loadingMetrics && metrics.length === 0 && (
        <EmptyState
          icon={TrendingUp}
          title="Nenhuma medição registada"
          description="Registe a primeira fotografia cientométrica deste pesquisador para iniciar o acompanhamento histórico."
          action={
            <PrimaryButton icon={Plus} onClick={startCreate}>
              Registar primeira medição
            </PrimaryButton>
          }
        />
      )}

      {selectedResearcherId && !loadingMetrics && latestMetric && (
        <>
          <section>
            <div className="mb-4 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-[0.22em] text-blue-700">
                  Medição mais recente
                </p>
                <h3 className="mt-2 text-xl font-black text-slate-950">
                  Panorama em {formatDate(latestMetric.snapshotDate)}
                </h3>
              </div>
              <Badge variant="blue">{formatSource(latestMetric.source)}</Badge>
            </div>

            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <MetricCard
                title="Citações"
                value={latestMetric.citationsTotal}
                description={`${formatNumber(latestMetric.citationsLastSixYears)} nos últimos 6 anos.`}
                icon={TrendingUp}
                tone="blue"
              />
              <MetricCard
                title="H-index"
                value={latestMetric.hIndexTotal}
                description={`${formatNumber(latestMetric.hIndexLastSixYears)} nos últimos 6 anos.`}
                icon={BarChart3}
                tone="emerald"
              />
              <MetricCard
                title="i10-index"
                value={latestMetric.i10IndexTotal}
                description={`${formatNumber(latestMetric.i10IndexLastSixYears)} nos últimos 6 anos.`}
                icon={CheckCircle2}
                tone="violet"
              />
              <MetricCard
                title="D-index"
                value={latestMetric.dIndex}
                description="Indicador informado na medição atual."
                icon={BarChart3}
                tone="amber"
              />
            </div>
          </section>

          <section className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
            <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <h3 className="font-black text-slate-950">
                Peso dos últimos 6 anos
              </h3>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Comparação entre o indicador recente e o valor acumulado.
              </p>

              <div className="mt-6 space-y-6">
                <ComparisonBar
                  label="Citações"
                  recent={latestMetric.citationsLastSixYears}
                  total={latestMetric.citationsTotal}
                  tone="blue"
                />
                <ComparisonBar
                  label="H-index"
                  recent={latestMetric.hIndexLastSixYears}
                  total={latestMetric.hIndexTotal}
                  tone="emerald"
                />
                <ComparisonBar
                  label="i10-index"
                  recent={latestMetric.i10IndexLastSixYears}
                  total={latestMetric.i10IndexTotal}
                  tone="violet"
                />
              </div>
            </div>

            <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <h3 className="font-black text-slate-950">Validação do perfil</h3>

              <div className="mt-5 space-y-3">
                <div className="flex items-start gap-3 rounded-2xl bg-slate-50 p-4">
                  <MailCheck className="mt-0.5 h-5 w-5 shrink-0 text-blue-700" />
                  <div className="min-w-0">
                    <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                      E-mail verificado
                    </p>
                    <p className="mt-1 break-all text-sm font-bold text-slate-950">
                      {latestMetric.verifiedEmail || "Não informado"}
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3 rounded-2xl bg-slate-50 p-4">
                  <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-emerald-700" />
                  <div>
                    <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                      E-mail institucional
                    </p>
                    <p className="mt-1 text-sm font-bold text-slate-950">
                      {latestMetric.institutionalEmailVerified
                        ? "Verificado"
                        : "Não verificado"}
                    </p>
                  </div>
                </div>

                {latestMetric.googleScholarProfileUrl && (
                  <a
                    href={latestMetric.googleScholarProfileUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="flex items-center justify-between gap-3 rounded-2xl border border-blue-100 bg-blue-50 p-4 text-sm font-black text-blue-700 transition hover:bg-blue-100"
                  >
                    Abrir perfil público informado
                    <ExternalLink className="h-4 w-4" />
                  </a>
                )}
              </div>
            </div>
          </section>

          <section>
            <div className="mb-4 flex items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2">
                  <History className="h-5 w-5 text-blue-700" />
                  <h3 className="text-xl font-black text-slate-950">
                    Histórico de medições
                  </h3>
                </div>
                <p className="mt-2 text-sm text-slate-500">
                  Evolução cronológica dos indicadores registados.
                </p>
              </div>
              <Badge variant="slate">{metrics.length} registos</Badge>
            </div>

            <div className="grid gap-4">
              {metrics.map((metric) => (
                <HistoryCard
                  key={metric.id}
                  metric={metric}
                  deleting={deletingId === metric.id}
                  onEdit={startEdit}
                  onDelete={handleDelete}
                />
              ))}
            </div>
          </section>
        </>
      )}
    </div>
  );
}
