import { useEffect, useMemo, useState } from "react";
import { Download, FileText, Printer, RefreshCw, Search, Wand2 } from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { reportService } from "../services/reportService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

function pickReportId(report) {
  return report?.id || report?.reportId || report?.optimizationReportId || null;
}

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

function getPriorityVariant(priority) {
  const normalized = String(priority || "").toUpperCase();

  if (normalized === "ALTA") return "red";
  if (normalized === "MÉDIA" || normalized === "MEDIA") return "amber";
  return "blue";
}

export default function Reports() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [reports, setReports] = useState([]);
  const [selectedReport, setSelectedReport] = useState(null);
  const [query, setQuery] = useState("");
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingData, setLoadingData] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const selectedResearcher = useMemo(() => {
    return researchers.find((researcher) => researcher.id === selectedResearcherId);
  }, [researchers, selectedResearcherId]);

  const filteredReports = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) return reports;

    return reports.filter((report) => {
      const searchable = JSON.stringify(report || {}).toLowerCase();
      return searchable.includes(normalizedQuery);
    });
  }, [reports, query]);

  async function loadResearchers() {
    setLoadingResearchers(true);
    setError("");

    try {
      const data = await researcherService.findAll();
      setResearchers(data);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar pesquisadores.");
    } finally {
      setLoadingResearchers(false);
    }
  }

  async function loadReports(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingData(true);
    setError("");
    setSuccess("");

    try {
      const [list, latest] = await Promise.all([
        reportService.findByResearcher(researcherId),
        reportService.findLatestByResearcher(researcherId)
      ]);

      const nextReports = list.length > 0 ? list : latest ? [latest] : [];

      setReports(nextReports);
      setSelectedReport(latest || nextReports[0] || null);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar relatórios.");
    } finally {
      setLoadingData(false);
    }
  }

  async function handleGenerateReport() {
    if (!selectedResearcherId) return;

    setGenerating(true);
    setError("");
    setSuccess("");

    try {
      const report = await reportService.generateByResearcher(selectedResearcherId);
      setSelectedReport(report);
      setSuccess("Relatório de otimização acadêmica gerado com sucesso.");

      await loadReports(selectedResearcherId);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível gerar relatório.");
    } finally {
      setGenerating(false);
    }
  }

  async function handleDownloadPdf() {
    if (!selectedResearcherId || !selectedReport) return;

    setDownloading(true);
    setError("");
    setSuccess("");

    try {
      const reportId = pickReportId(selectedReport);

      const file = await reportService.downloadPdf({
        researcherId: selectedResearcherId,
        reportId
      });

      downloadBlob(file.blob, file.filename);
      setSuccess("PDF do relatório baixado com sucesso.");
    } catch {
      setError(
        "Endpoint de PDF não encontrado na API. Use o botão Imprimir/Salvar PDF do navegador para exportar este relatório."
      );
    } finally {
      setDownloading(false);
    }
  }

  function handlePrintReport() {
    window.print();
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      loadReports(selectedResearcherId);
    }
  }, [selectedResearcherId]);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Otimização acadêmica"
        title="Relatórios"
        description="Geração, visualização e exportação do relatório de otimização acadêmica do pesquisador."
        actions={
          <PrimaryButton variant="light" icon={RefreshCw} onClick={loadResearchers}>
            Atualizar pesquisadores
          </PrimaryButton>
        }
      />

      {error && <ErrorState title="Atenção" message={error} />}

      {success && (
        <div className="rounded-3xl border border-emerald-200 bg-emerald-50 p-4 text-sm font-semibold text-emerald-800">
          {success}
        </div>
      )}

      {loadingResearchers && (
        <LoadingState message="Carregando pesquisadores para relatórios..." />
      )}

      {!loadingResearchers && (
        <>
          <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="grid gap-4 lg:grid-cols-[1fr_auto] lg:items-end">
              <div>
                <label className="text-sm font-bold text-slate-700">
                  Pesquisador
                </label>

                <select
                  value={selectedResearcherId}
                  onChange={(event) => {
                    setSelectedResearcherId(event.target.value);
                    setReports([]);
                    setSelectedReport(null);
                    setError("");
                    setSuccess("");
                  }}
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
                  onClick={() => loadReports()}
                >
                  Carregar
                </PrimaryButton>

                <PrimaryButton
                  icon={Wand2}
                  loading={generating}
                  disabled={!selectedResearcherId}
                  onClick={handleGenerateReport}
                >
                  Gerar relatório
                </PrimaryButton>

                <PrimaryButton
                  variant="light"
                  icon={Printer}
                  disabled={!selectedReport}
                  onClick={handlePrintReport}
                >
                  Imprimir / PDF
                </PrimaryButton>

                <PrimaryButton
                  variant="dark"
                  icon={Download}
                  loading={downloading}
                  disabled={!selectedResearcherId || !selectedReport}
                  onClick={handleDownloadPdf}
                >
                  Baixar PDF API
                </PrimaryButton>
              </div>
            </div>

            {selectedResearcher && (
              <div className="mt-5 grid gap-4 md:grid-cols-3">
                <div className="rounded-2xl bg-slate-50 p-4">
                  <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                    Pesquisador
                  </p>
                  <p className="mt-2 font-black text-slate-950">
                    {selectedResearcher.fullName}
                  </p>
                </div>

                <div className="rounded-2xl bg-slate-50 p-4">
                  <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                    Instituição
                  </p>
                  <p className="mt-2 font-black text-slate-950">
                    {selectedResearcher.institution || "Não informada"}
                  </p>
                </div>

                <div className="rounded-2xl bg-slate-50 p-4">
                  <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                    Relatórios locais
                  </p>
                  <p className="mt-2 font-black text-slate-950">
                    {formatNumber(reports.length)}
                  </p>
                </div>
              </div>
            )}
          </section>

          {!selectedResearcherId && (
            <EmptyState
              icon={FileText}
              title="Selecione um pesquisador"
              description="Escolha um pesquisador para gerar ou visualizar relatórios."
            />
          )}

          {selectedResearcherId && loadingData && (
            <LoadingState message="Carregando relatórios..." />
          )}

          {selectedResearcherId && !loadingData && (
            <section className="grid gap-6 xl:grid-cols-[0.8fr_1.2fr]">
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-5 flex items-center justify-between">
                  <h3 className="font-black text-slate-950">
                    Histórico de relatórios
                  </h3>
                  <Badge variant="blue">{filteredReports.length} registros</Badge>
                </div>

                <div className="mb-5 flex items-center rounded-2xl border border-slate-200 bg-slate-50 px-4">
                  <Search className="h-5 w-5 text-slate-400" />
                  <input
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    className="w-full bg-transparent px-3 py-3 text-sm outline-none"
                    placeholder="Buscar no histórico..."
                  />
                </div>

                {filteredReports.length === 0 ? (
                  <p className="text-sm text-slate-500">
                    Nenhum relatório encontrado. Clique em gerar relatório.
                  </p>
                ) : (
                  <div className="space-y-3">
                    {filteredReports.map((report, index) => (
                      <button
                        key={pickReportId(report) || index}
                        type="button"
                        onClick={() => setSelectedReport(report)}
                        className="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-left transition hover:border-blue-300 hover:bg-blue-50"
                      >
                        <p className="font-black text-slate-950">
                          {report.title ||
                            report.reportTitle ||
                            `Relatório #${index + 1}`}
                        </p>

                        <p className="mt-1 text-xs text-slate-500">
                          {formatDateTime(
                            report.generatedAt ||
                              report.createdAt ||
                              report.updatedAt
                          )}
                        </p>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="font-black text-slate-950">
                  Visualização do relatório
                </h3>

                {!selectedReport ? (
                  <p className="mt-3 text-sm text-slate-500">
                    Nenhum relatório selecionado.
                  </p>
                ) : (
                  <div className="mt-5 space-y-6">
                    <div className="rounded-3xl bg-slate-950 p-6 text-white">
                      <p className="text-xs font-bold uppercase tracking-[0.25em] text-blue-200">
                        Relatório de Otimização Acadêmica
                      </p>

                      <h2 className="mt-3 text-2xl font-black">
                        {selectedReport.researcherName ||
                          selectedReport.researcher?.fullName ||
                          "Pesquisador"}
                      </h2>

                      <p className="mt-2 text-sm text-slate-300">
                        Gerado em {formatDateTime(selectedReport.generatedAt)}
                      </p>

                      <div className="mt-5 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                        <div className="rounded-2xl bg-white/10 p-4">
                          <p className="text-xs text-slate-300">Score</p>
                          <p className="mt-2 text-3xl font-black">
                            {selectedReport.score || 0}%
                          </p>
                        </div>

                        <div className="rounded-2xl bg-white/10 p-4">
                          <p className="text-xs text-slate-300">OpenAlex</p>
                          <p className="mt-2 text-3xl font-black">
                            {selectedReport.summary?.totalOpenAlexWorks || 0}
                          </p>
                        </div>

                        <div className="rounded-2xl bg-white/10 p-4">
                          <p className="text-xs text-slate-300">ORCID</p>
                          <p className="mt-2 text-3xl font-black">
                            {selectedReport.summary?.totalOrcidWorks || 0}
                          </p>
                        </div>

                        <div className="rounded-2xl bg-white/10 p-4">
                          <p className="text-xs text-slate-300">Crossref</p>
                          <p className="mt-2 text-3xl font-black">
                            {selectedReport.summary?.totalCrossrefValidations || 0}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="rounded-3xl border border-slate-200 bg-slate-50 p-5">
                      <h4 className="font-black text-slate-950">
                        Recomendações
                      </h4>

                      <div className="mt-4 space-y-3">
                        {(selectedReport.recommendations || []).map(
                          (recommendation, index) => (
                            <div
                              key={`${recommendation.title}-${index}`}
                              className="rounded-2xl border border-slate-200 bg-white p-4"
                            >
                              <div className="mb-2">
                                <Badge
                                  variant={getPriorityVariant(
                                    recommendation.priority
                                  )}
                                >
                                  {recommendation.priority || "INFO"}
                                </Badge>
                              </div>

                              <p className="font-black text-slate-950">
                                {recommendation.title}
                              </p>

                              <p className="mt-2 text-sm leading-7 text-slate-600">
                                {recommendation.description}
                              </p>
                            </div>
                          )
                        )}
                      </div>
                    </div>

                    <details className="rounded-3xl border border-slate-200 bg-white p-5">
                      <summary className="cursor-pointer font-black text-slate-950">
                        Ver dados brutos do relatório
                      </summary>

                      <pre className="mt-5 max-h-[560px] overflow-auto whitespace-pre-wrap rounded-2xl bg-slate-950 p-5 text-xs leading-6 text-slate-100">
                        {JSON.stringify(selectedReport, null, 2)}
                      </pre>
                    </details>
                  </div>
                )}
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}