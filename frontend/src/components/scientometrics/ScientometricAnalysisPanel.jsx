import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Info,
  MailCheck,
  Scale
} from "lucide-react";

const STATUS_LABELS = {
  ALIGNED: "Alinhado",
  ATTENTION: "Atenção",
  CRITICAL: "Crítico",
  NOT_AVAILABLE: "Dados insuficientes",
  ACTIVE: "Ativo",
  RECOVERING: "Em recuperação",
  STAGNANT: "Estagnado",
  VERIFIED: "Verificado",
  DOMAIN_MATCH_PENDING: "Domínio reconhecido",
  NON_INSTITUTIONAL: "Não institucional",
  INCONSISTENT: "Inconsistente",
  NOT_INFORMED: "Não informado",
  GROWING: "Crescimento",
  STABLE: "Estável",
  REVIEW: "Revisar dados",
  NO_HISTORY: "Sem histórico"
};

const TONES = {
  good: {
    card: "border-emerald-200 bg-emerald-50",
    icon: "bg-emerald-100 text-emerald-700",
    badge: "bg-emerald-100 text-emerald-800"
  },
  warning: {
    card: "border-amber-200 bg-amber-50",
    icon: "bg-amber-100 text-amber-700",
    badge: "bg-amber-100 text-amber-900"
  },
  danger: {
    card: "border-rose-200 bg-rose-50",
    icon: "bg-rose-100 text-rose-700",
    badge: "bg-rose-100 text-rose-800"
  },
  neutral: {
    card: "border-slate-200 bg-white",
    icon: "bg-slate-100 text-slate-600",
    badge: "bg-slate-100 text-slate-700"
  }
};

function dIndexTone(status) {
  if (status === "ALIGNED") return "good";
  if (status === "CRITICAL") return "danger";
  if (status === "ATTENTION") return "warning";
  return "neutral";
}

function vitalityTone(status) {
  if (status === "ACTIVE") return "good";
  if (status === "STAGNANT") return "danger";
  if (status === "RECOVERING") return "warning";
  return "neutral";
}

function emailTone(status) {
  if (status === "VERIFIED") return "good";
  if (status === "INCONSISTENT") return "danger";
  if (status === "DOMAIN_MATCH_PENDING" || status === "NON_INSTITUTIONAL") {
    return "warning";
  }
  return "neutral";
}

function AssessmentCard({ icon: Icon, eyebrow, title, value, status, explanation, tone }) {
  const styles = TONES[tone] || TONES.neutral;

  return (
    <article className={`rounded-3xl border p-5 ${styles.card}`}>
      <div className="flex items-start gap-4">
        <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${styles.icon}`}>
          <Icon className="h-5 w-5" aria-hidden="true" />
        </div>

        <div className="min-w-0 flex-1">
          <p className="text-xs font-black uppercase tracking-[0.16em] text-slate-500">
            {eyebrow}
          </p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <h4 className="text-lg font-black text-slate-950">{title}</h4>
            <span className={`rounded-full px-2.5 py-1 text-xs font-black ${styles.badge}`}>
              {STATUS_LABELS[status] || status}
            </span>
          </div>
          {value && <p className="mt-3 text-2xl font-black text-slate-950">{value}</p>}
          <p className="mt-2 text-sm leading-6 text-slate-600">{explanation}</p>
        </div>
      </div>
    </article>
  );
}

function AlertCard({ alert }) {
  const severity = alert?.severity || "INFO";
  const styles =
    severity === "HIGH"
      ? {
          wrapper: "border-rose-200 bg-rose-50",
          icon: "text-rose-700",
          badge: "bg-rose-100 text-rose-800"
        }
      : severity === "MEDIUM"
        ? {
            wrapper: "border-amber-200 bg-amber-50",
            icon: "text-amber-700",
            badge: "bg-amber-100 text-amber-900"
          }
        : {
            wrapper: "border-blue-200 bg-blue-50",
            icon: "text-blue-700",
            badge: "bg-blue-100 text-blue-800"
          };
  const Icon = severity === "INFO" ? Info : AlertTriangle;

  return (
    <article className={`rounded-2xl border p-4 ${styles.wrapper}`}>
      <div className="flex items-start gap-3">
        <Icon className={`mt-0.5 h-5 w-5 shrink-0 ${styles.icon}`} aria-hidden="true" />
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <h5 className="font-black text-slate-950">{alert.title}</h5>
            <span className={`rounded-full px-2 py-0.5 text-[11px] font-black ${styles.badge}`}>
              {severity === "HIGH" ? "Alta" : severity === "MEDIUM" ? "Média" : "Informativa"}
            </span>
          </div>
          <p className="mt-2 text-sm leading-6 text-slate-700">{alert.message}</p>
          {alert.action && (
            <p className="mt-2 text-sm font-bold leading-6 text-slate-900">
              Ação: {alert.action}
            </p>
          )}
        </div>
      </div>
    </article>
  );
}

function formatValue(value, suffix = "") {
  return value === null || value === undefined ? "Não calculado" : `${value}${suffix}`;
}

export default function ScientometricAnalysisPanel({ analysis, loading = false }) {
  if (loading) {
    return (
      <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="h-5 w-64 animate-pulse rounded bg-slate-200" />
        <div className="mt-5 grid gap-4 xl:grid-cols-3">
          {[0, 1, 2].map((item) => (
            <div key={item} className="h-44 animate-pulse rounded-3xl bg-slate-100" />
          ))}
        </div>
      </section>
    );
  }

  if (!analysis) return null;

  const dIndex = analysis.dIndexAssessment || {};
  const vitality = analysis.vitalityAssessment || {};
  const email = analysis.institutionalEmailAssessment || {};
  const alerts = Array.isArray(analysis.alerts) ? analysis.alerts : [];

  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.22em] text-blue-700">
            Auditoria determinística
          </p>
          <h3 className="mt-2 text-xl font-black text-slate-950">
            D-index, vitalidade e e-mail institucional
          </h3>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-500">
            Diagnóstico calculado exclusivamente com as medições registadas. Nenhum dado externo é alterado automaticamente.
          </p>
        </div>

        <div className="inline-flex items-center gap-2 self-start rounded-2xl border border-blue-100 bg-blue-50 px-3 py-2 text-xs font-bold text-blue-800">
          <CheckCircle2 className="h-4 w-4" />
          {analysis.measurementCount || 0} medição(ões) analisada(s)
        </div>
      </div>

      <div className="mt-6 grid gap-4 xl:grid-cols-3">
        <AssessmentCard
          icon={Scale}
          eyebrow="Coerência disciplinar"
          title="H-index × D-index"
          value={
            dIndex.deviationPercent === null || dIndex.deviationPercent === undefined
              ? "Não calculado"
              : `${dIndex.deviationPercent}% de desvio`
          }
          status={dIndex.status}
          explanation={dIndex.explanation}
          tone={dIndexTone(dIndex.status)}
        />

        <AssessmentCard
          icon={Activity}
          eyebrow="Últimos 6 anos"
          title="Vitalidade científica"
          value={formatValue(vitality.score, "/100")}
          status={vitality.status}
          explanation={`${vitality.explanation || ""} Tendência: ${STATUS_LABELS[vitality.trend] || vitality.trend || "não calculada"}.`}
          tone={vitalityTone(vitality.status)}
        />

        <AssessmentCard
          icon={MailCheck}
          eyebrow="Identidade institucional"
          title="E-mail acadêmico"
          value={email.email || "Não informado"}
          status={email.status}
          explanation={email.explanation}
          tone={emailTone(email.status)}
        />
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <div className="rounded-2xl bg-slate-50 p-4">
          <p className="text-xs font-black uppercase tracking-wide text-slate-500">Citações recentes</p>
          <p className="mt-2 text-xl font-black text-slate-950">
            {formatValue(vitality.citationsRecentPercent, "%")}
          </p>
        </div>
        <div className="rounded-2xl bg-slate-50 p-4">
          <p className="text-xs font-black uppercase tracking-wide text-slate-500">H-index recente</p>
          <p className="mt-2 text-xl font-black text-slate-950">
            {formatValue(vitality.hIndexRecentPercent, "%")}
          </p>
        </div>
        <div className="rounded-2xl bg-slate-50 p-4">
          <p className="text-xs font-black uppercase tracking-wide text-slate-500">i10-index recente</p>
          <p className="mt-2 text-xl font-black text-slate-950">
            {formatValue(vitality.i10IndexRecentPercent, "%")}
          </p>
        </div>
      </div>

      {alerts.length > 0 && (
        <div className="mt-6">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-amber-600" />
            <h4 className="font-black text-slate-950">Alertas e ações recomendadas</h4>
          </div>
          <div className="mt-4 grid gap-3 lg:grid-cols-2">
            {alerts.map((alert) => (
              <AlertCard key={alert.code} alert={alert} />
            ))}
          </div>
        </div>
      )}

      <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-xs leading-6 text-slate-600">
        <strong className="text-slate-900">Metodologia:</strong> desvio D-index em relação ao H-index; vitalidade ponderada por citações (50%), H-index (30%) e i10-index (20%) dos últimos 6 anos; domínio institucional validado contra a lista oficial configurada no ambiente.
      </div>
    </section>
  );
}
