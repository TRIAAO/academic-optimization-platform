import { Edit, Eye, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";
import Badge from "../ui/Badge";
import { formatDateTime } from "../../utils/formatters";

function IconButton({
  icon: Icon,
  label,
  variant = "light",
  onClick,
  to,
  title
}) {
  const variants = {
    light:
      "border-slate-200 bg-white text-slate-700 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700",
    danger:
      "border-red-200 bg-red-600 text-white hover:border-red-700 hover:bg-red-700"
  };

  const content = (
    <span
      title={title || label}
      className={`inline-flex h-9 w-9 items-center justify-center rounded-xl border text-sm font-bold shadow-sm transition ${
        variants[variant] || variants.light
      }`}
    >
      <Icon className="h-4 w-4" />
    </span>
  );

  if (to) {
    return <Link to={to}>{content}</Link>;
  }

  return (
    <button type="button" onClick={onClick} aria-label={label}>
      {content}
    </button>
  );
}

function ResearcherCard({ researcher, onEdit, onDelete }) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <h3 className="truncate text-base font-black text-slate-950">
            {researcher.fullName}
          </h3>

          <p className="mt-1 truncate text-sm text-slate-500">
            {researcher.email}
          </p>

          {researcher.academicTitle && (
            <p className="mt-1 text-xs font-medium text-slate-400">
              {researcher.academicTitle}
            </p>
          )}
        </div>

        {researcher.active === false ? (
          <Badge variant="red">Inativo</Badge>
        ) : (
          <Badge variant="green">Ativo</Badge>
        )}
      </div>

      <div className="mt-5 grid gap-3 text-sm">
        <div className="rounded-2xl bg-slate-50 p-3">
          <p className="text-xs font-black uppercase tracking-wide text-slate-500">
            Instituição
          </p>

          <p className="mt-1 font-bold text-slate-900">
            {researcher.institution || "Não informada"}
          </p>

          <p className="mt-1 text-xs text-slate-500">
            {researcher.department || "Sem departamento"}
          </p>
        </div>

        <div className="rounded-2xl bg-slate-50 p-3">
          <p className="text-xs font-black uppercase tracking-wide text-slate-500">
            ORCID
          </p>

          <p className="mt-1 font-mono text-sm font-bold text-slate-900">
            {researcher.orcidId || "Não informado"}
          </p>
        </div>

        <div className="rounded-2xl bg-slate-50 p-3">
          <p className="text-xs font-black uppercase tracking-wide text-slate-500">
            Atualizado
          </p>

          <p className="mt-1 font-bold text-slate-900">
            {formatDateTime(researcher.updatedAt || researcher.createdAt)}
          </p>
        </div>
      </div>

      <div className="mt-5 flex justify-end gap-2">
        <IconButton
          to={`/admin/researchers/${researcher.id}`}
          icon={Eye}
          label="Ver"
          title="Ver detalhes"
        />

        <IconButton
          icon={Edit}
          label="Editar"
          title="Editar pesquisador"
          onClick={() => onEdit(researcher)}
        />

        <IconButton
          icon={Trash2}
          label="Remover"
          title="Remover pesquisador"
          variant="danger"
          onClick={() => onDelete(researcher)}
        />
      </div>
    </div>
  );
}

export default function ResearcherTable({ researchers, onEdit, onDelete }) {
  return (
    <>
      <div className="grid gap-4 lg:hidden">
        {researchers.map((researcher) => (
          <ResearcherCard
            key={researcher.id}
            researcher={researcher}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </div>

      <div className="hidden rounded-3xl border border-slate-200 bg-white shadow-sm lg:block">
        <div className="grid grid-cols-[minmax(220px,1.2fr)_minmax(240px,1fr)_190px_124px] items-center gap-4 border-b border-slate-200 bg-slate-50 px-5 py-4 text-xs font-black uppercase tracking-wide text-slate-500">
          <div>Pesquisador</div>
          <div>Instituição</div>
          <div>Dados acadêmicos</div>
          <div className="text-right">Ações</div>
        </div>

        <div className="divide-y divide-slate-100">
          {researchers.map((researcher) => (
            <div
              key={researcher.id}
              className="grid grid-cols-[minmax(220px,1.2fr)_minmax(240px,1fr)_190px_124px] items-center gap-4 px-5 py-5 transition hover:bg-slate-50"
            >
              <div className="min-w-0">
                <p className="truncate text-base font-black text-slate-950">
                  {researcher.fullName}
                </p>

                <p className="mt-1 truncate text-sm text-slate-500">
                  {researcher.email}
                </p>

                {researcher.academicTitle && (
                  <p className="mt-1 truncate text-xs text-slate-400">
                    {researcher.academicTitle}
                  </p>
                )}
              </div>

              <div className="min-w-0">
                <p className="line-clamp-2 text-sm font-bold leading-6 text-slate-900">
                  {researcher.institution || "Não informada"}
                </p>

                <p className="mt-1 line-clamp-2 text-xs leading-5 text-slate-500">
                  {researcher.department || "Sem departamento"}
                </p>
              </div>

              <div className="space-y-2">
                {researcher.orcidId ? (
                  <span className="inline-flex max-w-full rounded-xl bg-emerald-50 px-2.5 py-1.5 font-mono text-[11px] font-black leading-4 text-emerald-700 ring-1 ring-emerald-200">
                    <span className="truncate">{researcher.orcidId}</span>
                  </span>
                ) : (
                  <Badge variant="amber">Sem ORCID</Badge>
                )}

                <div>
                  {researcher.active === false ? (
                    <Badge variant="red">Inativo</Badge>
                  ) : (
                    <Badge variant="green">Ativo</Badge>
                  )}
                </div>

                <p className="text-xs leading-5 text-slate-500">
                  {formatDateTime(researcher.updatedAt || researcher.createdAt)}
                </p>
              </div>

              <div className="flex justify-end gap-2">
                <IconButton
                  to={`/admin/researchers/${researcher.id}`}
                  icon={Eye}
                  label="Ver"
                  title="Ver detalhes"
                />

                <IconButton
                  icon={Edit}
                  label="Editar"
                  title="Editar pesquisador"
                  onClick={() => onEdit(researcher)}
                />

                <IconButton
                  icon={Trash2}
                  label="Remover"
                  title="Remover pesquisador"
                  variant="danger"
                  onClick={() => onDelete(researcher)}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}