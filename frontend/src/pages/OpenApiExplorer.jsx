import { useEffect, useState } from "react";
import { ExternalLink, RefreshCw, ShieldCheck } from "lucide-react";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import Badge from "../components/ui/Badge";
import { APP_CONFIG } from "../config/app";
import { dashboardService } from "../services/dashboardService";
import { formatNumber } from "../utils/formatters";

export default function OpenApiExplorer() {
  const [openApi, setOpenApi] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadOpenApi() {
    setLoading(true);
    setError("");

    try {
      const data = await dashboardService.getOpenApiSummary();
      setOpenApi(data);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar o OpenAPI JSON.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadOpenApi();
  }, []);

  const paths = openApi?.paths ? Object.entries(openApi.paths) : [];

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Contrato da API"
        title="OpenAPI JSON"
        description="Swagger UI foi desativado de propósito. O painel administrativo usa apenas o contrato JSON publicado em produção."
        actions={
          <>
            <PrimaryButton variant="light" icon={RefreshCw} onClick={loadOpenApi}>
              Atualizar
            </PrimaryButton>

            <a href={APP_CONFIG.openApiUrl} target="_blank" rel="noreferrer">
              <PrimaryButton icon={ExternalLink}>Abrir JSON</PrimaryButton>
            </a>
          </>
        }
      >
        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-2xl bg-slate-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
              API
            </p>
            <p className="mt-2 font-black text-slate-950">
              {openApi?.title || "Academic Optimization Platform API"}
            </p>
          </div>

          <div className="rounded-2xl bg-slate-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
              Versão
            </p>
            <p className="mt-2 font-black text-slate-950">
              {openApi?.version || "Não informada"}
            </p>
          </div>

          <div className="rounded-2xl bg-slate-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
              Endpoints
            </p>
            <p className="mt-2 font-black text-slate-950">
              {formatNumber(openApi?.totalPaths || 0)}
            </p>
          </div>
        </div>
      </PageHeader>

      {loading && <LoadingState message="Carregando contrato OpenAPI..." />}

      {!loading && error && (
        <ErrorState title="Erro ao carregar OpenAPI" message={error} />
      )}

      {!loading && !error && paths.length === 0 && (
        <EmptyState
          icon={ShieldCheck}
          title="Nenhum endpoint encontrado"
          description="O contrato OpenAPI foi carregado, mas não retornou paths."
        />
      )}

      {!loading && !error && paths.length > 0 && (
        <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-5 flex items-center justify-between gap-4">
            <div>
              <h3 className="font-black text-slate-950">Endpoints publicados</h3>
              <p className="mt-1 text-sm text-slate-500">
                Resumo dos paths disponíveis no backend.
              </p>
            </div>

            <Badge variant="blue">{formatNumber(paths.length)} paths</Badge>
          </div>

          <div className="space-y-3">
            {paths.map(([path, methods]) => (
              <div
                key={path}
                className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
              >
                <p className="break-all font-mono text-sm font-bold text-slate-950">
                  {path}
                </p>

                <div className="mt-3 flex flex-wrap gap-2">
                  {Object.keys(methods || {}).map((method) => (
                    <Badge key={`${path}-${method}`} variant="slate">
                      {method.toUpperCase()}
                    </Badge>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}