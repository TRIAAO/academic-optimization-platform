import { Save, ShieldCheck, X } from "lucide-react";
import { useEffect, useState } from "react";
import PrimaryButton from "../ui/PrimaryButton";

function today() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

const EMPTY_FORM = {
  source: "MANUAL_GOOGLE_SCHOLAR",
  googleScholarAuthorId: "",
  googleScholarProfileUrl: "",
  hIndexTotal: "",
  hIndexLastSixYears: "",
  i10IndexTotal: "",
  i10IndexLastSixYears: "",
  citationsTotal: "",
  citationsLastSixYears: "",
  dIndex: "",
  verifiedEmail: "",
  institutionalEmailVerified: false,
  interests: "",
  notes: "",
  snapshotDate: today()
};

function buildInitialData(initialData) {
  if (!initialData) {
    return { ...EMPTY_FORM, snapshotDate: today() };
  }

  return {
    source: initialData.source || "MANUAL_GOOGLE_SCHOLAR",
    googleScholarAuthorId: initialData.googleScholarAuthorId || "",
    googleScholarProfileUrl: initialData.googleScholarProfileUrl || "",
    hIndexTotal: initialData.hIndexTotal ?? "",
    hIndexLastSixYears: initialData.hIndexLastSixYears ?? "",
    i10IndexTotal: initialData.i10IndexTotal ?? "",
    i10IndexLastSixYears: initialData.i10IndexLastSixYears ?? "",
    citationsTotal: initialData.citationsTotal ?? "",
    citationsLastSixYears: initialData.citationsLastSixYears ?? "",
    dIndex: initialData.dIndex ?? "",
    verifiedEmail: initialData.verifiedEmail || "",
    institutionalEmailVerified: Boolean(
      initialData.institutionalEmailVerified
    ),
    interests: initialData.interests || "",
    notes: initialData.notes || "",
    snapshotDate: initialData.snapshotDate || today()
  };
}

function Field({ label, hint, children }) {
  return (
    <label className="block">
      <span className="text-sm font-bold text-slate-700">{label}</span>
      {hint && <span className="ml-2 text-xs text-slate-400">{hint}</span>}
      <div className="mt-2">{children}</div>
    </label>
  );
}

const inputClassName =
  "w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-4 focus:ring-blue-100";

export default function ScientometricMetricForm({
  researcher,
  initialData,
  loading,
  onSubmit,
  onCancel
}) {
  const [formData, setFormData] = useState(() => buildInitialData(initialData));

  useEffect(() => {
    setFormData(buildInitialData(initialData));
  }, [initialData]);

  function updateField(event) {
    const { name, value, type, checked } = event.target;

    setFormData((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value
    }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit(formData);
  }

  const editing = Boolean(initialData?.id);

  return (
    <form
      onSubmit={handleSubmit}
      className="overflow-hidden rounded-3xl border border-blue-100 bg-white shadow-sm"
    >
      <div className="flex flex-col gap-4 border-b border-slate-200 bg-slate-50 p-5 sm:flex-row sm:items-start sm:justify-between sm:p-6">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.22em] text-blue-700">
            {editing ? "Editar medição" : "Nova medição manual"}
          </p>
          <h3 className="mt-2 text-xl font-black text-slate-950">
            {researcher?.fullName || "Pesquisador selecionado"}
          </h3>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            Registe os indicadores exatamente como foram conferidos na fonte
            pública ou institucional.
          </p>
        </div>

        <button
          type="button"
          onClick={onCancel}
          className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 transition hover:bg-slate-100 hover:text-slate-950"
          aria-label="Fechar formulário"
        >
          <X className="h-5 w-5" />
        </button>
      </div>

      <div className="space-y-7 p-5 sm:p-6">
        <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4">
          <div className="flex items-start gap-3">
            <ShieldCheck className="mt-0.5 h-5 w-5 shrink-0 text-amber-700" />
            <p className="text-sm leading-6 text-amber-900">
              Este registo é manual. A plataforma não acessa, não coleta e não
              altera dados diretamente no Google Acadêmico.
            </p>
          </div>
        </div>

        <section>
          <h4 className="font-black text-slate-950">Identificação da medição</h4>
          <div className="mt-4 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <Field label="Fonte">
              <select
                name="source"
                value={formData.source}
                onChange={updateField}
                className={inputClassName}
              >
                <option value="MANUAL_GOOGLE_SCHOLAR">
                  Google Acadêmico — registo manual
                </option>
                <option value="MANUAL_INSTITUTIONAL">
                  Fonte institucional — registo manual
                </option>
                <option value="OTHER_MANUAL">Outra fonte — registo manual</option>
              </select>
            </Field>

            <Field label="Data da medição">
              <input
                required
                type="date"
                name="snapshotDate"
                value={formData.snapshotDate}
                max={today()}
                onChange={updateField}
                className={inputClassName}
              />
            </Field>

            <Field label="Author ID" hint="Google Acadêmico">
              <input
                name="googleScholarAuthorId"
                value={formData.googleScholarAuthorId}
                onChange={updateField}
                maxLength={120}
                placeholder="Ex.: AbCdEfGhIjK"
                className={inputClassName}
              />
            </Field>

            <div className="md:col-span-2 xl:col-span-3">
              <Field label="URL pública do perfil" hint="Opcional">
                <input
                  type="url"
                  name="googleScholarProfileUrl"
                  value={formData.googleScholarProfileUrl}
                  onChange={updateField}
                  maxLength={500}
                  placeholder="https://scholar.google.com/citations?user=..."
                  className={inputClassName}
                />
              </Field>
            </div>
          </div>
        </section>

        <section>
          <h4 className="font-black text-slate-950">Indicadores cientométricos</h4>
          <p className="mt-1 text-sm text-slate-500">
            Use apenas números inteiros iguais ou superiores a zero.
          </p>

          <div className="mt-4 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <Field label="Citações — total">
              <input
                type="number"
                min="0"
                step="1"
                name="citationsTotal"
                value={formData.citationsTotal}
                onChange={updateField}
                placeholder="0"
                className={inputClassName}
              />
            </Field>

            <Field label="Citações — últimos 6 anos">
              <input
                type="number"
                min="0"
                step="1"
                name="citationsLastSixYears"
                value={formData.citationsLastSixYears}
                onChange={updateField}
                placeholder="0"
                className={inputClassName}
              />
            </Field>

            <Field label="H-index — total">
              <input
                type="number"
                min="0"
                step="1"
                name="hIndexTotal"
                value={formData.hIndexTotal}
                onChange={updateField}
                placeholder="0"
                className={inputClassName}
              />
            </Field>

            <Field label="H-index — últimos 6 anos">
              <input
                type="number"
                min="0"
                step="1"
                name="hIndexLastSixYears"
                value={formData.hIndexLastSixYears}
                onChange={updateField}
                placeholder="0"
                className={inputClassName}
              />
            </Field>

            <Field label="i10-index — total">
              <input
                type="number"
                min="0"
                step="1"
                name="i10IndexTotal"
                value={formData.i10IndexTotal}
                onChange={updateField}
                placeholder="0"
                className={inputClassName}
              />
            </Field>

            <Field label="i10-index — últimos 6 anos">
              <input
                type="number"
                min="0"
                step="1"
                name="i10IndexLastSixYears"
                value={formData.i10IndexLastSixYears}
                onChange={updateField}
                placeholder="0"
                className={inputClassName}
              />
            </Field>

            <Field label="D-index">
              <input
                type="number"
                min="0"
                step="1"
                name="dIndex"
                value={formData.dIndex}
                onChange={updateField}
                placeholder="0"
                className={inputClassName}
              />
            </Field>
          </div>
        </section>

        <section>
          <h4 className="font-black text-slate-950">Validação e contexto</h4>
          <div className="mt-4 grid gap-4 md:grid-cols-2">
            <Field label="E-mail verificado" hint="Opcional">
              <input
                type="email"
                name="verifiedEmail"
                value={formData.verifiedEmail}
                onChange={updateField}
                maxLength={180}
                placeholder="nome@imetroangola.com"
                className={inputClassName}
              />
            </Field>

            <label className="flex min-h-[50px] items-center gap-3 self-end rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
              <input
                type="checkbox"
                name="institutionalEmailVerified"
                checked={formData.institutionalEmailVerified}
                onChange={updateField}
                className="h-5 w-5 rounded border-slate-300 text-blue-700 focus:ring-blue-600"
              />
              <span className="text-sm font-bold text-slate-700">
                E-mail institucional verificado
              </span>
            </label>

            <Field label="Áreas de interesse" hint="Uma linha ou lista curta">
              <textarea
                name="interests"
                value={formData.interests}
                onChange={updateField}
                rows={4}
                placeholder="Ex.: Engenharia, saúde pública, inteligência artificial..."
                className={inputClassName}
              />
            </Field>

            <Field label="Notas da conferência" hint="Uso institucional">
              <textarea
                name="notes"
                value={formData.notes}
                onChange={updateField}
                rows={4}
                placeholder="Registe a origem e qualquer observação relevante..."
                className={inputClassName}
              />
            </Field>
          </div>
        </section>
      </div>

      <div className="flex flex-col-reverse gap-3 border-t border-slate-200 bg-slate-50 p-5 sm:flex-row sm:justify-end sm:p-6">
        <PrimaryButton variant="light" onClick={onCancel}>
          Cancelar
        </PrimaryButton>
        <PrimaryButton type="submit" icon={Save} loading={loading}>
          {editing ? "Guardar alterações" : "Registar medição"}
        </PrimaryButton>
      </div>
    </form>
  );
}
