import {
  Activity,
  BarChart3,
  BookMarked,
  BrainCircuit,
  BookOpenCheck,
  ClipboardCheck,
  CopyCheck,
  FileText,
  GraduationCap,
  History,
  LayoutDashboard,
  LibraryBig,
  Link2,
  Languages,
  Network,
  Scale,
  Sparkles,
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
    technical: false,
    description: "Visão executiva da plataforma acadêmica."
  },
  {
    key: "researchers",
    name: "Pesquisadores",
    href: "/admin/researchers",
    icon: UserRoundSearch,
    enabled: true,
    technical: false,
    description: "Cadastro, edição, busca e gestão de pesquisadores."
  },
  {
    key: "academic-profiles",
    name: "Perfil Acadêmico",
    href: "/admin/academic-profiles",
    icon: GraduationCap,
    enabled: true,
    technical: false,
    description: "Gestão dos perfis acadêmicos vinculados aos pesquisadores."
  },
  {
    key: "orcid",
    name: "ORCID",
    href: "/admin/orcid",
    icon: Link2,
    enabled: true,
    technical: false,
    description: "Importação, resumo público e sincronização ORCID."
  },
  {
    key: "openalex",
    name: "OpenAlex",
    href: "/admin/openalex",
    icon: Network,
    enabled: true,
    technical: false,
    description: "Validação por ORCID, candidatos e obras encontradas."
  },
  {
    key: "manual-review",
    name: "Revisão Manual",
    href: "/admin/manual-review",
    icon: BookOpenCheck,
    enabled: true,
    technical: false,
    description: "Confirmação manual de obras acadêmicas."
  },
  {
    key: "bibliographic-deduplication",
    name: "Deduplicação Bibliográfica",
    href: "/admin/bibliographic-deduplication",
    icon: CopyCheck,
    enabled: true,
    technical: false,
    description: "Comparação aproximada ORCID × OpenAlex com confirmação humana."
  },
  {
    key: "crossref",
    name: "Crossref / DOI",
    href: "/admin/crossref",
    icon: LibraryBig,
    enabled: true,
    technical: false,
    description: "Validação DOI e metadados via Crossref."
  },
  {
    key: "optimization",
    name: "Otimização Acadêmica",
    href: "/admin/optimization",
    icon: Sparkles,
    enabled: true,
    technical: false,
    description: "Score, recomendações, alertas éticos e plano de ação."
  },
  {
    key: "academic-recommendations",
    name: "Recomendações Inteligentes",
    href: "/admin/recommendations",
    icon: BrainCircuit,
    enabled: true,
    technical: false,
    description: "Temas, colaboradores e veículos científicos baseados em evidências."
  },
  {
    key: "abstract-analysis",
    name: "Análise de Abstracts",
    href: "/admin/abstract-analysis",
    icon: Languages,
    enabled: true,
    technical: false,
    description: "Cobertura PT–EN e temas recorrentes dos abstracts confirmados."
  },
  {
    key: "editorial-recommendations",
    name: "Direcionamento Editorial",
    href: "/admin/editorial-recommendations",
    icon: BookMarked,
    enabled: true,
    technical: false,
    description: "Matching explicável entre abstracts e periódicos relacionados."
  },
  {
    key: "reports",
    name: "Relatórios",
    href: "/admin/reports",
    icon: FileText,
    enabled: true,
    technical: false,
    description: "Relatórios de otimização acadêmica e exportação PDF."
  },
  {
    key: "scientometric-metrics",
    name: "Métricas Cientométricas",
    href: "/admin/scientometric-metrics",
    icon: BarChart3,
    enabled: true,
    technical: false,
    description: "Citações, H-index, i10-index, D-index e histórico manual."
  },
  {
    key: "scientometric-analysis",
    name: "Auditoria Cientométrica",
    href: "/admin/scientometric-analysis",
    icon: Scale,
    enabled: true,
    technical: false,
    description: "D-index, vitalidade científica e validação institucional do e-mail."
  },
  {
    key: "google-scholar",
    name: "Google Acadêmico",
    href: "/admin/google-scholar-checklist",
    icon: ClipboardCheck,
    enabled: true,
    technical: false,
    description: "Checklist manual e orientação segura."
  },

  /*
   * Área técnica TRIA Company.
   * Não deve aparecer para cliente institucional.
   */
  {
    key: "audit",
    name: "Auditoria",
    href: "/admin/audit",
    icon: History,
    enabled: true,
    technical: true,
    description: "Eventos, rastreabilidade e histórico de ações."
  },
  {
    key: "status",
    name: "Status Operacional",
    href: "/admin/status",
    icon: Activity,
    enabled: true,
    technical: true,
    description: "Saúde da API, banco, segurança e integrações."
  },
  {
    key: "openapi",
    name: "OpenAPI JSON",
    href: "/admin/openapi",
    icon: ShieldCheck,
    enabled: true,
    technical: true,
    description: "Resumo do contrato OpenAPI em produção."
  }
];

export function getModuleByKey(key) {
  return ADMIN_MODULES.find((module) => module.key === key);
}
