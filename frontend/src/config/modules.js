import {
  Activity,
  BookOpenCheck,
  ClipboardCheck,
  FileText,
  GraduationCap,
  History,
  LayoutDashboard,
  LibraryBig,
  Link2,
  Network,
  ShieldCheck,
  UserRoundSearch
} from "lucide-react";

export const ADMIN_MODULES = [
  {
    key: "dashboard",
    name: "Dashboard Institucional",
    href: "/admin/dashboard",
    icon: LayoutDashboard,
    enabled: true,
    description: "Visão executiva da plataforma acadêmica."
  },
  {
    key: "researchers",
    name: "Pesquisadores",
    href: "/admin/researchers",
    icon: UserRoundSearch,
    enabled: true,
    description: "Cadastro, edição, busca e gestão de pesquisadores."
  },
  {
    key: "academic-profiles",
    name: "Perfil Acadêmico",
    href: "/admin/academic-profiles",
    icon: GraduationCap,
    enabled: true,
    description: "Gestão dos perfis acadêmicos vinculados aos pesquisadores."
  },
  {
    key: "orcid",
    name: "ORCID",
    href: "/admin/orcid",
    icon: Link2,
    enabled: true,
    description: "Importação, resumo público e sincronização ORCID."
  },
  {
    key: "openalex",
    name: "OpenAlex",
    href: "/admin/openalex",
    icon: Network,
    enabled: true,
    description: "Validação por ORCID, candidatos e obras encontradas."
  },
  {
    key: "manual-review",
    name: "Revisão Manual",
    href: "/admin/manual-review",
    icon: BookOpenCheck,
    enabled: true,
    description: "Confirmação manual de obras acadêmicas."
  },
  {
    key: "crossref",
    name: "Crossref / DOI",
    href: "/admin/crossref",
    icon: LibraryBig,
    enabled: true,
    description: "Validação DOI e metadados via Crossref."
  },
  {
    key: "reports",
    name: "Relatórios",
    href: "/admin/reports",
    icon: FileText,
    enabled: true,
    description: "Relatórios de otimização acadêmica e exportação PDF."
  },
  {
    key: "google-scholar",
    name: "Google Acadêmico",
    href: "/admin/google-scholar-checklist",
    icon: ClipboardCheck,
    enabled: true,
    description: "Checklist manual e orientação segura."
  },
  {
    key: "audit",
    name: "Auditoria",
    href: "/admin/audit",
    icon: History,
    enabled: true,
    description: "Eventos, rastreabilidade e histórico de ações."
  },
  {
    key: "status",
    name: "Status Operacional",
    href: "/admin/status",
    icon: Activity,
    enabled: true,
    description: "Saúde da API, banco, segurança e integrações."
  },
  {
    key: "openapi",
    name: "OpenAPI JSON",
    href: "/admin/openapi",
    icon: ShieldCheck,
    enabled: true,
    description: "Resumo do contrato OpenAPI em produção."
  }
];

export function getModuleByKey(key) {
  return ADMIN_MODULES.find((module) => module.key === key);
}