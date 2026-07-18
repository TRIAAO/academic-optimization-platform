import {
  CalendarRange,
  ClockAlert,
  Database,
  MailCheck,
  RefreshCw,
  TrendingUp,
  UsersRound
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import InstitutionalLineChart from "../components/scientometrics/InstitutionalLineChart";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import MetricCard from "../components/ui/MetricCard";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { scientometricMetricService } from "../services/scientometricMetricService";
import { formatDateTime, formatNumber } from "../utils/formatters";

const PERIOD_OPTIONS = [6, 12, 24, 36, 60];

function formatDate(value) {
  if (!value) return "Sem medição";

  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("pt-AO", {
    dateStyle: "medium"
  }).format(date);
}

export default function InstitutionalScientometrics() {
  const [months, setMonths] = useState(12);
  const [history, setHistory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadHistory(selectedMonths = months) {
    setLoading(true);
    setError("");

    try {
      const data = await scientometricMetricService.findInstitutionalHistory(
        selectedMonths,
        12
      );
      setHistory(data);
    } catch (apiError) {
      setHistory(null);
      setError(
        apiError?.message ||
          "Não foi possível carregar as séries históricas institucionais."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadHistory(months);
  }, [months]);

  const timeline = useMemo(
    () => (Array.isArray(history?.timeline) ? history.timeline : []),
    [history]
  );

  const latestPoint = timeline.length > 0 ? timeline[timeline.length - 1] : null;

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Inteligência institucional"
        title="Séries Históricas Cientométricas"
        description="Acompanhe a evolução consolidada das citações, médias de H-index e D-index, cobertura das medições e verificação de e-mail institucional."
        actions={
          <PrimaryButton
            variant="light"
            icon={RefreshCw}
            onClick={() => loadHistory(months)}
            loading={loading}
          >
            Atualizar séries
          </PrimaryButton>
        }
      />

      <section className="rounded-3xl border border-blue-200 bg-blue-50 p-5 sm:p-6 dark:border-blue-500/50 dark:bg-blue-950/55">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex items-start gap-4">
            <TrendingUp className="mt-0.5 h-6 w-6 shrink-0 text-blue-700 dark:text-blue-300" />
            <div>
              <h3 className="font-black text-blue-950 dark:text-blue-100">
                Evolução institucional baseada em snapshots
              </h3>
              <p className="mt-2 max-w-4xl text-sm leading-7 text-blue-800 dark:text-blue-200">
                Cada mês utiliza a medição mais recente disponível de cada pesquisador até o fim do período. Os valores anteriores são preservados até existir um novo snapshot, sem alterar fontes externas.
              </p>
            </div>
          </div>

          <label className="min-w-48 text-sm font-bold text-blue-950 dark:text-blue-100">
            Período analisado
            <select
              value={months}
              onChange={(event) => setMonths(Number(event.target.value))}
              className="mt-2 w-full rounded-xl border border-blue-200 bg-white px-3 py-2.5 text-sm text-slate-950 outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100 dark:border-blue-700 dark:bg-slate-950 dark:text-slate-100 dark:focus:border-blue-400 dark:focus:ring-blue-950"
            >
              {PERIOD_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  Últimos {option} meses
                </option>
              ))}
            </select>
          </label>
        </div>
      </section>

      {error && (
        <ErrorState
          title="Séries históricas indisponíveis"
          message={error}
        />
      )}

      {loading && <LoadingState message="Calculando evolução institucional..." />}

      {!loading && !error && history && (
        <>
          <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <MetricCard
              title="Pesquisadores com métricas"
              value={`${formatNumber(history.researchersWithMetrics)} / ${formatNumber(
                history.totalActiveResearchers
              )}`}
              description={`${history.currentMetricCoverageRate || 0}% de cobertura institucional atual.`}
              icon={UsersRound}
              tone="blue"
            />
            <MetricCard
              title="Sem métricas recentes"
              value={formatNumber(history.researchersWithoutRecentMetrics)}
              description={`Sem snapshot nos últimos ${history.staleAfterMonths || 12} meses.`}
              icon={ClockAlert}
              tone="amber"
            />
            <MetricCard
              title="E-mails institucionais"
              value={`${history.currentInstitutionalEmailVerificationRate || 0}%`}
              description="Verificados entre os pesquisadores medidos."
              icon={MailCheck}
              tone="emerald"
            />
            <MetricCard
              title="Último snapshot"
              value={formatDate(history.latestSnapshotDate)}
              description={`Série gerada em ${formatDateTime(history.generatedAt)}.`}
              icon={Database}
              tone="violet"
              valueSize="sm"
            />
          </section>

          {timeline.length === 0 ? (
            <EmptyState
              icon={CalendarRange}
              title="Ainda não existem medições institucionais"
              description="Registe snapshots no módulo Métricas Cientométricas para iniciar as séries históricas."
            />
          ) : (
            <div className="space-y-6">
              <InstitutionalLineChart
                title="Evolução das citações institucionais"
                description="Soma das citações totais e das citações dos últimos seis anos usando o snapshot vigente de cada pesquisador em cada mês."
                data={timeline}
                series={[
                  {
                    key: "citationsTotal",
                    label: "Citações totais",
                    color: "#2563eb"
                  },
                  {
                    key: "citationsLastSixYears",
                    label: "Últimos 6 anos",
                    color: "#059669"
                  }
                ]}
              />

              <InstitutionalLineChart
                title="Média institucional: H-index × D-index"
                description="Comparação das médias dos indicadores disponíveis. A média não substitui o indicador individual e deve ser interpretada com a cobertura da série."
                data={timeline}
                series={[
                  {
                    key: "averageHIndexTotal",
                    label: "H-index médio",
                    color: "#7c3aed"
                  },
                  {
                    key: "averageDIndex",
                    label: "D-index médio",
                    color: "#d97706"
                  }
                ]}
              />

              <InstitutionalLineChart
                title="Cobertura e identidade institucional"
                description="Percentual de pesquisadores ativos com snapshot disponível e percentual de e-mails institucionais verificados entre os pesquisadores medidos."
                data={timeline}
                fixedMax={100}
                valueSuffix="%"
                series={[
                  {
                    key: "metricCoverageRate",
                    label: "Cobertura de métricas",
                    color: "#2563eb"
                  },
                  {
                    key: "institutionalEmailVerificationRate",
                    label: "E-mail institucional",
                    color: "#059669"
                  }
                ]}
              />
            </div>
          )}

          <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
            <h3 className="font-black text-slate-950">Leitura metodológica</h3>
            <div className="mt-4 grid gap-4 text-sm leading-7 text-slate-600 lg:grid-cols-3">
              <p>
                <strong className="text-slate-900">Carry-forward:</strong> a última medição conhecida permanece válida nos meses seguintes até o registo de um novo snapshot.
              </p>
              <p>
                <strong className="text-slate-900">Cobertura:</strong> os gráficos devem ser lidos junto do percentual de pesquisadores medidos para evitar conclusões sobre amostras incompletas.
              </p>
              <p>
                <strong className="text-slate-900">Governança:</strong> as séries são determinísticas, auditáveis e não coletam nem modificam automaticamente dados do Google Acadêmico.
              </p>
            </div>
            {latestPoint && (
              <p className="mt-5 border-t border-slate-200 pt-4 text-xs text-slate-500">
                Ponto mais recente: {formatDate(latestPoint.periodEnd)} — {formatNumber(
                  latestPoint.researchersMeasured
                )} pesquisador(es) medido(s), {latestPoint.metricCoverageRate || 0}% de cobertura.
              </p>
            )}
          </section>
        </>
      )}
    </div>
  );
}
