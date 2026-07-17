import {
  BarChart3,
  PanelLeftClose,
  PanelLeftOpen,
  ShieldCheck,
  X
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { APP_CONFIG } from "../../config/app";
import { ADMIN_MODULES } from "../../config/modules";

function SidebarContent({ onClose, collapsed = false, mobile = false }) {
  const visibleModules = ADMIN_MODULES.filter((item) => {
    return item.enabled && item.technical !== true;
  });

  const isCollapsed = collapsed && !mobile;

  return (
    <div className="flex h-full flex-col bg-slate-950 text-white">
      <div
        className={[
          "flex h-20 items-center border-b border-white/10 transition-all duration-300",
          isCollapsed ? "justify-center px-3" : "justify-between px-6"
        ].join(" ")}
      >
        <div
          className={[
            "flex min-w-0 items-center",
            isCollapsed ? "justify-center" : "gap-3"
          ].join(" ")}
        >
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-blue-600 shadow-lg shadow-blue-900/30">
            <BarChart3 className="h-6 w-6" />
          </div>

          {!isCollapsed && (
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold leading-5">IMETRO</p>
              <p className="truncate text-xs text-slate-400">Painel Acadêmico</p>
            </div>
          )}
        </div>

        {mobile && (
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl p-2 text-slate-400 transition hover:bg-white/10 hover:text-white focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-blue-500/25"
            aria-label="Fechar menu"
            title="Fechar menu"
          >
            <X className="h-5 w-5" />
          </button>
        )}
      </div>

      <div
        className={[
          "flex-1 overflow-y-auto py-5 transition-all duration-300",
          isCollapsed ? "px-2" : "px-4"
        ].join(" ")}
      >
        <nav className="space-y-1" aria-label="Navegação principal">
          {visibleModules.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.key}
                to={item.href}
                onClick={onClose}
                aria-label={item.name}
                title={isCollapsed ? item.name : undefined}
                className={({ isActive }) =>
                  [
                    "flex min-h-12 items-center rounded-2xl py-3 text-sm font-medium transition",
                    isCollapsed ? "justify-center px-2" : "gap-3 px-3",
                    isActive
                      ? "bg-blue-600 text-white shadow-lg shadow-blue-950/40"
                      : "text-slate-300 hover:bg-white/10 hover:text-white"
                  ].join(" ")
                }
              >
                <Icon className="h-5 w-5 shrink-0" aria-hidden="true" />
                {!isCollapsed && <span className="min-w-0 truncate">{item.name}</span>}
              </NavLink>
            );
          })}
        </nav>
      </div>

      <div
        className={[
          "border-t border-white/10 transition-all duration-300",
          isCollapsed ? "p-3" : "p-5"
        ].join(" ")}
      >
        {isCollapsed ? (
          <div
            className="flex h-11 w-full items-center justify-center rounded-2xl bg-white/5 text-emerald-400"
            aria-label="Regra de segurança do Google Acadêmico"
            title="Google Acadêmico apenas como checklist manual"
          >
            <ShieldCheck className="h-5 w-5" aria-hidden="true" />
          </div>
        ) : (
          <>
            <div className="rounded-2xl bg-white/5 p-4">
              <div className="flex items-start gap-3">
                <ShieldCheck className="mt-0.5 h-5 w-5 shrink-0 text-emerald-400" />

                <div>
                  <p className="text-xs font-semibold text-white">Regra de segurança</p>
                  <p className="mt-1 text-xs leading-5 text-slate-400">
                    Google Acadêmico apenas como checklist manual. Sem automação,
                    scraping ou alteração direta.
                  </p>
                </div>
              </div>
            </div>

            <p className="mt-4 text-xs text-slate-500">
              Executado por {APP_CONFIG.executor}
            </p>
          </>
        )}
      </div>
    </div>
  );
}

export default function Sidebar({
  open,
  onClose,
  collapsed = false,
  onToggleCollapsed
}) {
  const toggleLabel = collapsed ? "Expandir menu lateral" : "Recolher menu lateral";
  const ToggleIcon = collapsed ? PanelLeftOpen : PanelLeftClose;

  return (
    <>
      <aside
        className={[
          "hidden lg:fixed lg:inset-y-0 lg:z-40 lg:flex lg:flex-col lg:transition-[width] lg:duration-300",
          collapsed ? "lg:w-20" : "lg:w-72"
        ].join(" ")}
        aria-label="Menu lateral"
      >
        <SidebarContent onClose={onClose} collapsed={collapsed} />

        <button
          type="button"
          onClick={onToggleCollapsed}
          className="absolute -right-5 top-5 z-50 inline-flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-700 shadow-lg transition hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-blue-500/25 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200 dark:hover:border-blue-500 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          aria-label={toggleLabel}
          aria-pressed={collapsed}
          title={toggleLabel}
        >
          <ToggleIcon className="h-4 w-4" aria-hidden="true" />
        </button>
      </aside>

      {open && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="absolute inset-0 bg-slate-950/70"
            onClick={onClose}
            aria-hidden="true"
          />

          <div className="absolute inset-y-0 left-0 w-80 max-w-[85vw]">
            <SidebarContent onClose={onClose} mobile />
          </div>
        </div>
      )}
    </>
  );
}
