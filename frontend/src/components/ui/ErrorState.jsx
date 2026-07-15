import { AlertTriangle } from "lucide-react";

export default function ErrorState({ title = "Erro ao carregar", message }) {
  return (
    <div className="rounded-3xl border border-red-200 bg-red-50 p-6">
      <div className="flex gap-4">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-red-100 text-red-700">
          <AlertTriangle className="h-6 w-6" />
        </div>

        <div>
          <h3 className="font-bold text-red-950">{title}</h3>
          <p className="mt-2 text-sm leading-6 text-red-800">
            {message || "Não foi possível buscar os dados na API."}
          </p>
        </div>
      </div>
    </div>
  );
}