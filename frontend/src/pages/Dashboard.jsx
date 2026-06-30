import { useEffect, useState } from "react";
import {
  Activity,
  BookOpenCheck,
  ClipboardCheck,
  Database,
  FileText,
  GraduationCap,
  History,
  LibraryBig,
  Link2,
  Network,
  RefreshCw,
  ShieldCheck,
  UserRoundSearch
} from "lucide-react";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import StatCard from "../components/ui/StatCard";
import { APP_CONFIG } from "../config/app";
import { dashboardService } from "../services/dashboardService";
import { formatDateTime, formatNumber, formatStatus } from "../utils/formatters";

export default function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [status, setStatus] = useState(null);
  const [openApi, setOpenApi] = useState(null);
  const [errors, setErrors] = useState([]);
  const [loading, setLoading] = useState(true);

  async function loadDashboard() {
    setLoading(true);
    setErrors([]);

    const results = await Promise.allSettled([
      dashboardService.getInstitutionalDashboard(),
      dashboardService.getOperationalStatus(),
      dashboardService.getOpenApiSummary()
    ]);

    const nextErrors = [];

    if (results[0].status === "fulfilled") {
      setDashboard(results[0].value);
    } else {
      nextErrors.push({
        title: "Dashboard institucional",
        message: results[0].reason?.message
      });
    }

    if (results[1].status === "fulfilled") {
      setStatus(results[1].value);
    } else {
      nextErrors.push({
        title: "Status operacional",
        message: results[1].reason?.message
      });
    }

    if (results[2].status === "fulfilled") {
      setOpenApi(results[2].value);
    } else {
      nextErrors.push({
        title: "OpenAPI JSON",
        message: results[2].reason?.message
      });
    }

    setErrors(nextErrors);
    setLoading(false);
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  if (loading) {
    return (
      <LoadingState message="Consolidando dados institucionais da API em produção..." />
    );
  }

  const stats = [
    {
      title: "Pesquisadores",
      value: formatNumber(dashboard?.totalResearchers),
      description: "Registros de pesquisadores cadastrados.",
      icon: UserRoundSearch
    },
    {
      title: "Perfis Acadêmicos",
      value: formatNumber(dashboard?.totalAcademicProfiles),
      description: "Perfis acadêmicos vinculados aos pesquisadores.",
      icon: GraduationCap
    },
    {
      title: "Pesquisadores com ORCID",
      value: formatNumber(dashboard?.researchersWithOrcid),
      description: "Pesquisadores com ORCID informado no cadastro.",
      icon: Link2
    },
    {
      title: "Obras ORCID",
      value: formatNumber(dashboard?.totalOrcidWorks),
      description: "Obras importadas ou vinculadas via ORCID.",
      icon: BookOpenCheck
    },
    {
      title: "Obras OpenAlex",
      value: formatNumber(dashboard?.totalOpenAlexWorks),
      description: "Obras encontradas ou importadas do OpenAlex.",
      icon: Network
    },
    {
      title: "Obras Revisadas",
      value: formatNumber(dashboard?.reviewedWorks),
      description: "Obras confirmadas ou rejeitadas na revisão manual.",
      icon: ClipboardCheck
    },
    {
      title: "DOIs Validados",
      value: formatNumber(dashboard?.validatedDois),
      description: "Validações DOI e metadados via Crossref.",
      icon: LibraryBig
    },
    {
      title: "Eventos de Auditoria",
      value: formatNumber(dashboard?.auditEvents),
      description: "Eventos de rastreabilidade encontrados.",
      icon: History
    }
  ];

  return (
    <div className="space-y-8">
      <section className="rounded-[2rem] bg-slate-950 p-6 text-white shadow-xl shadow-slate-950/10 sm:p-8">
        <div className="grid gap-8 lg:grid-cols-[1.3fr_0.7fr] lg:items-end">
          <div>
            <div className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/10 px-4 py-2 text-xs font-semibold uppercase tracking-[0.2em] text-blue-100">
              <ShieldCheck className="h-4 w-4" />
              Produção
            </div>

            <h2 className="mt-6 max-w-4xl text-3xl font-black tracking-tight sm:text-4xl">
              Dashboard Institucional IMETRO
            </h2>

            <p className="mt-4 max-w-3xl text-sm leading-7 text-slate-300 sm:text-base">
              Visão executiva consolidada da plataforma acadêmica: pesquisadores,
              perfis, ORCID, OpenAlex, Crossref, relatórios, auditoria, checklist
              Google Acadêmico e status operacional.
            </p>

            <div className="mt-6">
              <PrimaryButton variant="light" icon={RefreshCw} onClick={loadDashboard}>
                Atualizar dashboard
              </PrimaryButton>
            </div>
          </div>

          <div className="rounded-3xl border border-white/10 bg-white/10 p-5">
            <div className="flex items-center gap-3">
              <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-400/20 text-emerald-300">
                <Activity className="h-6 w-6" />
              </div>

              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">
                  API
                </p>
                <p className="text-lg font-bold">
                  {formatStatus(status?.status || "ONLINE")}
                </p>
              </div>
            </div>

            <p className="mt-4 text-xs leading-6 text-slate-400">
              Base URL: {APP_CONFIG.apiBaseUrl}
            </p>
          </div>
        </div>
      </section>

      {errors.length > 0 && (
        <div className="space-y-3">
          {errors.map((error) => (
            <ErrorState
              key={error.title}
              title={error.title}
              message={error.message}
            />
          ))}
        </div>
      )}

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((item) => (
          <StatCard
            key={item.title}
            title={item.title}
            value={item.value}
            description={item.description}
            icon={item.icon}
          />
        ))}
      </section>

      <section className="grid gap-6 xl:grid-cols-3">
        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm xl:col-span-2">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-blue-50 text-blue-700">
              <Database className="h-6 w-6" />
            </div>

            <div>
              <h3 className="font-bold text-slate-950">Status operacional</h3>
              <p className="text-sm text-slate-500">
                Saúde geral dos serviços integrados.
              </p>
            </div>
          </div>

          <div className="mt-6 grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Aplicação
              </p>
              <p className="mt-2 font-bold text-slate-950">
                {status?.api || "Academic Optimization Platform API"}
              </p>
            </div>

            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Banco de dados
              </p>
              <p className="mt-2 font-bold text-slate-950">
                {status?.database || "PostgreSQL"}
              </p>
            </div>

            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Segurança
              </p>
              <p className="mt-2 font-bold text-slate-950">
                {status?.security || "JWT ativo"}
              </p>
            </div>

            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Última atualização
              </p>
              <p className="mt-2 font-bold text-slate-950">
                {formatDateTime(status?.generatedAt || dashboard?.lastSync)}
              </p>
            </div>
          </div>
        </div>

        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h3 className="font-bold text-slate-950">OpenAPI JSON</h3>

          <p className="mt-2 text-sm leading-6 text-slate-500">
            Swagger UI foi desativado de propósito. O frontend usa o contrato
            JSON publicado em produção.
          </p>

          <div className="mt-6 space-y-3">
            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Título
              </p>
              <p className="mt-2 font-bold text-slate-950">
                {openApi?.title || "OpenAPI JSON"}
              </p>
            </div>

            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                Endpoints mapeados
              </p>
              <p className="mt-2 font-bold text-slate-950">
                {formatNumber(openApi?.totalPaths)}
              </p>
            </div>

            <a
              href={APP_CONFIG.openApiUrl}
              target="_blank"
              rel="noreferrer"
              className="inline-flex w-full justify-center rounded-2xl bg-blue-700 px-4 py-3 text-sm font-bold text-white hover:bg-blue-800"
            >
              Abrir OpenAPI JSON
            </a>
          </div>
        </div>
      </section>

      <section className="grid gap-6 xl:grid-cols-3">
        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <h3 className="font-black text-slate-950">Revisão Manual</h3>

          <div className="mt-5 space-y-3">
            <div className="flex items-center justify-between rounded-2xl bg-slate-50 p-4">
              <span className="text-sm font-semibold text-slate-600">
                Pendentes
              </span>
              <span className="text-xl font-black text-amber-600">
                {formatNumber(dashboard?.pendingReviewWorks)}
              </span>
            </div>

            <div className="flex items-center justify-between rounded-2xl bg-slate-50 p-4">
              <span className="text-sm font-semibold text-slate-600">
                Confirmadas
              </span>
              <span className="text-xl font-black text-emerald-600">
                {formatNumber(dashboard?.confirmedWorks)}
              </span>
            </div>

            <div className="flex items-center justify-between rounded-2xl bg-slate-50 p-4">
              <span className="text-sm font-semibold text-slate-600">
                Rejeitadas
              </span>
              <span className="text-xl font-black text-red-600">
                {formatNumber(dashboard?.rejectedWorks)}
              </span>
            </div>
          </div>
        </div>

        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm xl:col-span-2">
          <h3 className="font-black text-slate-950">
            Checklist Google Acadêmico
          </h3>

          <p className="mt-3 text-sm leading-7 text-slate-600">
            {APP_CONFIG.googleScholarPolicy}
          </p>

          <div className="mt-5 rounded-2xl border border-amber-200 bg-amber-50 p-4">
            <p className="text-sm leading-7 text-amber-900">
              O painel apenas orienta o pesquisador a revisar manualmente seu
              perfil no Google Acadêmico. As integrações automáticas permitidas
              continuam sendo ORCID, OpenAlex e Crossref.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}