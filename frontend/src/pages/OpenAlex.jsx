import { useEffect, useMemo, useState } from "react";
import {
  BookOpenCheck,
  Database,
  RefreshCw,
  Search,
  Trash2,
  UserCheck,
  Users
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { openAlexService } from "../services/openAlexService";
import { researcherService } from "../services/researcherService";
import { formatNumber } from "../utils/formatters";

function getStatusVariant(status) {
  const normalized = String(status || "").toUpperCase();

  if (normalized === "CONFIRMED") return "green";
  if (normalized === "REJECTED") return "red";
  if (normalized === "PENDING_REVIEW") return "amber";

  return "slate";
}

function friendlyOpenAlexMessage(apiError) {
  const message = apiError?.message || "";

  if (
    message.toLowerCase().includes("autor não encontrado") ||
    message.toLowerCase().includes("openalex")
  ) {
    return "O OpenAlex não encontrou autor verificado para o ORCID informado. Você ainda pode usar os candidatos por nome ou revisar as obras já importadas.";
  }

  return message || "Não foi possível concluir a operação no OpenAlex.";
}

export default function OpenAlex() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [verifiedAuthor, setVerifiedAuthor] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [works, setWorks] = useState([]);
  const [lastImport, setLastImport] = useState(null);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingAction, setLoadingAction] = useState(false);
  const [query, setQuery] = useState("");
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
        work.openAlexId,
        work.sourceName,
        work.workType,
        work.publicationYear,
        work.reviewStatus
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

  async function loadExistingWorks(researcherId = selectedResearcherId) {
    if (!researcherId) return [];

    try {
      const existingWorks = await openAlexService.findWorksByResearcher(researcherId);
      setWorks(existingWorks);
      return existingWorks;
    } catch {
      return [];
    }
  }

  async function loadOpenAlexData(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingAction(true);
    setError("");
    setSuccess("");

    const warnings = [];

    try {
      const authorResult = await Promise.allSettled([
        openAlexService.findVerifiedAuthor(researcherId),
        openAlexService.findAuthorCandidates(researcherId),
        openAlexService.findWorksByResearcher(researcherId)
      ]);

      if (authorResult[0].status === "fulfilled") {
        setVerifiedAuthor(authorResult[0].value);
      } else {
        setVerifiedAuthor(null);
        warnings.push("autor verificado não encontrado pelo ORCID");
      }

      if (authorResult[1].status === "fulfilled") {
        setCandidates(authorResult[1].value);
      } else {
        setCandidates([]);
        warnings.push("candidatos não carregados");
      }

      if (authorResult[2].status === "fulfilled") {
        setWorks(authorResult[2].value);
      } else {
        setWorks([]);
      }

      if (warnings.length > 0) {
        setError(
          `OpenAlex carregou parcialmente: ${warnings.join(", ")}. Você pode usar candidatos por nome ou obras já importadas.`
        );
      } else {
        setSuccess("Dados OpenAlex carregados.");
      }
    } finally {
      setLoadingAction(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  async function handleSearchWorks() {
    if (!selectedResearcherId) return;

    setLoadingAction(true);
    setError("");
    setSuccess("");

    try {
      const data = await openAlexService.searchWorks(selectedResearcherId);
      setLastImport(data);
      setWorks(data?.works || []);
      setSuccess(
        `Busca concluída. Encontradas ${data?.totalFound || 0} obras para análise.`
      );
    } catch (apiError) {
      await loadExistingWorks();
      setError(friendlyOpenAlexMessage(apiError));
    } finally {
      setLoadingAction(false);
    }
  }

  async function handleImportWorks() {
    if (!selectedResearcherId) return;

    setLoadingAction(true);
    setError("");
    setSuccess("");

    try {
      const data = await openAlexService.importWorks(selectedResearcherId);
      setLastImport(data);

      if (Array.isArray(data?.works)) {
        setWorks(data.works);
      } else {
        await loadExistingWorks();
      }

      setSuccess(
        `Importação concluída. Importadas ${data?.totalImported || 0} obras.`
      );
    } catch (apiError) {
      const existingWorks = await loadExistingWorks();

      setError(
        `${friendlyOpenAlexMessage(apiError)} ${
          existingWorks.length > 0
            ? `Foram mantidas ${existingWorks.length} obras já existentes para revisão.`
            : ""
        }`
      );
    } finally {
      setLoadingAction(false);
    }
  }

  async function handleImportByCandidate(candidate) {
    if (!selectedResearcherId || !candidate?.openAlexAuthorShortId) return;

    setLoadingAction(true);
    setError("");
    setSuccess("");

    try {
      const data = await openAlexService.importWorksByApprovedAuthor(
        selectedResearcherId,
        candidate.openAlexAuthorShortId
      );

      setLastImport(data);

      if (Array.isArray(data?.works)) {
        setWorks(data.works);
      } else {
        await loadExistingWorks();
      }

      setSuccess(`Obras importadas pelo autor ${candidate.displayName}.`);
    } catch (apiError) {
      await loadExistingWorks();
      setError(apiError?.message || "Não foi possível importar pelo autor escolhido.");
    } finally {
      setLoadingAction(false);
    }
  }

  async function handleCleanupWorks() {
    if (!selectedResearcherId) return;

    const confirmed = window.confirm(
      "Deseja remover as obras OpenAlex deste pesquisador? Esta ação deve ser usada apenas para limpar uma importação incorreta."
    );

    if (!confirmed) return;

    setLoadingAction(true);
    setError("");
    setSuccess("");

    try {
      await openAlexService.deleteWorksByResearcher(selectedResearcherId);
      setWorks([]);
      setSuccess("Obras OpenAlex removidas para este pesquisador.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível limpar obras OpenAlex.");
    } finally {
      setLoadingAction(false);
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Integração científica"
        title="OpenAlex"
        description="Busca, validação por ORCID, candidatos de autor e importação de obras acadêmicas vindas do OpenAlex."
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
        <LoadingState message="Carregando pesquisadores para OpenAlex..." />
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
                    setVerifiedAuthor(null);
                    setCandidates([]);
                    setWorks([]);
                    setLastImport(null);
                    setError("");
                    setSuccess("");
                  }}
                  className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                >
                  <option value="">Selecione um pesquisador</option>

                  {researchers.map((researcher) => (
                    <option key={researcher.id} value={researcher.id}>
                      {researcher.fullName} — {researcher.orcidId || "sem ORCID"}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex flex-wrap gap-3">
                <PrimaryButton
                  variant="light"
                  icon={Database}
                  loading={loadingAction}
                  disabled={!selectedResearcherId}
                  onClick={() => loadOpenAlexData()}
                >
                  Carregar
                </PrimaryButton>

                <PrimaryButton
                  variant="light"
                  icon={Search}
                  loading={loadingAction}
                  disabled={!selectedResearcherId}
                  onClick={handleSearchWorks}
                >
                  Buscar obras
                </PrimaryButton>

                <PrimaryButton
                  icon={BookOpenCheck}
                  loading={loadingAction}
                  disabled={!selectedResearcherId}
                  onClick={handleImportWorks}
                >
                  Importar obras
                </PrimaryButton>

                <PrimaryButton
                  variant="danger"
                  icon={Trash2}
                  loading={loadingAction}
                  disabled={!selectedResearcherId || works.length === 0}
                  onClick={handleCleanupWorks}
                >
                  Limpar
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
                    ORCID
                  </p>
                  <p className="mt-2 font-black text-slate-950">
                    {selectedResearcher.orcidId || "Não informado"}
                  </p>
                </div>

                <div className="rounded-2xl bg-slate-50 p-4">
                  <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                    Obras carregadas
                  </p>
                  <p className="mt-2 font-black text-slate-950">
                    {formatNumber(works.length)}
                  </p>
                </div>
              </div>
            )}
          </section>

          {!selectedResearcherId && (
            <EmptyState
              icon={Users}
              title="Selecione um pesquisador"
              description="Escolha um pesquisador para buscar autor, candidatos e obras no OpenAlex."
            />
          )}

          {selectedResearcherId && (
            <section className="grid gap-6 xl:grid-cols-2">
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="font-black text-slate-950">Autor verificado</h3>

                {!verifiedAuthor ? (
                  <p className="mt-3 text-sm text-slate-500">
                    Nenhum autor verificado carregado. Isso pode acontecer quando
                    o ORCID do pesquisador não possui registro correspondente no
                    OpenAlex.
                  </p>
                ) : (
                  <div className="mt-4 rounded-2xl bg-slate-50 p-4">
                    <p className="font-black text-slate-950">
                      {verifiedAuthor.displayName || "Autor OpenAlex"}
                    </p>
                    <p className="mt-1 text-sm text-slate-500">
                      ORCID: {verifiedAuthor.orcid || "Não informado"}
                    </p>
                    <div className="mt-3 flex flex-wrap gap-2">
                      <Badge variant="blue">
                        {formatNumber(verifiedAuthor.worksCount)} obras
                      </Badge>
                      <Badge variant="green">
                        {formatNumber(verifiedAuthor.citedByCount)} citações
                      </Badge>
                    </div>
                  </div>
                )}
              </div>

              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex items-center justify-between">
                  <h3 className="font-black text-slate-950">
                    Candidatos OpenAlex
                  </h3>
                  <Badge variant="blue">{candidates.length} candidatos</Badge>
                </div>

                {candidates.length === 0 ? (
                  <p className="mt-3 text-sm text-slate-500">
                    Nenhum candidato carregado. Clique em carregar para buscar
                    candidatos por nome.
                  </p>
                ) : (
                  <div className="mt-4 space-y-3">
                    {candidates.map((candidate) => (
                      <div
                        key={candidate.openAlexAuthorId}
                        className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                      >
                        <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
                          <div>
                            <p className="font-black text-slate-950">
                              {candidate.displayName}
                            </p>
                            <p className="mt-1 text-sm text-slate-500">
                              {candidate.lastKnownInstitution ||
                                "Instituição não informada"}
                            </p>
                            <div className="mt-3 flex flex-wrap gap-2">
                              <Badge variant="slate">
                                {candidate.lastKnownCountryCode || "País n/i"}
                              </Badge>
                              <Badge variant="blue">
                                {formatNumber(candidate.worksCount)} obras
                              </Badge>
                              <Badge variant="green">
                                {formatNumber(candidate.citedByCount)} citações
                              </Badge>
                            </div>
                          </div>

                          <PrimaryButton
                            variant="light"
                            icon={UserCheck}
                            loading={loadingAction}
                            onClick={() => handleImportByCandidate(candidate)}
                          >
                            Usar autor
                          </PrimaryButton>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </section>
          )}

          {selectedResearcherId && (
            <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
              <div className="mb-5 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div>
                  <h3 className="font-black text-slate-950">Obras OpenAlex</h3>
                  <p className="mt-1 text-sm text-slate-500">
                    Obras encontradas/importadas para revisão institucional.
                  </p>
                </div>

                <Badge variant="blue">{formatNumber(filteredWorks.length)} obras</Badge>
              </div>

              {lastImport && (
                <div className="mb-5 grid gap-4 md:grid-cols-3">
                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                      Busca
                    </p>
                    <p className="mt-2 font-black text-slate-950">
                      {lastImport.searchName || "Não informada"}
                    </p>
                  </div>

                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                      Encontradas
                    </p>
                    <p className="mt-2 font-black text-slate-950">
                      {formatNumber(lastImport.totalFound)}
                    </p>
                  </div>

                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
                      Importadas
                    </p>
                    <p className="mt-2 font-black text-slate-950">
                      {formatNumber(lastImport.totalImported)}
                    </p>
                  </div>
                </div>
              )}

              <div className="mb-5 flex items-center rounded-2xl border border-slate-200 bg-slate-50 px-4">
                <Search className="h-5 w-5 text-slate-400" />
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  className="w-full bg-transparent px-3 py-3 text-sm outline-none"
                  placeholder="Buscar por título, DOI, fonte, ano ou status..."
                />
              </div>

              {filteredWorks.length === 0 ? (
                <EmptyState
                  icon={BookOpenCheck}
                  title="Nenhuma obra OpenAlex carregada"
                  description="Use buscar ou importar obras para preencher a revisão manual."
                />
              ) : (
                <div className="space-y-3">
                  {filteredWorks.map((work) => (
                    <div
                      key={work.id}
                      className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                    >
                      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                        <div>
                          <p className="font-black text-slate-950">
                            {work.title || "Obra sem título"}
                          </p>

                          <p className="mt-2 text-sm text-slate-500">
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
                            {work.isOpenAccess && (
                              <Badge variant="green">
                                {work.openAccessStatus || "Open Access"}
                              </Badge>
                            )}
                          </div>
                        </div>

                        {work.openAlexUrl || work.doiUrl ? (
                          <a
                            href={work.openAlexUrl || work.doiUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="text-sm font-bold text-blue-700 hover:text-blue-900"
                          >
                            Abrir fonte
                          </a>
                        ) : null}
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