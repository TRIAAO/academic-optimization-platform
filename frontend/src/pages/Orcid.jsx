import { useEffect, useMemo, useState } from "react";
import {
  BookOpenCheck,
  BriefcaseBusiness,
  ExternalLink,
  Globe2,
  GraduationCap,
  Link2,
  RefreshCw,
  Search,
  UserRoundSearch
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import MetricCard from "../components/ui/MetricCard";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { orcidService } from "../services/orcidService";
import { researcherService } from "../services/researcherService";
import { formatDateTime } from "../utils/formatters";
import { formatOrcidInput, isValidOrcid } from "../utils/orcid";

function isPublicHttpUrl(value) {
  try {
    const url = new URL(value);
    return url.protocol === "https:" || url.protocol === "http:";
  } catch {
    return false;
  }
}

function AffiliationList({ title, items, emptyMessage }) {
  return (
    <div>
      <h4 className="font-black text-slate-950">{title}</h4>
      {items?.length ? (
        <div className="mt-3 space-y-3">
          {items.map((item, index) => (
            <article
              key={`${item.organizationName || title}-${item.startDate || index}`}
              className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
            >
              <p className="font-bold text-slate-950">
                {item.organizationName || "Organização não informada"}
              </p>
              {(item.roleTitle || item.departmentName) && (
                <p className="mt-1 text-sm text-slate-600">
                  {[item.roleTitle, item.departmentName].filter(Boolean).join(" · ")}
                </p>
              )}
              <p className="mt-2 text-xs font-semibold text-slate-500">
                {[item.startDate, item.endDate].filter(Boolean).join(" — ") || "Período não informado"}
                {item.country ? ` · ${item.country}` : ""}
              </p>
            </article>
          ))}
        </div>
      ) : (
        <p className="mt-2 text-sm text-slate-500">{emptyMessage}</p>
      )}
    </div>
  );
}

function OrcidSummaryCard({ summary }) {
  const displayName = summary.displayName || summary.researcherName || "Nome não publicado";
  const publicWebsites = (summary.websites || []).filter((website) => isPublicHttpUrl(website.url));

  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="green">Autor encontrado</Badge>
            <Badge variant="slate">{summary.orcidId}</Badge>
          </div>
          <h3 className="mt-3 text-2xl font-black text-slate-950">{displayName}</h3>
          <p className="mt-1 text-sm text-slate-500">
            Dados públicos recuperados diretamente do registo ORCID.
          </p>
        </div>

        {summary.orcidUrl && (
          <a
            href={summary.orcidUrl}
            target="_blank"
            rel="noreferrer"
            className="inline-flex shrink-0 items-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-sm font-bold text-slate-700 hover:border-blue-300 hover:text-blue-700"
          >
            Abrir ORCID <ExternalLink className="h-4 w-4" />
          </a>
        )}
      </div>

      <div className="mt-5 grid gap-3 sm:grid-cols-3">
        <MetricCard
          icon={BookOpenCheck}
          title="Obras públicas"
          value={summary.worksCount || 0}
          description="registadas no ORCID"
          tone="blue"
        />
        <MetricCard
          icon={BriefcaseBusiness}
          title="Vínculos"
          value={summary.employments?.length || 0}
          description="empregos públicos"
          tone="emerald"
        />
        <MetricCard
          icon={GraduationCap}
          title="Formação"
          value={summary.educations?.length || 0}
          description="registos públicos"
          tone="violet"
        />
      </div>

      {summary.biography && (
        <div className="mt-6 border-t border-slate-200 pt-5">
          <h4 className="font-black text-slate-950">Biografia</h4>
          <p className="mt-2 whitespace-pre-wrap text-sm leading-7 text-slate-600">
            {summary.biography}
          </p>
        </div>
      )}

      <div className="mt-6 grid gap-6 border-t border-slate-200 pt-5 lg:grid-cols-2">
        <AffiliationList
          title="Vínculos profissionais"
          items={summary.employments}
          emptyMessage="Nenhum vínculo profissional público informado."
        />
        <AffiliationList
          title="Formação acadêmica"
          items={summary.educations}
          emptyMessage="Nenhuma formação pública informada."
        />
      </div>

      {(summary.keywords?.length > 0 || publicWebsites.length > 0) && (
        <div className="mt-6 grid gap-6 border-t border-slate-200 pt-5 lg:grid-cols-2">
          <div>
            <h4 className="font-black text-slate-950">Palavras-chave</h4>
            <div className="mt-3 flex flex-wrap gap-2">
              {summary.keywords?.length ? summary.keywords.map((keyword) => (
                <Badge key={keyword} variant="blue">{keyword}</Badge>
              )) : <p className="text-sm text-slate-500">Nenhuma palavra-chave pública.</p>}
            </div>
          </div>
          <div>
            <h4 className="font-black text-slate-950">Sites públicos</h4>
            <div className="mt-3 space-y-2">
              {publicWebsites.length ? publicWebsites.map((website, index) => (
                <a
                  key={`${website.url}-${index}`}
                  href={website.url}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-2 text-sm font-bold text-blue-700 hover:text-blue-900"
                >
                  <Globe2 className="h-4 w-4" />
                  {website.name || website.url}
                </a>
              )) : <p className="text-sm text-slate-500">Nenhum site público.</p>}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default function Orcid() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [manualOrcidId, setManualOrcidId] = useState("");
  const [summary, setSummary] = useState(null);
  const [works, setWorks] = useState([]);
  const [logs, setLogs] = useState([]);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingAction, setLoadingAction] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

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

  useEffect(() => {
    loadResearchers();
  }, []);

  const selectedResearcher = useMemo(() => {
    return researchers.find((researcher) => researcher.id === selectedResearcherId);
  }, [researchers, selectedResearcherId]);

  const validManualOrcid = useMemo(
    () => isValidOrcid(manualOrcidId),
    [manualOrcidId]
  );

  async function loadResearcherOrcidData() {
    if (!selectedResearcherId) return;

    setLoadingAction(true);
    setError("");
    setSuccess("");

    try {
      const [summaryData, worksData, logsData] = await Promise.allSettled([
        orcidService.findSummaryByResearcher(selectedResearcherId),
        orcidService.findWorksByResearcher(selectedResearcherId),
        orcidService.findImportLogsByResearcher(selectedResearcherId)
      ]);

      setSummary(summaryData.status === "fulfilled" ? summaryData.value : null);
      setWorks(worksData.status === "fulfilled" ? worksData.value : []);
      setLogs(logsData.status === "fulfilled" ? logsData.value : []);

      setSuccess("Dados ORCID carregados.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar dados ORCID.");
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
      await orcidService.importWorks(selectedResearcherId);
      const worksData = await orcidService.findWorksByResearcher(selectedResearcherId);
      const logsData = await orcidService.findImportLogsByResearcher(selectedResearcherId);

      setWorks(worksData);
      setLogs(logsData);
      setSuccess("Obras importadas do ORCID com sucesso.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível importar obras do ORCID.");
    } finally {
      setLoadingAction(false);
    }
  }

  async function handleSyncProfile() {
    if (!selectedResearcherId) return;

    setLoadingAction(true);
    setError("");
    setSuccess("");

    try {
      await orcidService.syncProfile(selectedResearcherId);
      setSuccess("Perfil acadêmico sincronizado com ORCID.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível sincronizar perfil.");
    } finally {
      setLoadingAction(false);
    }
  }

  async function handleSearchManualOrcid() {
    if (!validManualOrcid) {
      setError("Informe um ORCID válido com os 16 caracteres.");
      return;
    }

    setLoadingAction(true);
    setError("");
    setSuccess("");

    try {
      const summaryData = await orcidService.findSummaryByOrcidId(
        manualOrcidId.trim()
      );

      setSummary(summaryData);
      setManualOrcidId(summaryData.orcidId || manualOrcidId);
      setSuccess(`Autor encontrado no ORCID: ${summaryData.displayName || summaryData.orcidId}.`);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível consultar o ORCID informado.");
    } finally {
      setLoadingAction(false);
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Integração científica"
        title="ORCID"
        description="Importação de obras, resumo público e sincronização controlada com o perfil acadêmico do pesquisador."
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
        <LoadingState message="Carregando pesquisadores para integração ORCID..." />
      )}

      {!loadingResearchers && (
        <section className="grid gap-6 xl:grid-cols-[0.8fr_1.2fr]">
          <div className="space-y-6">
            <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
              <h3 className="font-black text-slate-950">
                Pesquisador vinculado
              </h3>

              <p className="mt-2 text-sm leading-6 text-slate-500">
                Selecione um pesquisador já cadastrado para importar obras e
                sincronizar o perfil.
              </p>

              <select
                value={selectedResearcherId}
                onChange={(event) => {
                  setSelectedResearcherId(event.target.value);
                  setSummary(null);
                  setWorks([]);
                  setLogs([]);
                }}
                className="mt-5 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
              >
                <option value="">Selecione um pesquisador</option>

                {researchers.map((researcher) => (
                  <option key={researcher.id} value={researcher.id}>
                    {researcher.fullName} — {researcher.orcidId || "sem ORCID"}
                  </option>
                ))}
              </select>

              {selectedResearcher && (
                <div className="mt-5 rounded-2xl bg-slate-50 p-4">
                  <p className="font-bold text-slate-950">
                    {selectedResearcher.fullName}
                  </p>
                  <p className="mt-1 text-sm text-slate-500">
                    {selectedResearcher.email}
                  </p>

                  <div className="mt-3">
                    {selectedResearcher.orcidId ? (
                      <Badge variant="green">{selectedResearcher.orcidId}</Badge>
                    ) : (
                      <Badge variant="amber">Sem ORCID no cadastro</Badge>
                    )}
                  </div>
                </div>
              )}

              <div className="mt-5 flex flex-wrap gap-3">
                <PrimaryButton
                  variant="light"
                  icon={Search}
                  loading={loadingAction}
                  disabled={!selectedResearcherId}
                  onClick={loadResearcherOrcidData}
                >
                  Carregar dados
                </PrimaryButton>

                <PrimaryButton
                  variant="light"
                  icon={BookOpenCheck}
                  loading={loadingAction}
                  disabled={!selectedResearcherId || !selectedResearcher?.orcidId}
                  onClick={handleImportWorks}
                >
                  Importar obras
                </PrimaryButton>

                <PrimaryButton
                  icon={Link2}
                  loading={loadingAction}
                  disabled={!selectedResearcherId || !selectedResearcher?.orcidId}
                  onClick={handleSyncProfile}
                >
                  Sincronizar
                </PrimaryButton>
              </div>
            </div>

            <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
              <h3 className="font-black text-slate-950">Buscar autor pelo ORCID</h3>

              <p className="mt-2 text-sm leading-6 text-slate-500">
                Digite somente os 16 caracteres. Os hífens são adicionados automaticamente e a consulta não altera nenhum cadastro.
              </p>

              <form
                className="mt-5"
                onSubmit={(event) => {
                  event.preventDefault();
                  handleSearchManualOrcid();
                }}
              >
                <label htmlFor="manual-orcid" className="text-xs font-black uppercase tracking-wide text-slate-600">
                  Número ORCID
                </label>
                <div className="mt-2 flex flex-col gap-3 sm:flex-row">
                  <input
                    id="manual-orcid"
                    value={manualOrcidId}
                    onChange={(event) => {
                      setManualOrcidId(formatOrcidInput(event.target.value));
                      setError("");
                    }}
                    inputMode="text"
                    autoComplete="off"
                    maxLength={64}
                    className="w-full rounded-2xl border border-slate-200 px-4 py-3 font-mono text-sm tracking-wide outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                    placeholder="0000-0000-0000-0000"
                  />

                  <PrimaryButton
                    icon={Search}
                    loading={loadingAction}
                    disabled={!validManualOrcid}
                    type="submit"
                  >
                    Buscar autor
                  </PrimaryButton>
                </div>
                <p className={`mt-2 text-xs ${manualOrcidId && !validManualOrcid ? "text-amber-700" : "text-slate-500"}`}>
                  {manualOrcidId && !validManualOrcid
                    ? "Continue digitando ou confira o dígito verificador."
                    : "Também é possível colar o link completo do perfil ORCID."}
                </p>
              </form>
            </div>
          </div>

          <div className="space-y-6">
            {!selectedResearcherId && !summary && (
              <EmptyState
                icon={UserRoundSearch}
                title="Selecione um pesquisador"
                description="Escolha um pesquisador com ORCID cadastrado ou consulte manualmente um ORCID público."
              />
            )}

            {summary && <OrcidSummaryCard summary={summary} />}

            {selectedResearcherId && (
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-4 flex items-center justify-between">
                  <h3 className="font-black text-slate-950">Obras ORCID</h3>
                  <Badge variant="blue">{works.length} obras</Badge>
                </div>

                {works.length === 0 ? (
                  <p className="text-sm text-slate-500">
                    Nenhuma obra carregada para este pesquisador.
                  </p>
                ) : (
                  <div className="space-y-3">
                    {works.map((work, index) => (
                      <div
                        key={work.id || work.putCode || index}
                        className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                      >
                        <p className="font-bold text-slate-950">
                          {work.title || work.workTitle || "Obra sem título"}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">
                          {work.publicationYear || work.year || "Ano não informado"}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {selectedResearcherId && (
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-4 flex items-center justify-between">
                  <h3 className="font-black text-slate-950">Logs de importação</h3>
                  <Badge variant="slate">{logs.length} logs</Badge>
                </div>

                {logs.length === 0 ? (
                  <p className="text-sm text-slate-500">
                    Nenhum log de importação encontrado.
                  </p>
                ) : (
                  <div className="space-y-3">
                    {logs.map((log, index) => (
                      <div
                        key={log.id || index}
                        className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                      >
                        <p className="font-bold text-slate-950">
                          {log.status || log.result || "Log ORCID"}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">
                          {formatDateTime(log.createdAt || log.importedAt)}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </section>
      )}

      <section className="rounded-3xl border border-amber-200 bg-amber-50 p-6">
        <h3 className="font-black text-amber-950">Importante</h3>
        <p className="mt-2 text-sm leading-7 text-amber-900">
          ORCID, OpenAlex e Crossref são fontes permitidas para consulta e
          organização acadêmica. Google Acadêmico permanece somente como
          checklist manual, sem automação, scraping ou alteração direta.
        </p>
      </section>
    </div>
  );
}
