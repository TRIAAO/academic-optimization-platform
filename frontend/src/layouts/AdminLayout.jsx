import { Menu, ShieldCheck, LogOut } from "lucide-react";
import { Outlet } from "react-router-dom";
import { useState } from "react";
import Sidebar from "../components/layout/Sidebar";
import ThemeToggle from "../components/ui/ThemeToggle";
import { useAuth } from "../context/AuthContext";
import { APP_CONFIG } from "../config/app";
import { canViewTechnicalArea } from "../config/permissions";

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout } = useAuth();

  const isTechnicalUser = canViewTechnicalArea(user);

  return (
    <div className="min-h-screen bg-slate-100 text-slate-950 dark:bg-slate-900 dark:text-slate-100">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="min-h-screen lg:pl-72">
        <header className="fixed left-0 right-0 top-0 z-50 border-b border-slate-200 bg-white/95 shadow-sm backdrop-blur dark:border-slate-800 dark:bg-slate-950/95 lg:left-72">
          <div className="flex min-h-20 items-center justify-between gap-3 px-4 py-3 sm:px-6 lg:px-8">
            <div className="flex min-w-0 items-center gap-3">
              <button
                type="button"
                onClick={() => setSidebarOpen(true)}
                className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-700 shadow-sm transition hover:border-blue-300 hover:text-blue-700 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-blue-500/25 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:border-blue-500 dark:hover:text-blue-300 lg:hidden"
                aria-label="Abrir menu"
              >
                <Menu className="h-5 w-5" />
              </button>

              <div className="min-w-0">
                <p className="hidden text-xs font-black uppercase tracking-[0.28em] text-blue-700 dark:text-blue-300 md:block">
                  Instituto Superior Politécnico Metropolitano de Angola / IMETRO
                </p>

                <p className="block text-xs font-black uppercase tracking-[0.25em] text-blue-700 dark:text-blue-300 md:hidden">
                  IMETRO
                </p>

                <h1 className="mt-1 truncate text-base font-extrabold leading-6 text-slate-950 dark:text-white sm:text-xl">
                  <span className="hidden sm:inline">
                    Plataforma de Otimização Acadêmica
                  </span>
                  <span className="sm:hidden">Painel Acadêmico</span>
                </h1>
              </div>
            </div>

            <div className="flex shrink-0 items-center gap-2">
              <ThemeToggle />

              <div className="hidden items-center gap-3 rounded-2xl bg-slate-100 px-4 py-2 dark:bg-slate-900 sm:flex">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-300">
                  <ShieldCheck className="h-5 w-5" />
                </div>

                <div className="leading-4">
                  <p className="max-w-32 truncate text-sm font-bold text-slate-800 dark:text-slate-100">
                    {user?.fullName || user?.name || "Administrador"}
                  </p>
                  <p className="text-xs font-semibold uppercase text-slate-500 dark:text-slate-400">
                    {isTechnicalUser ? "TRIA" : "IMETRO"}
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={logout}
                className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-slate-950 text-white shadow-sm transition hover:bg-slate-800 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-blue-500/25 dark:bg-blue-600 dark:hover:bg-blue-500"
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

        <footer className="px-4 pb-6 text-xs text-slate-500 dark:text-slate-400 sm:px-6 lg:px-8">
          Executado por {APP_CONFIG.executor}
        </footer>
      </div>
    </div>
  );
}
