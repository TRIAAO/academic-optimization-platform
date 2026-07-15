import { useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  CheckCircle2,
  ClipboardCheck,
  RefreshCw,
  ShieldCheck
} from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { APP_CONFIG } from "../config/app";
import { googleScholarService } from "../services/googleScholarService";
import { researcherService } from "../services/researcherService";
import { formatDateTime, formatNumber } from "../utils/formatters";

function getStatusBadge(item) {
  if (item.status === "OK") {
    return <Badge variant="green">OK</Badge>;
  }

  if (item.status === "ACTION_REQUIRED") {
    return <Badge variant="red">Ação necessária</Badge>;
  }

  if (item.status === "PENDING") {
    return <Badge variant="amber">Pendente</Badge>;
  }

  if (item.status === "SECURITY") {
    return <Badge variant="blue">Segurança</Badge>;
  }

  return <Badge variant="slate">Info</Badge>;
}

function getPriorityBadge(priority) {
  const normalized = String(priority || "").toUpperCase();

  if (normalized === "ALTA") {
    return <Badge variant="red">Alta</Badge>;
  }

  if (normalized === "MÉDIA" || normalized === "MEDIA") {
    return <Badge variant="amber">Média</Badge>;
  }

  return <Badge variant="blue">Baixa</Badge>;
}

function getItemIcon(item) {
  if (item.status === "OK") return CheckCircle2;
  if (item.status === "SECURITY") return ShieldCheck;
  if (item.status === "ACTION_REQUIRED") return AlertTriangle;
  return ClipboardCheck;
}

export default function GoogleScholarChecklist() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [checklistResult, setChecklistResult] = useState(null);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
  const [loadingChecklist, setLoadingChecklist] = useState(false);
  const [error, setError] = useState("");

  const selectedResearcher = useMemo(() => {
    return researchers.find((researcher) => researcher.id === selectedResearcherId);
  }, [researchers, selectedResearcherId]);

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

  async function generateChecklist(researcherId = selectedResearcherId) {
    if (!researcherId) return;

    setLoadingChecklist(true);
    setError("");

    try {
      const result =
        await googleScholarService.buildChecklistByResearcher(researcherId);

      setChecklistResult(result);
    } catch (apiError) {
      setChecklistResult(null);
      setError(
        apiError?.message ||
          "Não foi possível gerar checklist personalizado para este pesquisador."
      );
    } finally {
      setLoadingChecklist(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  useEffect(() => {
    if (selectedResearcherId) {
      generateChecklist(selectedResearcherId);
    } else {
      setChecklistResult(null);
    }
  }, [selectedResearcherId]);

  const checklist = checklistResult?.checklist || [];
  const summary = checklistResult?.summary || {};

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Orientação manual personalizada"
        title="Checklist Google Acadêmico"
        description="Checklist gerado com base nos dados reais do pesquisador: cadastro, perfil acadêmico, ORCID, OpenAlex, revisão manual e Crossref."
        actions={
          <PrimaryButton variant="light" icon={RefreshCw} onClick={loadResearchers}>
            Atualizar pesquisadores
          </PrimaryButton>
        }
      />

      <section className="rounded-3xl border border-amber-200 bg-amber-50 p-6">
        <div className="flex gap-4">
          <ShieldCheck className="mt-1 h-6 w-6 shrink-0 text-amber-700" />
          <div>
            <h3 className="font-black text-amber-950">
              Regra fixa de segurança
            </h3>
            <p className="mt-2 text-sm leading-7 text-amber-900">
              {APP_CONFIG.googleScholarPolicy}
            </p>
          </div>
        </div>
      </section>

      {error && <ErrorState title="Atenção" message={error} />}

      {loadingResearchers && (
        <LoadingState message="Carregando pesquisadores para checklist..." />
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
                  onChange={(event) => setSelectedResearcherId(event.target.value)}
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
                icon={RefreshCw}
                disabled={!selectedResearcherId}
                loading={loadingChecklist}
                onClick={() => generateChecklist()}
              >
                Gerar checklist real
              </PrimaryButton>
            </div>

            {selectedResearcher && (
              <div className="mt-5 rounded-2xl bg-slate-50 p-4">
                <p className="font-black text-slate-950">
                  {selectedResearcher.fullName}
                </p>
                <p className="mt-1 text-sm text-slate-500">
                  {selectedResearcher.institution || "Instituição não informada"} ·{" "}
                  {selectedResearcher.orcidId || "Sem ORCID"}
                </p>
              </div>
            )}
          </section>

          {!selectedResearcherId && (
            <EmptyState
              icon={ClipboardCheck}
              title="Selecione um pesquisador"
              description="Escolha um pesquisador para gerar um checklist real e personalizado com base nos dados da plataforma."
            />
          )}

          {selectedResearcherId && loadingChecklist && (
            <LoadingState message="Gerando checklist personalizado..." />
          )}

          {selectedResearcherId && !loadingChecklist && checklistResult && (
            <>
              <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
                <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                  <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                    Itens
                  </p>
                  <p className="mt-2 text-3xl font-black text-slate-950">
                    {formatNumber(summary.totalItems)}
                  </p>
                </div>

                <div className="rounded-3xl border border-emerald-200 bg-emerald-50 p-5 shadow-sm">
                  <p className="text-xs font-black uppercase tracking-wide text-emerald-700">
                    OK
                  </p>
                  <p className="mt-2 text-3xl font-black text-emerald-700">
                    {formatNumber(summary.totalOk)}
                  </p>
                </div>

                <div className="rounded-3xl border border-amber-200 bg-amber-50 p-5 shadow-sm">
                  <p className="text-xs font-black uppercase tracking-wide text-amber-700">
                    Pendentes
                  </p>
                  <p className="mt-2 text-3xl font-black text-amber-700">
                    {formatNumber(summary.totalPending)}
                  </p>
                </div>

                <div className="rounded-3xl border border-red-200 bg-red-50 p-5 shadow-sm">
                  <p className="text-xs font-black uppercase tracking-wide text-red-700">
                    Ações
                  </p>
                  <p className="mt-2 text-3xl font-black text-red-700">
                    {formatNumber(summary.totalActionRequired)}
                  </p>
                </div>

                <div className="rounded-3xl border border-blue-200 bg-blue-50 p-5 shadow-sm">
                  <p className="text-xs font-black uppercase tracking-wide text-blue-700">
                    Gerado em
                  </p>
                  <p className="mt-2 text-sm font-black text-blue-700">
                    {formatDateTime(checklistResult.generatedAt)}
                  </p>
                </div>
              </section>

              <section className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
                <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                  <div className="mb-5 flex items-center justify-between">
                    <h3 className="font-black text-slate-950">
                      Checklist personalizado
                    </h3>

                    <Badge variant="blue">{checklist.length} itens</Badge>
                  </div>

                  <div className="space-y-4">
                    {checklist.map((item, index) => {
                      const Icon = getItemIcon(item);

                      return (
                        <div
                          key={`${item.title}-${index}`}
                          className="rounded-2xl border border-slate-200 bg-slate-50 p-5"
                        >
                          <div className="flex items-start gap-4">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-white text-blue-700 shadow-sm">
                              <Icon className="h-5 w-5" />
                            </div>

                            <div className="min-w-0 flex-1">
                              <div className="flex flex-wrap items-center gap-2">
                                <p className="font-black text-slate-950">
                                  {index + 1}. {item.title}
                                </p>

                                {getStatusBadge(item)}
                                {getPriorityBadge(item.priority)}
                              </div>

                              <p className="mt-2 text-sm leading-7 text-slate-600">
                                {item.description}
                              </p>

                              {item.evidence && (
                                <div className="mt-3 rounded-2xl bg-white p-3 text-xs leading-6 text-slate-600">
                                  <span className="font-black text-slate-800">
                                    Evidência:{" "}
                                  </span>
                                  {item.evidence}
                                </div>
                              )}

                              {item.action && (
                                <div className="mt-3 rounded-2xl border border-blue-100 bg-blue-50 p-3 text-xs leading-6 text-blue-900">
                                  <span className="font-black">Ação: </span>
                                  {item.action}
                                </div>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>

                <div className="space-y-6">
                  <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                    <h3 className="font-black text-slate-950">
                      Base usada para gerar o checklist
                    </h3>

                    <div className="mt-5 space-y-3">
                      <div className="rounded-2xl bg-slate-50 p-4">
                        <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                          Obras ORCID
                        </p>
                        <p className="mt-2 text-2xl font-black text-slate-950">
                          {formatNumber(summary.totalOrcidWorks)}
                        </p>
                      </div>

                      <div className="rounded-2xl bg-slate-50 p-4">
                        <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                          Obras OpenAlex
                        </p>
                        <p className="mt-2 text-2xl font-black text-slate-950">
                          {formatNumber(summary.totalOpenAlexWorks)}
                        </p>
                      </div>

                      <div className="rounded-2xl bg-slate-50 p-4">
                        <p className="text-xs font-black uppercase tracking-wide text-slate-500">
                          Validações Crossref
                        </p>
                        <p className="mt-2 text-2xl font-black text-slate-950">
                          {formatNumber(summary.totalCrossrefValidations)}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                    <h3 className="font-black text-slate-950">
                      O que este módulo faz
                    </h3>

                    <div className="mt-5 space-y-4 text-sm leading-7 text-slate-600">
                      <p>
                        O checklist é personalizado a partir dos dados reais do
                        pesquisador existentes no sistema.
                      </p>

                      <p>
                        Ele orienta a equipa e o pesquisador sobre o que deve ser
                        conferido manualmente no Google Acadêmico.
                      </p>

                      <p>
                        A plataforma não acessa, não altera, não coleta e não
                        automatiza dados do Google Acadêmico.
                      </p>
                    </div>
                  </div>

                  <details className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                    <summary className="cursor-pointer font-black text-slate-950">
                      Ver retorno técnico
                    </summary>

                    <pre className="mt-5 max-h-[520px] overflow-auto whitespace-pre-wrap rounded-2xl bg-slate-950 p-5 text-xs leading-6 text-slate-100">
                      {JSON.stringify(checklistResult, null, 2)}
                    </pre>
                  </details>
                </div>
              </section>
            </>
          )}
        </>
      )}
    </div>
  );
}