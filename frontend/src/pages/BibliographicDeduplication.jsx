import {
  AlertTriangle,
  CheckCircle2,
  CopyCheck,
  ExternalLink,
  RefreshCw,
  ScanSearch,
  XCircle
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { bibliographicDeduplicationService } from "../services/bibliographicDeduplicationService";
import { researcherService } from "../services/researcherService";

const STATUS_LABELS = {
  PENDING: "Pendente",
  CONFIRMED: "Mesma obra confirmada",
  REJECTED: "Candidato rejeitado"
};

const STATUS_STYLES = {
  PENDING: "border-amber-200 bg-amber-50 text-amber-900",
  CONFIRMED: "border-emerald-200 bg-emerald-50 text-emerald-800",
  REJECTED: "border-slate-200 bg-slate-100 text-slate-700"
};

function SummaryCard({ label, value, tone = "slate" }) {
  const styles = {
    slate: "border-slate-200 bg-white",
    amber: "border-amber-200 bg-amber-50",
    emerald: "border-emerald-200 bg-emerald-50",
    rose: "border-rose-200 bg-rose-50"
  };

  return (
    <div className={`rounded-2xl border p-4 ${styles[tone] || styles.slate}`}>
      <p className="text-xs font-black uppercase tracking-wide text-slate-500">
        {label}
      </p>
      <p className="mt-2 text-xl font-black text-slate-950">{value}</p>
    </div>
  );
}

function WorkCard({ work }) {
  const externalUrl =
    work?.source === "OPENALEX" && work?.externalId
      ? `https://openalex.org/${String(work.externalId).replace("https://openalex.org/", "")}`
      : null;

  return (
    <div className="min-w-0 rounded-2xl border border-slate-200 bg-slate-50 p-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <span className="rounded-full bg-blue-100 px-2.5 py-1 text-[11px] font-black text-blue-800">
          {work?.source || "Fonte"}
        </span>
        {work?.publicationYear && (
          <span className="text-xs font-bold text-slate-500">
            {work.publicationYear}
          </span>
        )}
      </div>

      <h4 className="mt-3 break-words text-sm font-black leading-6 text-slate-950">
        {work?.title || "Título não informado"}
      </h4>

      <div className="mt-3 space-y-1 text-xs leading-5 text-slate-600">
        <p>
          <strong>DOI:</strong> {work?.doi || "não informado"}
        </p>
        <p>
          <strong>Veículo:</strong> {work?.venue || "não informado"}
        </p>
        {work?.citations !== null && work?.citations !== undefined && (
          <p>
            <strong>Citações OpenAlex:</strong> {work.citations}
          </p>
        )}
      </div>

      {externalUrl && (
        <a
          href={externalUrl}
          target="_blank"
          rel="noreferrer"
          className="mt-3 inline-flex items-center gap-1 text-xs font-bold text-blue-700 hover:underline"
        >
          Abrir registo
          <ExternalLink className="h-3.5 w-3.5" />
        </a>
      )}
    </div>
  );
}

function CandidateCard({ candidate, note, onNoteChange, onReview, busy }) {
  const pending = candidate.reviewStatus === "PENDING";
  const rejectDisabled = busy || !String(note || "").trim();

  return (
    <article className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span
              className={`rounded-full border px-3 py-1 text-xs font-black ${
                STATUS_STYLES[candidate.reviewStatus] || STATUS_STYLES.PENDING
              }`}
            >
              {STATUS_LABELS[candidate.reviewStatus] || candidate.reviewStatus}
            </span>
            <span className="rounded-full bg-blue-100 px-3 py-1 text-xs font-black text-blue-800">
              {candidate.similarityScore}% de confiança
            </span>
          </div>
          <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-600">
            {candidate.rationale}
          </p>
        </div>

        <div className="grid grid-cols-3 gap-2 text-center text-xs">
          <div className="rounded-xl bg-slate-50 px-3 py-2">
            <p className="font-black text-slate-950">{candidate.titleSimilarity}%</p>
            <p className="text-slate-500">título</p>
          </div>
          <div className="rounded-xl bg-slate-50 px-3 py-2">
            <p className="font-black text-slate-950">
              {candidate.doiExactMatch ? "Sim" : "Não"}
            </p>
            <p className="text-slate-500">DOI igual</p>
          </div>
          <div className="rounded-xl bg-slate-50 px-3 py-2">
            <p className="font-black text-slate-950">
              {candidate.publicationYearCompatible ? "Sim" : "Não"}
            </p>
            <p className="text-slate-500">ano compatível</p>
          </div>
        </div>
      </div>

      <div className="mt-5 grid gap-4 xl:grid-cols-2">
        <WorkCard work={candidate.leftWork} />
        <WorkCard work={candidate.rightWork} />
      </div>

      {pending ? (
        <div className="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-4">
          <label className="text-xs font-black uppercase tracking-wide text-slate-600">
            Observação da revisão
          </label>
          <textarea
            value={note || ""}
            onChange={(event) => onNoteChange(event.target.value)}
            rows={3}
            maxLength={2000}
            placeholder="Obrigatória para rejeitar; opcional para confirmar."
            className="mt-2 w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
          />

          <div className="mt-3 flex flex-wrap gap-3">
            <button
              type="button"
              disabled={busy}
              onClick={() => onReview("CONFIRMED")}
              className="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <CheckCircle2 className="h-4 w-4" />
              Confirmar mesma obra
            </button>
            <button
              type="button"
              disabled={rejectDisabled}
              onClick={() => onReview("REJECTED")}
              className="inline-flex items-center gap-2 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-sm font-bold text-rose-800 transition hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <XCircle className="h-4 w-4" />
              Rejeitar candidato
            </button>
          </div>
        </div>
      ) : (
        <div className="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
          <p>
            <strong className="text-slate-900">Revisado por:</strong>{" "}
            {candidate.reviewedBy || "utilizador autenticado"}
          </p>
          {candidate.reviewerNote && (
            <p className="mt-2">
              <strong className="text-slate-900">Observação:</strong>{" "}
              {candidate.reviewerNote}
            </p>
          )}
        </div>
      )}
    </article>
  );
}

export default function BibliographicDeduplication() {
  const [searchParams, setSearchParams] = useSearchParams();
  const researcherFromUrl = searchParams.get("researcherId") || "";
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState(researcherFromUrl);
  const [result, setResult] = useState(null);
  const [notes, setNotes] = useState({});
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingCandidates, setLoadingCandidates] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [busyCandidateId, setBusyCandidateId] = useState("");
  const [error, setError] = useState("");

  const selectedResearcher = useMemo(
    () => researchers.find((item) => item.id === selectedResearcherId),
    [researchers, selectedResearcherId]
  );

  async function loadResearchers() {
    setLoadingResearchers(true);
    setError("");
    try {
      const data = await researcherService.findAll();
      setResearchers(data);
      if (researcherFromUrl && !data.some((item) => item.id === researcherFromUrl)) {
        setSelectedResearcherId("");
        setSearchParams({});
      }
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar pesquisadores.");
    } finally {
      setLoadingResearchers(false);
    }
  }

  async function loadCandidates(researcherId = selectedResearcherId) {
    if (!researcherId) return;
    setLoadingCandidates(true);
    setError("");
    try {
      const data = await bibliographicDeduplicationService.findByResearcher(
        researcherId
      );
      setResult(data);
    } catch (apiError) {
      setResult(null);
      setError(apiError?.message || "Não foi possível carregar a revisão bibliográfica.");
    } finally {
      setLoadingCandidates(false);
    }
  }

  async function scan() {
    if (!selectedResearcherId) return;
    setScanning(true);
    setError("");
    try {
      const data = await bibliographicDeduplicationService.scan(
        selectedResearcherId
      );
      setResult(data);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível analisar as obras importadas.");
    } finally {
      setScanning(false);
    }
  }

  async function review(candidate, status) {
    setBusyCandidateId(candidate.id);
    setError("");
    try {
      await bibliographicDeduplicationService.review(
        selectedResearcherId,
        candidate.id,
        status,
        notes[candidate.id]
      );
      setNotes((current) => ({ ...current, [candidate.id]: "" }));
      await loadCandidates(selectedResearcherId);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível guardar a decisão humana.");
    } finally {
      setBusyCandidateId("");
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    setResult(null);
    setNotes({});
    if (selectedResearcherId) {
      loadCandidates(selectedResearcherId);
    }
  }, [selectedResearcherId]);

  function selectResearcher(event) {
    const researcherId = event.target.value;
    setSelectedResearcherId(researcherId);
    setError("");
    setSearchParams(researcherId ? { researcherId } : {});
  }

  const candidates = Array.isArray(result?.candidates) ? result.candidates : [];

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Integridade bibliográfica"
        title="Deduplicação Bibliográfica"
        description="Compara obras ORCID e OpenAlex por DOI, título e ano. Nenhum registo é fundido ou eliminado sem decisão humana."
        actions={
          <PrimaryButton variant="light" icon={RefreshCw} onClick={loadResearchers}>
            Atualizar pesquisadores
          </PrimaryButton>
        }
      />

      <section className="rounded-3xl border border-blue-200 bg-blue-50 p-5 sm:p-6">
        <div className="flex items-start gap-4">
          <CopyCheck className="mt-0.5 h-6 w-6 shrink-0 text-blue-700" />
          <div>
            <h3 className="font-black text-blue-950">Confirmação humana obrigatória</h3>
            <p className="mt-2 text-sm leading-7 text-blue-900/80">
              A plataforma apenas identifica candidatos. A confirmação indica que as duas
              referências representam a mesma obra intelectual e orienta correções manuais
              nas fontes externas, sem alteração automática do ORCID, OpenAlex ou Google Acadêmico.
            </p>
          </div>
        </div>
      </section>

      {error && <ErrorState title="Atenção" message={error} />}
      {loadingResearchers && <LoadingState message="Carregando pesquisadores..." />}

      {!loadingResearchers && (
        <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
          <div className="grid gap-4 lg:grid-cols-[1fr_auto] lg:items-end">
            <div>
              <label className="text-sm font-bold text-slate-700">Pesquisador</label>
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

            <PrimaryButton
              icon={ScanSearch}
              disabled={!selectedResearcherId}
              loading={scanning}
              onClick={scan}
            >
              Analisar obras
            </PrimaryButton>
          </div>

          {selectedResearcher && (
            <p className="mt-4 text-sm text-slate-500">
              Pesquisador selecionado: <strong className="text-slate-900">{selectedResearcher.fullName}</strong>
            </p>
          )}
        </section>
      )}

      {selectedResearcherId && loadingCandidates && (
        <LoadingState message="Carregando candidatos de duplicidade..." />
      )}

      {selectedResearcherId && !loadingCandidates && result && (
        <div className="grid gap-3 sm:grid-cols-4">
          <SummaryCard label="Candidatos" value={result.totalCandidates || 0} />
          <SummaryCard label="Pendentes" value={result.pendingCandidates || 0} tone="amber" />
          <SummaryCard label="Confirmados" value={result.confirmedCandidates || 0} tone="emerald" />
          <SummaryCard label="Rejeitados" value={result.rejectedCandidates || 0} tone="rose" />
        </div>
      )}

      {!loadingResearchers && !selectedResearcherId && (
        <EmptyState
          icon={ScanSearch}
          title="Selecione um pesquisador"
          description="Escolha um pesquisador para comparar as obras importadas do ORCID com as obras do OpenAlex."
        />
      )}

      {selectedResearcherId && !loadingCandidates && result && candidates.length === 0 && (
        <EmptyState
          icon={CheckCircle2}
          title="Nenhum candidato identificado"
          description="Execute a análise após importar obras no ORCID e no OpenAlex. Candidatos só aparecem quando há evidência bibliográfica suficiente."
        />
      )}

      {candidates.length > 0 && (
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-amber-600" />
            <h3 className="font-black text-slate-950">Candidatos para revisão</h3>
          </div>

          {candidates.map((candidate) => (
            <CandidateCard
              key={candidate.id}
              candidate={candidate}
              note={notes[candidate.id]}
              onNoteChange={(value) =>
                setNotes((current) => ({ ...current, [candidate.id]: value }))
              }
              onReview={(status) => review(candidate, status)}
              busy={busyCandidateId === candidate.id}
            />
          ))}
        </div>
      )}
    </div>
  );
}
