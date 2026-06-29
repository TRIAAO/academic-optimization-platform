import { useEffect, useState } from "react";
import PrimaryButton from "../ui/PrimaryButton";

const emptyForm = {
  researcherId: "",
  researchArea: "",
  biography: "",
  keywords: "",
  googleScholarUrl: "",
  orcidUrl: "",
  scopusAuthorId: "",
  webOfScienceId: "",
  lattesUrl: "",
  institutionalProfileUrl: ""
};

export default function AcademicProfileForm({
  researchers = [],
  initialData,
  researcherId,
  lockResearcher = false,
  mode = "create",
  loading = false,
  onSubmit,
  onCancel
}) {
  const [formData, setFormData] = useState(emptyForm);

  useEffect(() => {
    if (initialData) {
      setFormData({
        researcherId: initialData.researcherId || researcherId || "",
        researchArea: initialData.researchArea || "",
        biography: initialData.biography || "",
        keywords: initialData.keywords || "",
        googleScholarUrl: initialData.googleScholarUrl || "",
        orcidUrl: initialData.orcidUrl || "",
        scopusAuthorId: initialData.scopusAuthorId || "",
        webOfScienceId: initialData.webOfScienceId || "",
        lattesUrl: initialData.lattesUrl || "",
        institutionalProfileUrl: initialData.institutionalProfileUrl || ""
      });
    } else {
      setFormData({
        ...emptyForm,
        researcherId: researcherId || ""
      });
    }
  }, [initialData, researcherId]);

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((current) => ({
      ...current,
      [name]: value
    }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit(formData);
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm"
    >
      <div className="mb-6">
        <h3 className="text-lg font-black text-slate-950">
          {mode === "edit" ? "Editar perfil acadêmico" : "Novo perfil acadêmico"}
        </h3>

        <p className="mt-1 text-sm leading-6 text-slate-500">
          Organize os dados acadêmicos que ajudam na otimização da presença
          científica do pesquisador.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="md:col-span-2">
          <label className="text-sm font-bold text-slate-700">
            Pesquisador *
          </label>

          <select
            name="researcherId"
            value={formData.researcherId}
            onChange={handleChange}
            required
            disabled={lockResearcher || mode === "edit"}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100 disabled:bg-slate-100 disabled:text-slate-500"
          >
            <option value="">Selecione um pesquisador</option>

            {researchers.map((researcher) => (
              <option key={researcher.id} value={researcher.id}>
                {researcher.fullName} — {researcher.email}
              </option>
            ))}
          </select>
        </div>

        <div className="md:col-span-2">
          <label className="text-sm font-bold text-slate-700">
            Área de pesquisa
          </label>
          <input
            name="researchArea"
            value={formData.researchArea}
            onChange={handleChange}
            maxLength={180}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="Ex: Saúde Pública, Engenharia, Ciências Sociais"
          />
        </div>

        <div className="md:col-span-2">
          <label className="text-sm font-bold text-slate-700">Biografia</label>
          <textarea
            name="biography"
            value={formData.biography}
            onChange={handleChange}
            rows={5}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="Resumo acadêmico e profissional do pesquisador."
          />
        </div>

        <div className="md:col-span-2">
          <label className="text-sm font-bold text-slate-700">
            Palavras-chave
          </label>
          <input
            name="keywords"
            value={formData.keywords}
            onChange={handleChange}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="Ex: Angola, educação superior, inovação, saúde"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">
            Google Acadêmico
          </label>
          <input
            name="googleScholarUrl"
            value={formData.googleScholarUrl}
            onChange={handleChange}
            maxLength={255}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="URL do perfil manual"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">ORCID URL</label>
          <input
            name="orcidUrl"
            value={formData.orcidUrl}
            onChange={handleChange}
            maxLength={255}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="https://orcid.org/0000-0000-0000-0000"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">
            Scopus Author ID
          </label>
          <input
            name="scopusAuthorId"
            value={formData.scopusAuthorId}
            onChange={handleChange}
            maxLength={100}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">
            Web of Science ID
          </label>
          <input
            name="webOfScienceId"
            value={formData.webOfScienceId}
            onChange={handleChange}
            maxLength={100}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">Lattes URL</label>
          <input
            name="lattesUrl"
            value={formData.lattesUrl}
            onChange={handleChange}
            maxLength={255}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">
            Perfil institucional
          </label>
          <input
            name="institutionalProfileUrl"
            value={formData.institutionalProfileUrl}
            onChange={handleChange}
            maxLength={255}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
          />
        </div>
      </div>

      <div className="mt-6 flex flex-wrap justify-end gap-3">
        {onCancel && (
          <PrimaryButton variant="light" onClick={onCancel}>
            Cancelar
          </PrimaryButton>
        )}

        <PrimaryButton type="submit" loading={loading}>
          {mode === "edit" ? "Salvar perfil" : "Criar perfil"}
        </PrimaryButton>
      </div>
    </form>
  );
}