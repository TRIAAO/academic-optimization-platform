import { useEffect, useMemo, useState } from "react";
import { Plus, RefreshCw, Search, UserRoundSearch } from "lucide-react";
import AcademicProfileForm from "../components/academicProfiles/AcademicProfileForm";
import ResearcherForm from "../components/researchers/ResearcherForm";
import ResearcherTable from "../components/researchers/ResearcherTable";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { academicProfileService } from "../services/academicProfileService";
import { researcherService } from "../services/researcherService";

export default function Researchers() {
  const [researchers, setResearchers] = useState([]);
  const [selectedResearcher, setSelectedResearcher] = useState(null);
  const [profileResearcher, setProfileResearcher] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [showProfileForm, setShowProfileForm] = useState(false);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadResearchers() {
    setLoading(true);
    setError("");

    try {
      const data = await researcherService.findAll();
      setResearchers(data);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar pesquisadores.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadResearchers();
  }, []);

  const filteredResearchers = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) return researchers;

    return researchers.filter((researcher) => {
      const searchable = [
        researcher.fullName,
        researcher.email,
        researcher.phone,
        researcher.institution,
        researcher.department,
        researcher.academicTitle,
        researcher.orcidId
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchable.includes(normalizedQuery);
    });
  }, [query, researchers]);

  function startCreate() {
    setSelectedResearcher(null);
    setProfileResearcher(null);
    setShowProfileForm(false);
    setShowForm(true);
    setSuccess("");
    setError("");
  }

  function startEdit(researcher) {
    setSelectedResearcher(researcher);
    setProfileResearcher(null);
    setShowProfileForm(false);
    setShowForm(true);
    setSuccess("");
    setError("");
  }

  function startProfile(researcher) {
    setProfileResearcher(researcher);
    setSelectedResearcher(null);
    setShowForm(false);
    setShowProfileForm(true);
    setSuccess("");
    setError("");
  }

  function cancelForms() {
    setShowForm(false);
    setShowProfileForm(false);
    setSelectedResearcher(null);
    setProfileResearcher(null);
  }

  async function handleSubmitResearcher(formData) {
    setSaving(true);
    setError("");
    setSuccess("");

    try {
      if (selectedResearcher?.id) {
        await researcherService.update(selectedResearcher.id, formData);
        setSuccess("Pesquisador atualizado com sucesso.");
      } else {
        await researcherService.create(formData);
        setSuccess("Pesquisador cadastrado com sucesso.");
      }

      cancelForms();
      await loadResearchers();
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível salvar pesquisador.");
    } finally {
      setSaving(false);
    }
  }

  async function handleCreateProfile(formData) {
    setSaving(true);
    setError("");
    setSuccess("");

    try {
      await academicProfileService.create(formData);
      setSuccess("Perfil acadêmico criado com sucesso.");
      cancelForms();
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível criar perfil acadêmico.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteResearcher(researcher) {
    const confirmed = window.confirm(
      `Deseja remover/desativar o pesquisador "${researcher.fullName}"?`
    );

    if (!confirmed) return;

    setError("");
    setSuccess("");

    try {
      await researcherService.deactivate(researcher.id);
      setSuccess("Pesquisador removido/desativado com sucesso.");
      await loadResearchers();
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível remover pesquisador.");
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Cadastro institucional"
        title="Pesquisadores"
        description="Gestão dos professores e pesquisadores da Universidade Metropolitana de Angola / IMETRO."
        actions={
          <>
            <PrimaryButton variant="light" icon={RefreshCw} onClick={loadResearchers}>
              Atualizar
            </PrimaryButton>

            <PrimaryButton icon={Plus} onClick={startCreate}>
              Novo pesquisador
            </PrimaryButton>
          </>
        }
      >
        <div className="flex items-center rounded-2xl border border-slate-200 bg-slate-50 px-4">
          <Search className="h-5 w-5 text-slate-400" />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            className="w-full bg-transparent px-3 py-3 text-sm outline-none"
            placeholder="Buscar por nome, e-mail, instituição, departamento ou ORCID..."
          />
        </div>
      </PageHeader>

      {error && <ErrorState title="Atenção" message={error} />}

      {success && (
        <div className="rounded-3xl border border-emerald-200 bg-emerald-50 p-4 text-sm font-semibold text-emerald-800">
          {success}
        </div>
      )}

      {showForm && (
        <ResearcherForm
          mode={selectedResearcher ? "edit" : "create"}
          initialData={selectedResearcher}
          loading={saving}
          onSubmit={handleSubmitResearcher}
          onCancel={cancelForms}
        />
      )}

      {showProfileForm && (
        <AcademicProfileForm
          researchers={researchers}
          researcherId={profileResearcher?.id}
          lockResearcher
          loading={saving}
          onSubmit={handleCreateProfile}
          onCancel={cancelForms}
        />
      )}

      {loading && <LoadingState message="Carregando pesquisadores..." />}

      {!loading && filteredResearchers.length === 0 && (
        <EmptyState
          icon={UserRoundSearch}
          title="Nenhum pesquisador encontrado"
          description="Cadastre o primeiro pesquisador para iniciar os fluxos de perfil acadêmico, ORCID, OpenAlex, Crossref e relatórios."
          action={
            <PrimaryButton icon={Plus} onClick={startCreate}>
              Cadastrar pesquisador
            </PrimaryButton>
          }
        />
      )}

      {!loading && filteredResearchers.length > 0 && (
        <ResearcherTable
          researchers={filteredResearchers}
          onEdit={startEdit}
          onDelete={handleDeleteResearcher}
          onCreateProfile={startProfile}
        />
      )}
    </div>
  );
}