import { useEffect, useState } from "react";
import { ArrowLeft, BookOpenCheck, FileText, Link2, RefreshCw } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import AcademicProfileForm from "../components/academicProfiles/AcademicProfileForm";
import ResearcherForm from "../components/researchers/ResearcherForm";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { academicProfileService } from "../services/academicProfileService";
import { orcidService } from "../services/orcidService";
import { researcherService } from "../services/researcherService";
import { formatDateTime } from "../utils/formatters";

export default function ResearcherDetails() {
  const { id } = useParams();

  const [researcher, setResearcher] = useState(null);
  const [profile, setProfile] = useState(null);
  const [orcidSummary, setOrcidSummary] = useState(null);
  const [orcidWorks, setOrcidWorks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingResearcher, setSavingResearcher] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);
  const [orcidLoading, setOrcidLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadResearcherDetails() {
    setLoading(true);
    setError("");

    try {
      const researcherData = await researcherService.findById(id);
      setResearcher(researcherData);

      try {
        const profileData = await academicProfileService.findByResearcherId(id);
        setProfile(profileData);
      } catch {
        setProfile(null);
      }

      if (researcherData?.orcidId) {
        try {
          const summary = await orcidService.findSummaryByResearcher(id);
          setOrcidSummary(summary);
        } catch {
          setOrcidSummary(null);
        }

        try {
          const works = await orcidService.findWorksByResearcher(id);
          setOrcidWorks(works);
        } catch {
          setOrcidWorks([]);
        }
      }
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar o pesquisador.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadResearcherDetails();
  }, [id]);

  async function handleUpdateResearcher(formData) {
    setSavingResearcher(true);
    setError("");
    setSuccess("");

    try {
      const updated = await researcherService.update(id, formData);
      setResearcher(updated);
      setSuccess("Dados do pesquisador atualizados com sucesso.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível atualizar pesquisador.");
    } finally {
      setSavingResearcher(false);
    }
  }

  async function handleSaveProfile(formData) {
    setSavingProfile(true);
    setError("");
    setSuccess("");

    try {
      if (profile?.id) {
        const updated = await academicProfileService.update(profile.id, formData);
        setProfile(updated);
        setSuccess("Perfil acadêmico atualizado com sucesso.");
      } else {
        const created = await academicProfileService.create({
          ...formData,
          researcherId: id
        });
        setProfile(created);
        setSuccess("Perfil acadêmico criado com sucesso.");
      }
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível salvar perfil acadêmico.");
    } finally {
      setSavingProfile(false);
    }
  }

  async function handleImportOrcidWorks() {
    setOrcidLoading(true);
    setError("");
    setSuccess("");

    try {
      await orcidService.importWorks(id);
      const works = await orcidService.findWorksByResearcher(id);
      setOrcidWorks(works);
      setSuccess("Obras ORCID importadas com sucesso.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível importar obras do ORCID.");
    } finally {
      setOrcidLoading(false);
    }
  }

  async function handleSyncOrcidProfile() {
    setOrcidLoading(true);
    setError("");
    setSuccess("");

    try {
      await orcidService.syncProfile(id);
      const profileData = await academicProfileService.findByResearcherId(id);
      setProfile(profileData);
      setSuccess("Perfil acadêmico sincronizado com ORCID.");
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível sincronizar com ORCID.");
    } finally {
      setOrcidLoading(false);
    }
  }

  if (loading) {
    return <LoadingState message="Carregando detalhes do pesquisador..." />;
  }

  if (error && !researcher) {
    return <ErrorState title="Erro ao carregar pesquisador" message={error} />;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Detalhe do pesquisador"
        title={researcher?.fullName || "Pesquisador"}
        description="Dados cadastrais, perfil acadêmico e integração ORCID do pesquisador."
        actions={
          <>
            <Link to="/admin/researchers">
              <PrimaryButton variant="light" icon={ArrowLeft}>
                Voltar
              </PrimaryButton>
            </Link>

            <PrimaryButton
              variant="light"
              icon={RefreshCw}
              onClick={loadResearcherDetails}
            >
              Atualizar
            </PrimaryButton>
          </>
        }
      >
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <div className="rounded-2xl bg-slate-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
              E-mail
            </p>
            <p className="mt-2 break-all font-bold text-slate-950">
              {researcher?.email || "Não informado"}
            </p>
          </div>

          <div className="rounded-2xl bg-slate-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
              Instituição
            </p>
            <p className="mt-2 font-bold text-slate-950">
              {researcher?.institution || "Não informada"}
            </p>
          </div>

          <div className="rounded-2xl bg-slate-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
              ORCID
            </p>
            <p className="mt-2 font-bold text-slate-950">
              {researcher?.orcidId || "Não informado"}
            </p>
          </div>

          <div className="rounded-2xl bg-slate-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wide text-slate-500">
              Atualizado
            </p>
            <p className="mt-2 font-bold text-slate-950">
              {formatDateTime(researcher?.updatedAt || researcher?.createdAt)}
            </p>
          </div>
        </div>
      </PageHeader>

      {error && <ErrorState title="Atenção" message={error} />}

      {success && (
        <div className="rounded-3xl border border-emerald-200 bg-emerald-50 p-4 text-sm font-semibold text-emerald-800">
          {success}
        </div>
      )}

      <ResearcherForm
        mode="edit"
        initialData={researcher}
        loading={savingResearcher}
        onSubmit={handleUpdateResearcher}
      />

      <AcademicProfileForm
        researchers={researcher ? [researcher] : []}
        researcherId={id}
        lockResearcher
        mode={profile ? "edit" : "create"}
        initialData={profile}
        loading={savingProfile}
        onSubmit={handleSaveProfile}
      />

      <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <h3 className="text-lg font-black text-slate-950">ORCID</h3>
            <p className="mt-1 text-sm leading-6 text-slate-500">
              Importe obras, consulte resumo público e sincronize dados com o
              perfil acadêmico.
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <PrimaryButton
              variant="light"
              icon={BookOpenCheck}
              loading={orcidLoading}
              disabled={!researcher?.orcidId}
              onClick={handleImportOrcidWorks}
            >
              Importar obras
            </PrimaryButton>

            <PrimaryButton
              icon={Link2}
              loading={orcidLoading}
              disabled={!researcher?.orcidId}
              onClick={handleSyncOrcidProfile}
            >
              Sincronizar perfil
            </PrimaryButton>
          </div>
        </div>

        {!researcher?.orcidId && (
          <EmptyState
            icon={Link2}
            title="Pesquisador sem ORCID"
            description="Informe o ORCID ID no cadastro do pesquisador para liberar importação e sincronização."
          />
        )}

        {researcher?.orcidId && (
          <div className="mt-6 grid gap-6 xl:grid-cols-2">
            <div className="rounded-2xl bg-slate-50 p-5">
              <h4 className="font-black text-slate-950">Resumo ORCID</h4>

              {orcidSummary ? (
                <pre className="mt-4 max-h-80 overflow-auto whitespace-pre-wrap rounded-2xl bg-slate-950 p-4 text-xs leading-6 text-slate-100">
                  {JSON.stringify(orcidSummary, null, 2)}
                </pre>
              ) : (
                <p className="mt-3 text-sm text-slate-500">
                  Nenhum resumo carregado ainda.
                </p>
              )}
            </div>

            <div className="rounded-2xl bg-slate-50 p-5">
              <div className="mb-4 flex items-center justify-between">
                <h4 className="font-black text-slate-950">Obras importadas</h4>
                <Badge variant="blue">{orcidWorks.length} obras</Badge>
              </div>

              {orcidWorks.length === 0 ? (
                <p className="text-sm text-slate-500">
                  Nenhuma obra ORCID importada para este pesquisador.
                </p>
              ) : (
                <div className="space-y-3">
                  {orcidWorks.map((work, index) => (
                    <div
                      key={work.id || work.putCode || index}
                      className="rounded-2xl border border-slate-200 bg-white p-4"
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
          </div>
        )}

        <div className="mt-6 rounded-2xl border border-amber-200 bg-amber-50 p-4">
          <div className="flex gap-3">
            <FileText className="mt-0.5 h-5 w-5 text-amber-700" />
            <p className="text-sm leading-6 text-amber-900">
              O módulo Google Acadêmico continua apenas como checklist e
              orientação manual. Nenhuma automação, scraping, acesso direto ou
              alteração automática é realizada.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}