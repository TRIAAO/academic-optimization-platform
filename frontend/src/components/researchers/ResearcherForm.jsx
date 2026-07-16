import { useEffect, useState } from "react";
import { CheckCircle2, Search } from "lucide-react";
import PrimaryButton from "../ui/PrimaryButton";
import { orcidService } from "../../services/orcidService";
import {
  formatOrcidInput,
  isValidOrcid,
  selectCurrentOrcidAffiliation
} from "../../utils/orcid";

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
  const [lookupLoading, setLookupLoading] = useState(false);
  const [lookupError, setLookupError] = useState("");
  const [lookupSummary, setLookupSummary] = useState(null);

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

    setLookupError("");
    setLookupSummary(null);
  }, [initialData]);

  function handleChange(event) {
    const { name, value } = event.target;

    const normalizedValue = name === "orcidId" ? formatOrcidInput(value) : value;

    setFormData((current) => ({
      ...current,
      [name]: normalizedValue
    }));

    if (name === "orcidId") {
      setLookupError("");
      setLookupSummary(null);
    }
  }

  async function handleOrcidLookup() {
    if (!isValidOrcid(formData.orcidId)) {
      setLookupError("Informe um ORCID válido com os 16 caracteres.");
      return;
    }

    setLookupLoading(true);
    setLookupError("");
    setLookupSummary(null);

    try {
      const summary = await orcidService.findSummaryByOrcidId(formData.orcidId);
      const affiliation = selectCurrentOrcidAffiliation(summary);

      setFormData((current) => ({
        ...current,
        orcidId: summary.orcidId || current.orcidId,
        fullName: summary.displayName || current.fullName,
        email: summary.primaryEmail || current.email,
        institution: affiliation?.organizationName || current.institution,
        department: affiliation?.departmentName || current.department,
        academicTitle: affiliation?.roleTitle || current.academicTitle
      }));
      setLookupSummary(summary);
    } catch (apiError) {
      setLookupError(apiError?.message || "Não foi possível consultar o ORCID informado.");
    } finally {
      setLookupLoading(false);
    }
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
          {mode === "create"
            ? "Comece pelo ORCID para preencher automaticamente os dados públicos disponíveis."
            : "Atualize os dados principais do professor ou pesquisador vinculado ao IMETRO."}
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {mode === "create" && (
          <div className="md:col-span-2 rounded-2xl border border-blue-200 bg-blue-50 p-4">
            <label htmlFor="researcher-orcid" className="text-sm font-black text-blue-950">
              ORCID ID
            </label>
            <p className="mt-1 text-xs leading-5 text-blue-800">
              Digite os 16 caracteres ou cole o link do perfil ORCID. Nenhum cadastro será criado antes da sua confirmação.
            </p>

            <div className="mt-3 flex flex-col gap-3 sm:flex-row">
              <input
                id="researcher-orcid"
                name="orcidId"
                value={formData.orcidId}
                onChange={handleChange}
                inputMode="text"
                autoComplete="off"
                maxLength={64}
                className="w-full rounded-2xl border border-blue-200 bg-white px-4 py-3 font-mono text-sm tracking-wide outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                placeholder="0000-0000-0000-0000"
              />

              <PrimaryButton
                variant="light"
                icon={Search}
                loading={lookupLoading}
                disabled={!isValidOrcid(formData.orcidId)}
                onClick={handleOrcidLookup}
              >
                Buscar dados
              </PrimaryButton>
            </div>

            {formData.orcidId && !isValidOrcid(formData.orcidId) && !lookupError && (
              <p className="mt-2 text-xs font-semibold text-amber-700">
                Continue digitando ou confira o dígito verificador.
              </p>
            )}

            {lookupError && (
              <p className="mt-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm font-semibold text-red-800">
                {lookupError}
              </p>
            )}

            {lookupSummary && (
              <div className="mt-3 flex items-start gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-3 text-emerald-900">
                <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" />
                <div>
                  <p className="text-sm font-black">
                    Dados públicos encontrados para {lookupSummary.displayName || lookupSummary.orcidId}.
                  </p>
                  <p className="mt-1 text-xs leading-5 text-emerald-800">
                    Revise os campos preenchidos antes de cadastrar.
                    {!lookupSummary.primaryEmail && " O e-mail não está público no ORCID e deve ser informado manualmente."}
                  </p>
                </div>
              </div>
            )}
          </div>
        )}

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

        {mode === "edit" && (
          <div>
            <label className="text-sm font-bold text-slate-700">ORCID ID</label>
            <input
              name="orcidId"
              value={formData.orcidId}
              onChange={handleChange}
              maxLength={64}
              className="mt-2 w-full rounded-2xl border border-slate-200 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
              placeholder="0000-0000-0000-0000"
            />
          </div>
        )}
      </div>

      <div className="mt-6 flex flex-wrap justify-end gap-3">
        {onCancel && (
          <PrimaryButton variant="light" onClick={onCancel}>
            Cancelar
          </PrimaryButton>
        )}

        <PrimaryButton type="submit" loading={loading} disabled={lookupLoading}>
          {mode === "edit" ? "Salvar alterações" : "Cadastrar pesquisador"}
        </PrimaryButton>
      </div>
    </form>
  );
}
