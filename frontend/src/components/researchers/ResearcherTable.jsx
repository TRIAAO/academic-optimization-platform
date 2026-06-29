import { Edit, Eye, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";
import Badge from "../ui/Badge";
import PrimaryButton from "../ui/PrimaryButton";
import { formatDateTime } from "../../utils/formatters";

export default function ResearcherTable({ researchers, onEdit, onDelete }) {
  return (
    <div className="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-5 py-4 text-left text-xs font-black uppercase tracking-wide text-slate-500">
                Pesquisador
              </th>
              <th className="px-5 py-4 text-left text-xs font-black uppercase tracking-wide text-slate-500">
                Instituição
              </th>
              <th className="px-5 py-4 text-left text-xs font-black uppercase tracking-wide text-slate-500">
                ORCID
              </th>
              <th className="px-5 py-4 text-left text-xs font-black uppercase tracking-wide text-slate-500">
                Status
              </th>
              <th className="px-5 py-4 text-left text-xs font-black uppercase tracking-wide text-slate-500">
                Atualizado
              </th>
              <th className="px-5 py-4 text-right text-xs font-black uppercase tracking-wide text-slate-500">
                Ações
              </th>
            </tr>
          </thead>

          <tbody className="divide-y divide-slate-100 bg-white">
            {researchers.map((researcher) => (
              <tr key={researcher.id} className="hover:bg-slate-50">
                <td className="px-5 py-4">
                  <p className="font-bold text-slate-950">
                    {researcher.fullName}
                  </p>
                  <p className="mt-1 text-sm text-slate-500">
                    {researcher.email}
                  </p>
                  {researcher.academicTitle && (
                    <p className="mt-1 text-xs text-slate-400">
                      {researcher.academicTitle}
                    </p>
                  )}
                </td>

                <td className="px-5 py-4">
                  <p className="text-sm font-semibold text-slate-800">
                    {researcher.institution || "Não informada"}
                  </p>
                  <p className="mt-1 text-xs text-slate-500">
                    {researcher.department || "Sem departamento"}
                  </p>
                </td>

                <td className="px-5 py-4">
                  {researcher.orcidId ? (
                    <Badge variant="green">{researcher.orcidId}</Badge>
                  ) : (
                    <Badge variant="amber">Sem ORCID</Badge>
                  )}
                </td>

                <td className="px-5 py-4">
                  {researcher.active === false ? (
                    <Badge variant="red">Inativo</Badge>
                  ) : (
                    <Badge variant="green">Ativo</Badge>
                  )}
                </td>

                <td className="px-5 py-4 text-sm text-slate-500">
                  {formatDateTime(researcher.updatedAt || researcher.createdAt)}
                </td>

                <td className="px-5 py-4">
                  <div className="flex justify-end gap-2">
                    <Link to={`/admin/researchers/${researcher.id}`}>
                      <PrimaryButton variant="light" icon={Eye}>
                        Ver
                      </PrimaryButton>
                    </Link>

                    <PrimaryButton
                      variant="light"
                      icon={Edit}
                      onClick={() => onEdit(researcher)}
                    >
                      Editar
                    </PrimaryButton>

                    <PrimaryButton
                      variant="danger"
                      icon={Trash2}
                      onClick={() => onDelete(researcher)}
                    >
                      Remover
                    </PrimaryButton>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}