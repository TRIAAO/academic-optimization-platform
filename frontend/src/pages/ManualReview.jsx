import { useEffect, useMemo, useState } from "react";
import {
  BookOpenCheck,
  CheckCircle2,
  Clock3,
  RefreshCw,
  Search,
  ThumbsDown,
  Undo2
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { openAlexService } from "../services/openAlexService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

const REVIEW_STATUSES = [
  {
    value: "ALL",
    label: "Todas"
  },
  {
    value: "PENDING_REVIEW",
    label: "Pendentes"
  },
  {
    value: "CONFIRMED",
    label: "Confirmadas"
  },
  {
    value: "REJECTED",
    label: "Rejeitadas"
  }
];

function getStatusVariant(status) {
  const normalized = String(status || "").toUpperCase();

  if (normalized === "CONFIRMED") return "green";
  if (normalized === "REJECTED") return "red";
  if (normalized === "PENDING_REVIEW") return "amber";

  return "slate";
}

export default function ManualReview() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [reviewStatus, setReviewStatus] = useState("PENDING_REVIEW");
  const [works, setWorks] = useState([]);
  const [query, setQuery] = useState("");
  const [reviewNotes, setReviewNotes] = useState({});
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingWorks, setLoadingWorks] = useState(false);
  const [actionWorkId, setActionWorkId] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const selectedResearcher = useMemo(() => {
    return researchers.find((researcher) => researcher.id === selectedResearcherId);
  }, [researchers, selectedResearcherId]);

  const filteredWorks = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) return works;

    return works.filter((work) => {
      const searchable = [
        work.title,
        work.doi,
        work.sourceName,
        work.publicationYear,
        work.reviewStatus,
        work.reviewNote
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchable.includes(normalizedQuery);
    });
  }, [works, query]);

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

  async function loadWorks() {
    if (!selectedResearcherId) return;

    setLoadingWorks(true);
    setError("");
    setSuccess("");

    try {
      let data = [];

      if (reviewStatus === "ALL") {
        data = await openAlexService.findWorksByResearcher(selectedResearcherId);
      } else if (reviewStatus === "PENDING_REVIEW") {
        data = await openAlexService.findPendingReviewWorks(selectedResearcherId);
      } else {
        data = await openAlexService.findWorksByResearcherAndStatus(
          selectedResearcherId,
          reviewStatus
        );
      }

      setWorks(data);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar obras para revisão.");
    } finally {
      setLoadingWorks(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      loadWorks();
    }
  }, [selectedResearcherId, reviewStatus]);

  function handleNoteChange(workId, value) {
    setReviewNotes((current) => ({
      ...current,
      [workId]: value
    }));
  }

  async function runReviewAction(work, action) {
    setActionWorkId(work.id);
    setError("");
    setSuccess("");

    const note = reviewNotes[work.id] || "";

    try {
      if (action === "confirm") {
        await openAlexService.confirmWork(work.id, note);
        setSuccess("Obra confirmada com sucesso.");
      }

      if (action === "reject") {
        await openAlexService.rejectWork(work.id, note);
        setSuccess("Obra rejeitada com sucesso.");
      }

      if (action === "pending") {
        await openAlexService.markWorkAsPendingReview(work.id, note);
        setSuccess("Obra marcada como pendente de revisão.");
      }

      await loadWorks();
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível executar a revisão.");
    } finally {
      setActionWorkId("");
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Curadoria institucional"
        title="Revisão Manual de Obras"
        description="Confirme, rejeite ou mantenha como pendentes as obras importadas do OpenAlex antes de usá-las nos relatórios acadêmicos."
        actions={
          <PrimaryButton
            variant="light"
            icon={RefreshCw}
            disabled={!selectedResearcherId}
            onClick={loadWorks}
          >
            Atualizar obras
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
        <LoadingState message="Carregando pesquisadores para revisão manual..." />
      )}

      {!loadingResearchers && (
        <>
          <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="grid gap-4 xl:grid-cols-[1fr_240px]">
              <div>
                <label className="text-sm font-bold text-slate-700">
                  Pesquisador
                </label>

                <select
                  value={selectedResearcherId}
                  onChange={(event) => {
                    setSelectedResearcherId(event.target.value);
                    setWorks([]);
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

              <div>
                <label className="text-sm font-bold text-slate-700">Status</label>

                <select
                  value={reviewStatus}
                  onChange={(event) => setReviewStatus(event.target.value)}
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                >
                  {REVIEW_STATUSES.map((status) => (
                    <option key={status.value} value={status.value}>
                      {status.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {selectedResearcher && (
              <div className="mt-5 rounded-2xl bg-slate-50 p-4">
                <p className="font-black text-slate-950">
                  {selectedResearcher.fullName}
                </p>
                <p className="mt-1 text-sm text-slate-500">
                  {selectedResearcher.institution || "Instituição não informada"} ·{" "}
                  {selectedResearcher.department || "Departamento não informado"}
                </p>
              </div>
            )}
          </section>

          {!selectedResearcherId && (
            <EmptyState
              icon={Clock3}
              title="Selecione um pesquisador"
              description="Escolha um pesquisador para revisar as obras importadas do OpenAlex."
            />
          )}

          {selectedResearcherId && loadingWorks && (
            <LoadingState message="Carregando obras para revisão..." />
          )}

          {selectedResearcherId && !loadingWorks && (
            <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
              <div className="mb-5 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div>
                  <h3 className="font-black text-slate-950">
                    Obras para revisão
                  </h3>
                  <p className="mt-1 text-sm text-slate-500">
                    A revisão manual evita associação incorreta de publicações.
                  </p>
                </div>

                <Badge variant="blue">
                  {formatNumber(filteredWorks.length)} obras
                </Badge>
              </div>

              <div className="mb-5 flex items-center rounded-2xl border border-slate-200 bg-slate-50 px-4">
                <Search className="h-5 w-5 text-slate-400" />
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  className="w-full bg-transparent px-3 py-3 text-sm outline-none"
                  placeholder="Buscar por título, DOI, fonte, ano ou observação..."
                />
              </div>

              {filteredWorks.length === 0 ? (
                <EmptyState
                  icon={BookOpenCheck}
                  title="Nenhuma obra encontrada"
                  description="Não há obras para o filtro selecionado. Importe obras pelo módulo OpenAlex."
                />
              ) : (
                <div className="space-y-4">
                  {filteredWorks.map((work) => (
                    <div
                      key={work.id}
                      className="rounded-3xl border border-slate-200 bg-slate-50 p-5"
                    >
                      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                        <div>
                          <p className="text-lg font-black text-slate-950">
                            {work.title || "Obra sem título"}
                          </p>

                          <p className="mt-2 text-sm leading-6 text-slate-500">
                            {work.sourceName || "Fonte não informada"} ·{" "}
                            {work.publicationYear || "Ano não informado"} ·{" "}
                            {work.workType || "Tipo não informado"}
                          </p>

                          <p className="mt-2 break-all text-xs text-slate-500">
                            DOI: {work.doi || "Não informado"}
                          </p>

                          <div className="mt-3 flex flex-wrap gap-2">
                            <Badge variant={getStatusVariant(work.reviewStatus)}>
                              {work.reviewStatus || "Sem status"}
                            </Badge>
                            <Badge variant="blue">
                              {formatNumber(work.citedByCount)} citações
                            </Badge>
                            {work.reviewedAt && (
                              <Badge variant="slate">
                                Revisado em {formatDateTime(work.reviewedAt)}
                              </Badge>
                            )}
                          </div>

                          {work.reviewNote && (
                            <p className="mt-3 rounded-2xl bg-white p-3 text-sm text-slate-600">
                              <span className="font-bold">Observação atual: </span>
                              {work.reviewNote}
                            </p>
                          )}
                        </div>
                      </div>

                      <textarea
                        value={reviewNotes[work.id] || ""}
                        onChange={(event) =>
                          handleNoteChange(work.id, event.target.value)
                        }
                        maxLength={1000}
                        rows={3}
                        className="mt-5 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                        placeholder="Observação da revisão manual..."
                      />

                      <div className="mt-4 flex flex-wrap justify-end gap-3">
                        <PrimaryButton
                          variant="light"
                          icon={Undo2}
                          loading={actionWorkId === work.id}
                          onClick={() => runReviewAction(work, "pending")}
                        >
                          Pendente
                        </PrimaryButton>

                        <PrimaryButton
                          variant="danger"
                          icon={ThumbsDown}
                          loading={actionWorkId === work.id}
                          onClick={() => runReviewAction(work, "reject")}
                        >
                          Rejeitar
                        </PrimaryButton>

                        <PrimaryButton
                          icon={CheckCircle2}
                          loading={actionWorkId === work.id}
                          onClick={() => runReviewAction(work, "confirm")}
                        >
                          Confirmar
                        </PrimaryButton>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          )}
        </>
      )}
    </div>
  );
}