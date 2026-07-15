import { useEffect, useMemo, useState } from "react";
import { GraduationCap, Plus, RefreshCw, Search } from "lucide-react";
import AcademicProfileForm from "../components/academicProfiles/AcademicProfileForm";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { academicProfileService } from "../services/academicProfileService";
import { researcherService } from "../services/researcherService";
import { formatDateTime } from "../utils/formatters";

export default function AcademicProfiles() {
  const [profiles, setProfiles] = useState([]);
  const [researchers, setResearchers] = useState([]);
  const [selectedProfile, setSelectedProfile] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadData() {
    setLoading(true);
    setError("");

    try {
      const [profileData, researcherData] = await Promise.all([
        academicProfileService.findAll(),
        researcherService.findAll()
      ]);

      setProfiles(profileData);
      setResearchers(researcherData);
    } catch (apiError) {
      setError(
        apiError?.message || "Não foi possível carregar perfis acadêmicos."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  const filteredProfiles = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) return profiles;

    return profiles.filter((profile) => {
      const searchable = [
        profile.researcherName,
        profile.researcherEmail,
        profile.institution,
        profile.department,
        profile.researchArea,
        profile.keywords
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchable.includes(normalizedQuery);
    });
  }, [profiles, query]);

  function startCreate() {
    setSelectedProfile(null);
    setShowForm(true);
    setError("");
    setSuccess("");
  }

  function startEdit(profile) {
    setSelectedProfile(profile);
    setShowForm(true);
    setError("");
    setSuccess("");
  }

  function cancelForm() {
    setSelectedProfile(null);
    setShowForm(false);
  }

  async function handleSubmit(formData) {
    setSaving(true);
    setError("");
    setSuccess("");

    try {
      if (selectedProfile?.id) {
        await academicProfileService.update(selectedProfile.id, formData);
        setSuccess("Perfil acadêmico atualizado com sucesso.");
      } else {
        await academicProfileService.create(formData);
        setSuccess("Perfil acadêmico criado com sucesso.");
      }

      cancelForm();
      await loadData();
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível salvar perfil acadêmico.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Otimização acadêmica"
        title="Perfil Acadêmico"
        description="Gestão dos perfis acadêmicos dos pesquisadores: biografia, área de pesquisa, palavras-chave e perfis científicos."
        actions={
          <>
            <PrimaryButton variant="light" icon={RefreshCw} onClick={loadData}>
              Atualizar
            </PrimaryButton>

            <PrimaryButton icon={Plus} onClick={startCreate}>
              Novo perfil
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
            placeholder="Buscar por pesquisador, e-mail, instituição, área ou palavra-chave..."
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
        <AcademicProfileForm
          researchers={researchers}
          initialData={selectedProfile}
          mode={selectedProfile ? "edit" : "create"}
          loading={saving}
          onSubmit={handleSubmit}
          onCancel={cancelForm}
        />
      )}

      {loading && <LoadingState message="Carregando perfis acadêmicos..." />}

      {!loading && filteredProfiles.length === 0 && (
        <EmptyState
          icon={GraduationCap}
          title="Nenhum perfil acadêmico encontrado"
          description="Crie um perfil acadêmico para organizar dados científicos do pesquisador."
          action={
            <PrimaryButton icon={Plus} onClick={startCreate}>
              Criar perfil acadêmico
            </PrimaryButton>
          }
        />
      )}

      {!loading && filteredProfiles.length > 0 && (
        <div className="grid gap-4 xl:grid-cols-2">
          {filteredProfiles.map((profile) => (
            <div
              key={profile.id}
              className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm"
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h3 className="text-lg font-black text-slate-950">
                    {profile.researcherName || "Pesquisador"}
                  </h3>

                  <p className="mt-1 text-sm text-slate-500">
                    {profile.researcherEmail || "E-mail não informado"}
                  </p>
                </div>

                <Badge variant="blue">
                  {profile.profileCompletionPercentage || 0}% completo
                </Badge>
              </div>

              <div className="mt-5 space-y-3 text-sm">
                <p>
                  <span className="font-bold text-slate-700">Área: </span>
                  <span className="text-slate-600">
                    {profile.researchArea || "Não informada"}
                  </span>
                </p>

                <p>
                  <span className="font-bold text-slate-700">Instituição: </span>
                  <span className="text-slate-600">
                    {profile.institution || "Não informada"}
                  </span>
                </p>

                <p>
                  <span className="font-bold text-slate-700">Palavras-chave: </span>
                  <span className="text-slate-600">
                    {profile.keywords || "Não informadas"}
                  </span>
                </p>

                <p>
                  <span className="font-bold text-slate-700">Atualizado: </span>
                  <span className="text-slate-600">
                    {formatDateTime(profile.updatedAt || profile.createdAt)}
                  </span>
                </p>
              </div>

              <div className="mt-5 flex justify-end">
                <PrimaryButton variant="light" onClick={() => startEdit(profile)}>
                  Editar perfil
                </PrimaryButton>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}