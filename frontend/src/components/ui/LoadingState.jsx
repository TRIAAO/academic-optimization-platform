import { Loader2 } from "lucide-react";

export default function LoadingState({ message = "Carregando informações..." }) {
  return (
    <div className="flex min-h-[320px] items-center justify-center rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
      <div className="text-center">
        <Loader2 className="mx-auto h-8 w-8 animate-spin text-blue-700" />
        <p className="mt-4 text-sm font-medium text-slate-600">{message}</p>
      </div>
    </div>
  );
}