import { useEffect, useState } from "react";
import { Activity, Database, RefreshCw, ShieldCheck, Wifi } from "lucide-react";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import StatCard from "../components/ui/StatCard";
import { APP_CONFIG } from "../config/app";
import { dashboardService } from "../services/dashboardService";
import { formatDateTime, formatStatus } from "../utils/formatters";

export default function OperationalStatus() {
  const [status, setStatus] = useState(null);
  const [openApi, setOpenApi] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadStatus() {
    setLoading(true);
    setError("");

    try {
      const [statusData, openApiData] = await Promise.all([
        dashboardService.getOperationalStatus(),
        dashboardService.getOpenApiSummary()
      ]);

      setStatus(statusData);
      setOpenApi(openApiData);
    } catch (apiError) {
      setError(
        apiError?.message || "Não foi possível carregar o status operacional."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadStatus();
  }, []);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Monitoramento"
        title="Status Operacional"
        description="Acompanhe a saúde da API, segurança, banco de dados, OpenAPI JSON e integrações principais."
        actions={
          <PrimaryButton variant="light" icon={RefreshCw} onClick={loadStatus}>
            Atualizar
          </PrimaryButton>
        }
      />

      {loading && <LoadingState message="Consultando status da API..." />}

      {!loading && error && (
        <ErrorState title="Erro ao carregar status operacional" message={error} />
      )}

      {!loading && !error && (
        <>
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard
              title="API"
              value={formatStatus(status?.status)}
              description={status?.api || "Academic Optimization Platform"}
              icon={Wifi}
            />

            <StatCard
              title="Banco de dados"
              value={status?.database || "Não informado"}
              description="Status reportado pelo backend."
              icon={Database}
            />

            <StatCard
              title="Segurança"
              value={status?.security || "JWT ativo"}
              description="Autenticação e rotas protegidas."
              icon={ShieldCheck}
            />

            <StatCard
              title="OpenAPI"
              value={`${openApi?.totalPaths || 0} paths`}
              description="Contrato JSON publicado em produção."
              icon={Activity}
            />
          </section>

          <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="font-black text-slate-950">Detalhes técnicos</h3>

            <div className="mt-5 grid gap-4 md:grid-cols-2">
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                  Base URL
                </p>
                <p className="mt-2 break-all font-mono text-sm font-bold text-slate-950">
                  {APP_CONFIG.apiBaseUrl}
                </p>
              </div>

              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                  OpenAPI JSON
                </p>
                <p className="mt-2 break-all font-mono text-sm font-bold text-slate-950">
                  {APP_CONFIG.openApiUrl}
                </p>
              </div>

              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                  Endpoint fonte do status
                </p>
                <p className="mt-2 break-all font-mono text-sm font-bold text-slate-950">
                  {status?.sourceEndpoint || "Não informado"}
                </p>
              </div>

              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                  Última verificação
                </p>
                <p className="mt-2 font-bold text-slate-950">
                  {formatDateTime(status?.generatedAt)}
                </p>
              </div>
            </div>
          </section>
        </>
      )}
    </div>
  );
}