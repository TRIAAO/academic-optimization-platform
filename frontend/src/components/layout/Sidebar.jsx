import {
  Activity,
  BarChart3,
  BookOpenCheck,
  ClipboardCheck,
  Database,
  FileText,
  GraduationCap,
  History,
  LayoutDashboard,
  LibraryBig,
  Link2,
  Network,
  ShieldCheck,
  UserRoundSearch,
  X
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { APP_CONFIG } from "../../config/app";

const navigation = [
  {
    name: "Dashboard Institucional",
    href: "/admin/dashboard",
    icon: LayoutDashboard,
    enabled: true
  },
  {
    name: "Pesquisadores",
    href: "/admin/researchers",
    icon: UserRoundSearch,
    enabled: false
  },
  {
    name: "Perfil Acadêmico",
    href: "/admin/academic-profiles",
    icon: GraduationCap,
    enabled: false
  },
  {
    name: "ORCID",
    href: "/admin/orcid",
    icon: Link2,
    enabled: false
  },
  {
    name: "OpenAlex",
    href: "/admin/openalex",
    icon: Network,
    enabled: false
  },
  {
    name: "Crossref / DOI",
    href: "/admin/crossref",
    icon: LibraryBig,
    enabled: false
  },
  {
    name: "Relatórios",
    href: "/admin/reports",
    icon: FileText,
    enabled: false
  },
  {
    name: "Google Acadêmico",
    href: "/admin/google-scholar-checklist",
    icon: ClipboardCheck,
    enabled: false
  },
  {
    name: "Auditoria",
    href: "/admin/audit",
    icon: History,
    enabled: false
  },
  {
    name: "Status Operacional",
    href: "/admin/status",
    icon: Activity,
    enabled: false
  }
];

function SidebarContent({ onClose }) {
  return (
    <div className="flex h-full flex-col bg-slate-950 text-white">
      <div className="flex h-20 items-center justify-between border-b border-white/10 px-6">
        <div>
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-blue-600 shadow-lg shadow-blue-900/30">
              <BarChart3 className="h-6 w-6" />
            </div>

            <div>
              <p className="text-sm font-semibold leading-5">IMETRO</p>
              <p className="text-xs text-slate-400">Painel Acadêmico</p>
            </div>
          </div>
        </div>

        <button
          type="button"
          onClick={onClose}
          className="rounded-xl p-2 text-slate-400 hover:bg-white/10 hover:text-white lg:hidden"
          aria-label="Fechar menu"
        >
          <X className="h-5 w-5" />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-5">
        <nav className="space-y-1">
          {navigation.map((item) => {
            const Icon = item.icon;

            if (!item.enabled) {
              return (
                <div
                  key={item.name}
                  className="flex cursor-not-allowed items-center justify-between rounded-2xl px-3 py-3 text-sm text-slate-500"
                  title="Será implementado nos próximos módulos do frontend"
                >
                  <div className="flex items-center gap-3">
                    <Icon className="h-5 w-5" />
                    <span>{item.name}</span>
                  </div>

                  <span className="rounded-full bg-white/5 px-2 py-0.5 text-[10px] uppercase tracking-wide text-slate-500">
                    breve
                  </span>
                </div>
              );
            }

            return (
              <NavLink
                key={item.name}
                to={item.href}
                onClick={onClose}
                className={({ isActive }) =>
                  [
                    "flex items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium transition",
                    isActive
                      ? "bg-blue-600 text-white shadow-lg shadow-blue-950/40"
                      : "text-slate-300 hover:bg-white/10 hover:text-white"
                  ].join(" ")
                }
              >
                <Icon className="h-5 w-5" />
                <span>{item.name}</span>
              </NavLink>
            );
          })}
        </nav>
      </div>

      <div className="border-t border-white/10 p-5">
        <div className="rounded-2xl bg-white/5 p-4">
          <div className="flex items-start gap-3">
            <ShieldCheck className="mt-0.5 h-5 w-5 text-emerald-400" />

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
      </div>
    </div>
  );
}

export default function Sidebar({ open, onClose }) {
  return (
    <>
      <div className="hidden lg:fixed lg:inset-y-0 lg:z-40 lg:flex lg:w-72 lg:flex-col">
        <SidebarContent onClose={onClose} />
      </div>

      {open && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="absolute inset-0 bg-slate-950/70"
            onClick={onClose}
            aria-hidden="true"
          />

          <div className="absolute inset-y-0 left-0 w-80 max-w-[85vw]">
            <SidebarContent onClose={onClose} />
          </div>
        </div>
      )}
    </>
  );
}