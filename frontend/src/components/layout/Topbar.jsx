import { LogOut, Menu, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { APP_CONFIG } from "../../config/app";
import { useAuth } from "../../context/AuthContext";

export default function Topbar({ onOpenSidebar }) {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="flex h-20 items-center justify-between px-4 sm:px-6 lg:px-8">
        <div className="flex items-center gap-4">
          <button
            type="button"
            onClick={onOpenSidebar}
            className="rounded-2xl border border-slate-200 bg-white p-2 text-slate-700 shadow-sm hover:bg-slate-50 lg:hidden"
            aria-label="Abrir menu"
          >
            <Menu className="h-5 w-5" />
          </button>

          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.25em] text-blue-700">
              {APP_CONFIG.organization}
            </p>
            <h1 className="mt-1 text-lg font-bold text-slate-950 sm:text-xl">
              {APP_CONFIG.name}
            </h1>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <a
            href={APP_CONFIG.openApiUrl}
            target="_blank"
            rel="noreferrer"
            className="hidden rounded-2xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 shadow-sm hover:bg-slate-50 md:inline-flex"
          >
            OpenAPI JSON
          </a>

          <div className="hidden items-center gap-3 rounded-2xl bg-slate-100 px-4 py-2 md:flex">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-emerald-100 text-emerald-700">
              <ShieldCheck className="h-5 w-5" />
            </div>

            <div>
              <p className="text-sm font-semibold text-slate-900">
                {user?.name || "Administrador"}
              </p>
              <p className="text-xs text-slate-500">{user?.role || "ADMIN"}</p>
            </div>
          </div>

          <button
            type="button"
            onClick={handleLogout}
            className="rounded-2xl bg-slate-950 p-2.5 text-white shadow-sm hover:bg-slate-800"
            aria-label="Sair"
          >
            <LogOut className="h-5 w-5" />
          </button>
        </div>
      </div>
    </header>
  );
}