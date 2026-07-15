import { Inbox } from "lucide-react";

export default function EmptyState({
  icon: Icon = Inbox,
  title = "Nenhum registro encontrado",
  description = "Ainda não existem informações para exibir neste módulo.",
  action
}) {
  return (
    <div className="rounded-3xl border border-dashed border-slate-300 bg-white p-10 text-center">
      <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-slate-500">
        <Icon className="h-7 w-7" />
      </div>

      <h3 className="mt-5 text-lg font-bold text-slate-950">{title}</h3>

      <p className="mx-auto mt-2 max-w-xl text-sm leading-7 text-slate-500">
        {description}
      </p>

      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}