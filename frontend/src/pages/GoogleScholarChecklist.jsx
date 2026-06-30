import { useEffect, useMemo, useState } from "react";
import { ClipboardCheck, RefreshCw, ShieldCheck } from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { APP_CONFIG } from "../config/app";
import { DEFAULT_GOOGLE_SCHOLAR_CHECKLIST } from "../services/googleScholarService";
import { researcherService } from "../services/researcherService";

export default function GoogleScholarChecklist() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcherId, setSelectedResearcherId] = useState("");
  const [checklist] = useState(DEFAULT_GOOGLE_SCHOLAR_CHECKLIST);
  const [loadingResearchers, setLoadingResearchers] = useState(true);
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

  useEffect(() => {
    loadResearchers();
  }, []);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Orientação manual segura"
        title="Checklist Google Acadêmico"
        description="Checklist de orientação para o pesquisador revisar manualmente o perfil no Google Acadêmico."
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

      {error && (
        <div className="rounded-3xl border border-red-200 bg-red-50 p-4 text-sm font-semibold text-red-800">
          {error}
        </div>
      )}

      {loadingResearchers && (
        <LoadingState message="Carregando pesquisadores para checklist..." />
      )}

      {!loadingResearchers && (
        <>
          <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
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
              description="Escolha um pesquisador para visualizar o checklist manual."
            />
          )}

          {selectedResearcherId && (
            <section className="grid gap-6 xl:grid-cols-[1fr_0.8fr]">
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="mb-5 flex items-center justify-between">
                  <h3 className="font-black text-slate-950">
                    Checklist manual
                  </h3>

                  <Badge variant="blue">{checklist.length} itens</Badge>
                </div>

                <div className="space-y-4">
                  {checklist.map((item, index) => (
                    <div
                      key={`${item.title}-${index}`}
                      className="rounded-2xl border border-slate-200 bg-slate-50 p-5"
                    >
                      <div className="flex items-start gap-4">
                        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl bg-blue-100 text-sm font-black text-blue-700">
                          {index + 1}
                        </div>

                        <div>
                          <p className="font-black text-slate-950">
                            {item.title}
                          </p>

                          <p className="mt-2 text-sm leading-7 text-slate-600">
                            {item.description}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="font-black text-slate-950">
                  O que este módulo faz
                </h3>

                <div className="mt-5 space-y-4 text-sm leading-7 text-slate-600">
                  <p>
                    Este módulo ajuda a orientar o pesquisador sobre os passos
                    manuais para melhorar seu perfil no Google Acadêmico.
                  </p>

                  <p>
                    A plataforma não acessa a conta do pesquisador, não coleta
                    dados diretamente do Google Acadêmico e não executa qualquer
                    automação.
                  </p>

                  <p>
                    As informações oficiais usadas pela plataforma vêm de fontes
                    permitidas e integradas, como ORCID, OpenAlex e Crossref.
                  </p>
                </div>
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}