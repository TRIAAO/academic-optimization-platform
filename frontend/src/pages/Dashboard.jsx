import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  AlertTriangle,
  ArrowRight,
  BookOpenCheck,
  CheckCircle2,
  ClipboardCheck,
  FileText,
  GraduationCap,
  LibraryBig,
  Link2,
  Network,
  RefreshCw,
  SearchCheck,
  Sparkles,
  UserRoundSearch
} from "lucide-react";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PrimaryButton from "../components/ui/PrimaryButton";
import { APP_CONFIG } from "../config/app";
import { dashboardService } from "../services/dashboardService";
import { formatDateTime, formatNumber } from "../utils/formatters";

function percentage(value, total) {
  if (!total || total <= 0) return 0;
  return Math.round((Number(value || 0) / Number(total)) * 100);
}

function MetricCard({ title, value, description, icon: Icon, tone = "blue" }) {
  const tones = {
    blue: "bg-blue-50 text-blue-700 ring-blue-100",
    emerald: "bg-emerald-50 text-emerald-700 ring-emerald-100",
    amber: "bg-amber-50 text-amber-700 ring-amber-100",
    violet: "bg-violet-50 text-violet-700 ring-violet-100",
    rose: "bg-rose-50 text-rose-700 ring-rose-100",
    slate: "bg-slate-50 text-slate-700 ring-slate-100"
  };

  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-slate-500">{title}</p>
          <p className="mt-3 text-4xl font-black tracking-tight text-slate-950">
            {value}
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

function ProgressCard({ title, value, description, tone = "blue" }) {
  const width = Math.min(Math.max(Number(value || 0), 0), 100);

  const bars = {
    blue: "bg-blue-600",
    emerald: "bg-emerald-600",
    amber: "bg-amber-500",
    rose: "bg-rose-600"
  };

  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="font-black text-slate-950">{title}</p>
          <p className="mt-1 text-sm leading-6 text-slate-500">{description}</p>
        </div>

        <p className="text-2xl font-black text-slate-950">{width}%</p>
      </div>

      <div className="mt-5 h-3 overflow-hidden rounded-full bg-slate-100">
        <div
          className={`h-full rounded-full ${bars[tone] || bars.blue}`}
          style={{ width: `${width}%` }}
        />
      </div>
    </div>
  );
}

function ActionCard({ title, description, href, icon: Icon }) {
  return (
    <Link
      to={href}
      className="group rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-blue-200 hover:shadow-md"
    >
      <div className="flex items-start justify-between gap-4">
        <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-blue-700 ring-1 ring-blue-100">
          <Icon className="h-6 w-6" />
        </div>

        <ArrowRight className="h-5 w-5 text-slate-300 transition group-hover:translate-x-1 group-hover:text-blue-700" />
      </div>

      <h3 className="mt-5 font-black text-slate-950">{title}</h3>

      <p className="mt-2 text-sm leading-6 text-slate-500">{description}</p>
    </Link>
  );
}

export default function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function loadDashboard() {
    setLoading(true);
    setError("");

    try {
      const data = await dashboardService.getInstitutionalDashboard();
      setDashboard(data);
    } catch (apiError) {
      setError(
        apiError?.message ||
          "Não foi possível carregar o dashboard institucional."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  const orcidCoverage = useMemo(() => {
    return percentage(dashboard?.researchersWithOrcid, dashboard?.totalResearchers);
  }, [dashboard]);

  const profileCoverage = useMemo(() => {
    return percentage(
      dashboard?.totalAcademicProfiles,
      dashboard?.totalResearchers
    );
  }, [dashboard]);

  const reviewCompletion = useMemo(() => {
    return percentage(dashboard?.reviewedWorks, dashboard?.totalOpenAlexWorks);
  }, [dashboard]);

  if (loading) {
    return (
      <LoadingState message="Carregando visão institucional da plataforma..." />
    );
  }

  return (
    <div className="space-y-8">
      <section className="overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-sm">
        <div className="relative p-6 sm:p-8">
          <div className="absolute right-0 top-0 h-40 w-40 rounded-bl-[4rem] bg-blue-50" />

          <div className="relative grid gap-8 xl:grid-cols-[1.1fr_0.9fr] xl:items-end">
            <div>
              <div className="inline-flex items-center gap-2 rounded-full border border-blue-100 bg-blue-50 px-4 py-2 text-xs font-black uppercase tracking-[0.2em] text-blue-700">
                <Sparkles className="h-4 w-4" />
                Visão institucional
              </div>

              <h1 className="mt-5 max-w-4xl text-3xl font-black tracking-tight text-slate-950 sm:text-4xl">
                Dashboard Institucional IMETRO
              </h1>

              <p className="mt-4 max-w-4xl text-sm leading-7 text-slate-600 sm:text-base">
                Acompanhe de forma simples a evolução dos pesquisadores, perfis
                acadêmicos, ORCID, obras científicas, validações DOI e relatórios
                de otimização acadêmica.
              </p>
            </div>

            <div className="flex flex-col gap-3 rounded-3xl bg-slate-50 p-5 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                  Última atualização
                </p>
                <p className="mt-1 font-black text-slate-950">
                  {formatDateTime(dashboard?.lastSync)}
                </p>
              </div>

              <PrimaryButton
                variant="light"
                icon={RefreshCw}
                onClick={loadDashboard}
              >
                Atualizar
              </PrimaryButton>
            </div>
          </div>
        </div>
      </section>

      {error && (
        <ErrorState title="Erro no dashboard institucional" message={error} />
      )}

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard
          title="Pesquisadores"
          value={formatNumber(dashboard?.totalResearchers)}
          description="Professores e pesquisadores cadastrados na plataforma."
          icon={UserRoundSearch}
          tone="blue"
        />

        <MetricCard
          title="Perfis Acadêmicos"
          value={formatNumber(dashboard?.totalAcademicProfiles)}
          description="Perfis acadêmicos estruturados e vinculados."
          icon={GraduationCap}
          tone="violet"
        />

        <MetricCard
          title="Pesquisadores com ORCID"
          value={formatNumber(dashboard?.researchersWithOrcid)}
          description="Pesquisadores com identificador ORCID informado."
          icon={Link2}
          tone="emerald"
        />

        <MetricCard
          title="Obras ORCID"
          value={formatNumber(dashboard?.totalOrcidWorks)}
          description="Obras acadêmicas vinculadas ou importadas via ORCID."
          icon={BookOpenCheck}
          tone="amber"
        />

        <MetricCard
          title="Obras OpenAlex"
          value={formatNumber(dashboard?.totalOpenAlexWorks)}
          description="Obras encontradas ou importadas para análise."
          icon={Network}
          tone="blue"
        />

        <MetricCard
          title="Obras Revisadas"
          value={formatNumber(dashboard?.reviewedWorks)}
          description="Obras confirmadas ou rejeitadas pela revisão manual."
          icon={ClipboardCheck}
          tone="emerald"
        />

        <MetricCard
          title="DOIs Validados"
          value={formatNumber(dashboard?.validatedDois)}
          description="Validações bibliográficas realizadas via Crossref."
          icon={LibraryBig}
          tone="violet"
        />

        <MetricCard
          title="Relatórios"
          value={formatNumber(dashboard?.optimizationReports || 0)}
          description="Relatórios de otimização acadêmica gerados."
          icon={FileText}
          tone="slate"
        />
      </section>

      <section className="grid gap-4 xl:grid-cols-3">
        <ProgressCard
          title="Cobertura ORCID"
          value={orcidCoverage}
          description="Percentual de pesquisadores com ORCID informado."
          tone="emerald"
        />

        <ProgressCard
          title="Perfis acadêmicos"
          value={profileCoverage}
          description="Percentual de pesquisadores com perfil acadêmico vinculado."
          tone="blue"
        />

        <ProgressCard
          title="Revisão de obras"
          value={reviewCompletion}
          description="Percentual de obras OpenAlex já revisadas."
          tone={reviewCompletion >= 70 ? "emerald" : "amber"}
        />
      </section>

      <section className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h3 className="font-black text-slate-950">
                Situação da revisão manual
              </h3>
              <p className="mt-2 text-sm leading-7 text-slate-500">
                A revisão manual reduz risco de obras atribuídas incorretamente
                aos pesquisadores.
              </p>
            </div>

            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-blue-700">
              <SearchCheck className="h-6 w-6" />
            </div>
          </div>

          <div className="mt-6 space-y-3">
            <div className="flex items-center justify-between rounded-2xl bg-amber-50 p-4">
              <span className="text-sm font-semibold text-amber-900">
                Pendentes
              </span>
              <span className="text-xl font-black text-amber-700">
                {formatNumber(dashboard?.pendingReviewWorks)}
              </span>
            </div>

            <div className="flex items-center justify-between rounded-2xl bg-emerald-50 p-4">
              <span className="text-sm font-semibold text-emerald-900">
                Confirmadas
              </span>
              <span className="text-xl font-black text-emerald-700">
                {formatNumber(dashboard?.confirmedWorks)}
              </span>
            </div>

            <div className="flex items-center justify-between rounded-2xl bg-red-50 p-4">
              <span className="text-sm font-semibold text-red-900">
                Rejeitadas
              </span>
              <span className="text-xl font-black text-red-700">
                {formatNumber(dashboard?.rejectedWorks)}
              </span>
            </div>
          </div>

          <div className="mt-6">
            <Link to="/admin/manual-review">
              <PrimaryButton variant="light" icon={ClipboardCheck}>
                Ir para revisão manual
              </PrimaryButton>
            </Link>
          </div>
        </div>

        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h3 className="font-black text-slate-950">
                Checklist Google Acadêmico
              </h3>

              <p className="mt-2 text-sm leading-7 text-slate-500">
                Checklist personalizado para orientar a revisão manual do perfil
                público do pesquisador.
              </p>
            </div>

            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-50 text-amber-700">
              <AlertTriangle className="h-6 w-6" />
            </div>
          </div>

          <div className="mt-6 rounded-2xl border border-amber-200 bg-amber-50 p-5">
            <p className="text-sm leading-7 text-amber-900">
              {APP_CONFIG.googleScholarPolicy}
            </p>
          </div>

          <div className="mt-6">
            <Link to="/admin/google-scholar-checklist">
              <PrimaryButton variant="light" icon={CheckCircle2}>
                Abrir checklist
              </PrimaryButton>
            </Link>
          </div>
        </div>
      </section>

      <section>
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h3 className="font-black text-slate-950">Ações rápidas</h3>
            <p className="mt-1 text-sm text-slate-500">
              Acesse os principais módulos acadêmicos da plataforma.
            </p>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <ActionCard
            title="Pesquisadores"
            description="Gerir cadastro, dados institucionais e ORCID dos pesquisadores."
            href="/admin/researchers"
            icon={UserRoundSearch}
          />

          <ActionCard
            title="Perfil Acadêmico"
            description="Organizar biografia, área científica, palavras-chave e perfis públicos."
            href="/admin/academic-profiles"
            icon={GraduationCap}
          />

          <ActionCard
            title="ORCID"
            description="Importar obras, consultar resumo e sincronizar dados acadêmicos."
            href="/admin/orcid"
            icon={Link2}
          />

          <ActionCard
            title="OpenAlex"
            description="Buscar autores, candidatos e obras científicas para validação."
            href="/admin/openalex"
            icon={Network}
          />

          <ActionCard
            title="Crossref / DOI"
            description="Validar DOI e metadados bibliográficos das obras."
            href="/admin/crossref"
            icon={LibraryBig}
          />

          <ActionCard
            title="Relatórios"
            description="Gerar relatórios de otimização acadêmica dos pesquisadores."
            href="/admin/reports"
            icon={FileText}
          />
        </div>
      </section>
    </div>
  );
}