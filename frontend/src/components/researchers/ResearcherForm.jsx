import { useEffect, useState } from "react";
import PrimaryButton from "../ui/PrimaryButton";

const emptyForm = {
  fullName: "",
  email: "",
  phone: "",
  institution: "Universidade Metropolitana de Angola",
  department: "",
  academicTitle: "",
  orcidId: ""
};

export default function ResearcherForm({
  initialData,
  mode = "create",
  loading = false,
  onSubmit,
  onCancel
}) {
  const [formData, setFormData] = useState(emptyForm);

  useEffect(() => {
    if (initialData) {
      setFormData({
        fullName: initialData.fullName || "",
        email: initialData.email || "",
        phone: initialData.phone || "",
        institution:
          initialData.institution || "Universidade Metropolitana de Angola",
        department: initialData.department || "",
        academicTitle: initialData.academicTitle || "",
        orcidId: initialData.orcidId || ""
      });
    } else {
      setFormData(emptyForm);
    }
  }, [initialData]);

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
          {mode === "edit" ? "Editar pesquisador" : "Novo pesquisador"}
        </h3>

        <p className="mt-1 text-sm leading-6 text-slate-500">
          Preencha os dados principais do professor ou pesquisador vinculado ao
          IMETRO.
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="md:col-span-2">
          <label className="text-sm font-bold text-slate-700">
            Nome completo *
          </label>
          <input
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            required
            maxLength={180}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="Ex: Maria Esperança António"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">E-mail *</label>
          <input
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            required={mode === "create"}
            disabled={mode === "edit"}
            maxLength={180}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100 disabled:bg-slate-100 disabled:text-slate-500"
            placeholder="professor@imetro.ao"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">Telefone</label>
          <input
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            maxLength={50}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="+244 9XX XXX XXX"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">Instituição</label>
          <input
            name="institution"
            value={formData.institution}
            onChange={handleChange}
            maxLength={180}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="Universidade Metropolitana de Angola"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">Departamento</label>
          <input
            name="department"
            value={formData.department}
            onChange={handleChange}
            maxLength={180}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="Ex: Ciências da Saúde"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">
            Título acadêmico
          </label>
          <input
            name="academicTitle"
            value={formData.academicTitle}
            onChange={handleChange}
            maxLength={120}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="Ex: PhD, Mestre, Professor Auxiliar"
          />
        </div>

        <div>
          <label className="text-sm font-bold text-slate-700">ORCID ID</label>
          <input
            name="orcidId"
            value={formData.orcidId}
            onChange={handleChange}
            maxLength={50}
            className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
            placeholder="0000-0000-0000-0000"
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
          {mode === "edit" ? "Salvar alterações" : "Cadastrar pesquisador"}
        </PrimaryButton>
      </div>
    </form>
  );
}