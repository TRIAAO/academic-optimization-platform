import { Menu, ShieldCheck, LogOut } from "lucide-react";
import { Outlet } from "react-router-dom";
import { useState } from "react";
import Sidebar from "../components/layout/Sidebar";
import { useAuth } from "../context/AuthContext";
import { APP_CONFIG } from "../config/app";
import { canViewTechnicalArea } from "../config/permissions";

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout } = useAuth();

  const isTechnicalUser = canViewTechnicalArea(user);

  return (
    <div className="min-h-screen bg-slate-100">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="min-h-screen lg:pl-72">
        <header className="fixed left-0 right-0 top-0 z-50 border-b border-slate-200 bg-white/95 shadow-sm backdrop-blur lg:left-72">
          <div className="flex min-h-20 items-center justify-between gap-3 px-4 py-3 sm:px-6 lg:px-8">
            <div className="flex min-w-0 items-center gap-3">
              <button
                type="button"
                onClick={() => setSidebarOpen(true)}
                className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-700 shadow-sm lg:hidden"
                aria-label="Abrir menu"
              >
                <Menu className="h-5 w-5" />
              </button>

              <div className="min-w-0">
                <p className="hidden text-xs font-black uppercase tracking-[0.28em] text-blue-700 md:block">
                  Instituto Superior Politécnico Metropolitano de Angola / IMETRO
                </p>

                <p className="block text-xs font-black uppercase tracking-[0.25em] text-blue-700 md:hidden">
                  IMETRO
                </p>

                <h1 className="mt-1 truncate text-base font-black leading-6 text-slate-950 sm:text-xl">
                  <span className="hidden sm:inline">
                    Plataforma de Otimização Acadêmica
                  </span>
                  <span className="sm:hidden">Painel Acadêmico</span>
                </h1>
              </div>
            </div>

            <div className="flex shrink-0 items-center gap-2">
              <div className="hidden items-center gap-3 rounded-2xl bg-slate-100 px-4 py-2 sm:flex">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-100 text-emerald-700">
                  <ShieldCheck className="h-5 w-5" />
                </div>

                <div className="leading-4">
                  <p className="max-w-32 truncate text-sm font-black text-slate-800">
                    {user?.fullName || user?.name || "Administrador"}
                  </p>
                  <p className="text-xs font-semibold uppercase text-slate-500">
                    {isTechnicalUser ? "TRIA" : "IMETRO"}
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={logout}
                className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-sm transition hover:bg-slate-800"
                aria-label="Sair"
                title="Sair"
              >
                <LogOut className="h-5 w-5" />
              </button>
            </div>
          </div>
        </header>

        <main className="min-w-0 px-4 pb-5 pt-24 sm:px-6 sm:pb-6 lg:px-8">
          <Outlet />
        </main>

        <footer className="px-4 pb-6 text-xs text-slate-500 sm:px-6 lg:px-8">
          Executado por {APP_CONFIG.executor}
        </footer>
      </div>
    </div>
  );
}