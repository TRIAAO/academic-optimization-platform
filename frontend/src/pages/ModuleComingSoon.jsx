import { Construction, ExternalLink } from "lucide-react";
import { Link } from "react-router-dom";
import EmptyState from "../components/ui/EmptyState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { APP_CONFIG } from "../config/app";
import { getModuleByKey } from "../config/modules";

export default function ModuleComingSoon({ moduleKey }) {
  const module = getModuleByKey(moduleKey);
  const Icon = module?.icon || Construction;

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Módulo preparado"
        title={module?.name || "Módulo em preparação"}
        description={
          module?.description ||
          "Este módulo será conectado aos endpoints da API nos próximos pacotes."
        }
        actions={
          <a href={APP_CONFIG.openApiUrl} target="_blank" rel="noreferrer">
            <PrimaryButton variant="light" icon={ExternalLink}>
              Ver OpenAPI
            </PrimaryButton>
          </a>
        }
      />

      <EmptyState
        icon={Icon}
        title={`${module?.name || "Módulo"} pronto para implementação`}
        description="A rota, o layout e a navegação já estão preparados. No próximo pacote vamos conectar esta tela aos endpoints reais da API, com listagem, formulários, ações e tratamento de erro."
        action={
          <Link to="/admin/dashboard">
            <PrimaryButton variant="dark">Voltar ao dashboard</PrimaryButton>
          </Link>
        }
      />
    </div>
  );
}