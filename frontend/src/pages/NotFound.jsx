import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="max-w-md rounded-3xl border border-slate-200 bg-white p-8 text-center shadow-sm">
        <p className="text-sm font-semibold uppercase tracking-[0.3em] text-blue-700">
          404
        </p>

        <h1 className="mt-4 text-2xl font-black text-slate-950">
          Página não encontrada
        </h1>

        <p className="mt-3 text-sm leading-6 text-slate-500">
          A rota solicitada ainda não existe ou será liberada nos próximos
          módulos do painel administrativo.
        </p>

        <Link
          to="/admin/dashboard"
          className="mt-6 inline-flex rounded-2xl bg-blue-700 px-5 py-3 text-sm font-bold text-white hover:bg-blue-800"
        >
          Voltar ao dashboard
        </Link>
      </div>
    </main>
  );
}