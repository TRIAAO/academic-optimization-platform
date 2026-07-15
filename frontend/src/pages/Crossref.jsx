import { useEffect, useMemo, useState } from "react";
import { CheckCircle2, FileSearch, RefreshCw, Search } from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { crossrefService } from "../services/crossrefService";
import { openAlexService } from "../services/openAlexService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

function getMatchVariant(status, isDoiValid) {
  const normalized = String(status || "").toUpperCase();

  if (isDoiValid === true || normalized.includes("MATCH")) return "green";
  if (isDoiValid === false || normalized.includes("MISMATCH")) return "red";

  return "amber";
}

export default function Crossref() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [works, setWorks] = useState([]);
  const [validations, setValidations] = useState([]);
  const [selectedValidation, setSelectedValidation] = useState(null);
  const [query, setQuery] = useState("");
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingData, setLoadingData] = useState(false);
  const [actionWorkId, setActionWorkId] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const selectedResearcher = useMemo(() => {
    return researchers.find((researcher) => researcher.id === selectedResearcherId);
  }, [researchers, selectedResearcherId]);

  const worksWithDoi = useMemo(() => {
    return works.filter((work) => Boolean(work.doi));
  }, [works]);

  const filteredWorks = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) return worksWithDoi;

    return worksWithDoi.filter((work) => {
      const searchable = [
        work.title,
        work.doi,
        work.sourceName,
        work.publicationYear,
        work.reviewStatus
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchable.includes(normalizedQuery);
    });
  }, [worksWithDoi, query]);

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

  async function loadCrossrefData(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingData(true);
    setError("");
    setSuccess("");
    setSelectedValidation(null);

    try {
      const [worksResult, validationsResult] = await Promise.allSettled([
        openAlexService.findWorksByResearcher(researcherId),
        crossrefService.findValidationsByResearcher(researcherId)
      ]);

      setWorks(worksResult.status === "fulfilled" ? worksResult.value : []);
      setValidations(
        validationsResult.status === "fulfilled" ? validationsResult.value : []
      );
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar dados Crossref.");
    } finally {
      setLoadingData(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      loadCrossrefData(selectedResearcherId);
    }
  }, [selectedResearcherId]);

  async function handleValidateWork(work) {
    setActionWorkId(work.id);
    setError("");
    setSuccess("");

    try {
      const validation = await crossrefService.validateOpenAlexWork(work.id);
      setSelectedValidation(validation);

      const updatedValidations =
        await crossrefService.findValidationsByResearcher(selectedResearcherId);
      setValidations(updatedValidations);

      setSuccess("DOI e metadados validados via Crossref.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível validar DOI via Crossref.");
    } finally {
      setActionWorkId("");
    }
  }

  async function handleLoadLatestValidation(work) {
    setActionWorkId(work.id);
    setError("");
    setSuccess("");

    try {
      const validation =
        await crossrefService.findLatestValidationByOpenAlexWork(work.id);
      setSelectedValidation(validation);
      setSuccess("Última validação carregada.");
    } catch (apiError) {
      setError(
        apiError?.message || "Não foi possível carregar a última validação."
      );
    } finally {
      setActionWorkId("");
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Validação bibliográfica"
        title="Crossref / DOI"
        description="Validação de DOI e metadados das obras importadas do OpenAlex usando Crossref."
        actions={
          <PrimaryButton
            variant="light"
            icon={RefreshCw}
            disabled={!selectedResearcherId}
            onClick={() => loadCrossrefData()}
          >
            Atualizar
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
        <LoadingState message="Carregando pesquisadores para Crossref..." />
      )}

      {!loadingResearchers && (
        <>
          <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <label className="text-sm font-bold text-slate-700">
              Pesquisador
            </label>

            <select
              value={selectedResearcherId}
              onChange={(event) => {
                setSelectedResearcherId(event.target.value);
                setWorks([]);
                setValidations([]);
                setSelectedValidation(null);
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
                    Obras com DOI
                  </p>
                  <p className="mt-2 font-black text-slate-950">
                    {formatNumber(worksWithDoi.length)}
                  </p>
                </div>

                <div className="rounded-2xl bg-slate-50 p-4">
                  <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                    Validações
                  </p>
                  <p className="mt-2 font-black text-slate-950">
                    {formatNumber(validations.length)}
                  </p>
                </div>
              </div>
            )}
          </section>

          {!selectedResearcherId && (
            <EmptyState
              icon={FileSearch}
              title="Selecione um pesquisador"
              description="Escolha um pesquisador com obras OpenAlex importadas para validar DOI e metadados."
            />
          )}

          {selectedResearcherId && loadingData && (
            <LoadingState message="Carregando obras e validações Crossref..." />
          )}

          {selectedResearcherId && !loadingData && (
            <section className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-5 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                  <div>
                    <h3 className="font-black text-slate-950">
                      Obras com DOI
                    </h3>
                    <p className="mt-1 text-sm text-slate-500">
                      Apenas obras com DOI podem ser validadas via Crossref.
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
                    placeholder="Buscar por título, DOI, fonte ou ano..."
                  />
                </div>

                {filteredWorks.length === 0 ? (
                  <EmptyState
                    icon={FileSearch}
                    title="Nenhuma obra com DOI"
                    description="Importe obras pelo OpenAlex e confirme se elas possuem DOI."
                  />
                ) : (
                  <div className="space-y-3">
                    {filteredWorks.map((work) => (
                      <div
                        key={work.id}
                        className="rounded-3xl border border-slate-200 bg-slate-50 p-5"
                      >
                        <p className="font-black text-slate-950">
                          {work.title || "Obra sem título"}
                        </p>

                        <p className="mt-2 text-sm text-slate-500">
                          {work.sourceName || "Fonte não informada"} ·{" "}
                          {work.publicationYear || "Ano não informado"}
                        </p>

                        <p className="mt-2 break-all text-xs font-semibold text-slate-600">
                          DOI: {work.doi}
                        </p>

                        <div className="mt-4 flex flex-wrap justify-end gap-3">
                          <PrimaryButton
                            variant="light"
                            icon={FileSearch}
                            loading={actionWorkId === work.id}
                            onClick={() => handleLoadLatestValidation(work)}
                          >
                            Última validação
                          </PrimaryButton>

                          <PrimaryButton
                            icon={CheckCircle2}
                            loading={actionWorkId === work.id}
                            onClick={() => handleValidateWork(work)}
                          >
                            Validar DOI
                          </PrimaryButton>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="space-y-6">
                <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                  <h3 className="font-black text-slate-950">
                    Resultado da validação
                  </h3>

                  {!selectedValidation ? (
                    <p className="mt-3 text-sm text-slate-500">
                      Nenhuma validação selecionada ainda.
                    </p>
                  ) : (
                    <div className="mt-5 space-y-4">
                      <Badge
                        variant={getMatchVariant(
                          selectedValidation.matchStatus,
                          selectedValidation.isDoiValid
                        )}
                      >
                        {selectedValidation.matchStatus || "Status não informado"}
                      </Badge>

                      <div className="rounded-2xl bg-slate-50 p-4">
                        <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                          DOI enviado
                        </p>
                        <p className="mt-2 break-all font-bold text-slate-950">
                          {selectedValidation.doiSubmitted || "Não informado"}
                        </p>
                      </div>

                      <div className="rounded-2xl bg-slate-50 p-4">
                        <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                          DOI encontrado
                        </p>
                        <p className="mt-2 break-all font-bold text-slate-950">
                          {selectedValidation.doiFound || "Não informado"}
                        </p>
                      </div>

                      <div className="rounded-2xl bg-slate-50 p-4">
                        <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                          Título encontrado
                        </p>
                        <p className="mt-2 font-bold text-slate-950">
                          {selectedValidation.titleFound || "Não informado"}
                        </p>
                      </div>

                      <div className="rounded-2xl bg-slate-50 p-4">
                        <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                          Mensagem
                        </p>
                        <p className="mt-2 text-sm leading-6 text-slate-700">
                          {selectedValidation.message || "Sem mensagem"}
                        </p>
                      </div>

                      <p className="text-xs text-slate-500">
                        Validado em{" "}
                        {formatDateTime(
                          selectedValidation.validatedAt ||
                            selectedValidation.createdAt
                        )}
                      </p>
                    </div>
                  )}
                </div>

                <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                  <div className="mb-4 flex items-center justify-between">
                    <h3 className="font-black text-slate-950">
                      Histórico Crossref
                    </h3>

                    <Badge variant="slate">
                      {formatNumber(validations.length)} registros
                    </Badge>
                  </div>

                  {validations.length === 0 ? (
                    <p className="text-sm text-slate-500">
                      Nenhuma validação encontrada para este pesquisador.
                    </p>
                  ) : (
                    <div className="space-y-3">
                      {validations.map((validation) => (
                        <button
                          key={validation.id}
                          type="button"
                          onClick={() => setSelectedValidation(validation)}
                          className="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-left transition hover:border-blue-300 hover:bg-blue-50"
                        >
                          <div className="flex items-start justify-between gap-3">
                            <div>
                              <p className="font-bold text-slate-950">
                                {validation.titleSubmitted ||
                                  validation.titleFound ||
                                  "Validação Crossref"}
                              </p>

                              <p className="mt-1 break-all text-xs text-slate-500">
                                DOI:{" "}
                                {validation.doiSubmitted ||
                                  validation.doiFound ||
                                  "Não informado"}
                              </p>
                            </div>

                            <Badge
                              variant={getMatchVariant(
                                validation.matchStatus,
                                validation.isDoiValid
                              )}
                            >
                              {validation.matchStatus || "N/I"}
                            </Badge>
                          </div>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}