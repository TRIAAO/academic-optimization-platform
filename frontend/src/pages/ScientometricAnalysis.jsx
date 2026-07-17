import { Activity, BarChart3, RefreshCw, ShieldCheck } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import ScientometricAnalysisPanel from "../components/scientometrics/ScientometricAnalysisPanel";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { researcherService } from "../services/researcherService";
import { scientometricMetricService } from "../services/scientometricMetricService";

export default function ScientometricAnalysis() {
  const [searchParams, setSearchParams] = useSearchParams();
  const researcherFromUrl = searchParams.get("researcherId") || "";

  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState(
    researcherFromUrl
  );
  const [analysis, setAnalysis] = useState(null);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);
  const [error, setError] = useState("");

  const selectedResearcher = useMemo(() => {
    return researchers.find(
      (researcher) => researcher.id === selectedResearcherId
    );
  }, [researchers, selectedResearcherId]);

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

  async function loadAnalysis(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingAnalysis(true);
    setError("");

    try {
      const data = await scientometricMetricService.analyze(researcherId);
      setAnalysis(data);
    } catch (apiError) {
      setAnalysis(null);
      setError(
        apiError?.message ||
          "Não foi possível calcular a auditoria cientométrica."
      );
    } finally {
      setLoadingAnalysis(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    setAnalysis(null);

    if (selectedResearcherId) {
      loadAnalysis(selectedResearcherId);
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

  function openMetrics() {
    if (!selectedResearcherId) return;
    window.location.href = `/admin/scientometric-metrics?researcherId=${selectedResearcherId}`;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Integridade e vitalidade científica"
        title="Auditoria Cientométrica"
        description="Regras determinísticas para comparar H-index e D-index, medir a vitalidade dos últimos 6 anos e validar o domínio do e-mail institucional."
        actions={
          <PrimaryButton
            variant="light"
            icon={RefreshCw}
            onClick={loadResearchers}
          >
            Atualizar pesquisadores
          </PrimaryButton>
        }
      />

      <section className="rounded-3xl border border-blue-200 bg-blue-50 p-5 sm:p-6">
        <div className="flex items-start gap-4">
          <ShieldCheck className="mt-0.5 h-6 w-6 shrink-0 text-blue-700" />
          <div>
            <h3 className="font-black text-blue-950">
              Análise explicável e sem alteração automática
            </h3>
            <p className="mt-2 text-sm leading-7 text-blue-900/80">
              O diagnóstico utiliza apenas as medições registadas na plataforma.
              Alertas orientam revisão humana e não modificam perfis públicos,
              indicadores ou fontes externas.
            </p>
          </div>
        </div>
      </section>

      {error && <ErrorState title="Atenção" message={error} />}

      {loadingResearchers && (
        <LoadingState message="Carregando pesquisadores para auditoria..." />
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
                icon={BarChart3}
                disabled={!selectedResearcherId}
                onClick={openMetrics}
              >
                Abrir medições
              </PrimaryButton>
              <PrimaryButton
                icon={RefreshCw}
                disabled={!selectedResearcherId}
                loading={loadingAnalysis}
                onClick={() => loadAnalysis()}
              >
                Recalcular análise
              </PrimaryButton>
            </div>
          </div>

          {selectedResearcher && (
            <div className="mt-5 rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                Pesquisador selecionado
              </p>
              <p className="mt-2 text-sm font-bold text-slate-950 sm:text-base">
                {selectedResearcher.fullName}
              </p>
              <p className="mt-1 text-sm text-slate-500">
                {selectedResearcher.institution || "Instituição não informada"}
              </p>
            </div>
          )}
        </section>
      )}

      {!loadingResearchers && !selectedResearcherId && (
        <EmptyState
          icon={Activity}
          title="Selecione um pesquisador"
          description="Escolha um pesquisador para calcular o diagnóstico determinístico com base nas medições cientométricas existentes."
        />
      )}

      {selectedResearcherId && loadingAnalysis && (
        <ScientometricAnalysisPanel loading />
      )}

      {selectedResearcherId && !loadingAnalysis && analysis && (
        <ScientometricAnalysisPanel analysis={analysis} />
      )}
    </div>
  );
}
