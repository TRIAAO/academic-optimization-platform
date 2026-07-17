import {
  BadgeCheck,
  ExternalLink,
  ShieldCheck,
  Unlink
} from "lucide-react";
import { useEffect, useState } from "react";
import Badge from "../ui/Badge";
import PrimaryButton from "../ui/PrimaryButton";
import { orcidService } from "../../services/orcidService";
import { formatDateTime } from "../../utils/formatters";

export default function OrcidOAuthPanel({ researcherId, onConnectionChanged }) {
  const [configuration, setConfiguration] = useState(null);
  const [connection, setConnection] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState("");

  async function loadConnection() {
    if (!researcherId) return;

    setLoading(true);
    setError("");

    try {
      const [configurationData, connectionData] = await Promise.all([
        orcidService.getOAuthConfiguration(),
        orcidService.getOAuthConnection(researcherId)
      ]);

      setConfiguration(configurationData);
      setConnection(connectionData);
    } catch (apiError) {
      setError(
        apiError?.message ||
          "Não foi possível consultar a conexão autenticada com o ORCID."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadConnection();
  }, [researcherId]);

  async function handleConnect() {
    setActionLoading(true);
    setError("");

    try {
      const authorization =
        await orcidService.createOAuthAuthorization(researcherId);

      if (!authorization?.authorizationUrl) {
        throw new Error("O endereço de autorização ORCID não foi devolvido.");
      }

      window.location.assign(authorization.authorizationUrl);
    } catch (apiError) {
      setError(
        apiError?.message ||
          "Não foi possível iniciar a autenticação com o ORCID."
      );
      setActionLoading(false);
    }
  }

  async function handleDisconnect() {
    const confirmed = window.confirm(
      "Deseja revogar a autorização OAuth deste pesquisador? O identificador ORCID permanecerá no cadastro, mas os tokens de acesso serão removidos."
    );

    if (!confirmed) return;

    setActionLoading(true);
    setError("");

    try {
      const connectionData = await orcidService.disconnectOAuth(researcherId);
      setConnection(connectionData);
      onConnectionChanged?.(connectionData);
    } catch (apiError) {
      setError(
        apiError?.message ||
          "Não foi possível revogar a autorização OAuth ORCID."
      );
    } finally {
      setActionLoading(false);
    }
  }

  if (loading) {
    return (
      <div className="mt-5 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-500">
        Verificando conexão autenticada com o ORCID...
      </div>
    );
  }

  const connected = Boolean(connection?.connected);
  const enabled = Boolean(configuration?.enabled);

  return (
    <div className="mt-5 min-w-0 rounded-2xl border border-slate-200 bg-slate-50 p-4">
      <div className="flex min-w-0 flex-col gap-4 2xl:flex-row 2xl:items-start 2xl:justify-between">
        <div className="flex min-w-0 items-start gap-3">
          <div
            className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${
              connected
                ? "bg-emerald-100 text-emerald-700"
                : "bg-blue-100 text-blue-700"
            }`}
          >
            {connected ? (
              <BadgeCheck className="h-5 w-5" />
            ) : (
              <ShieldCheck className="h-5 w-5" />
            )}
          </div>

          <div className="min-w-0 flex-1">
            <div className="flex min-w-0 flex-wrap items-center gap-2">
              <p className="min-w-0 break-words font-black text-slate-950">
                Conexão autenticada ORCID
              </p>
              {connected ? (
                <Badge variant="green">Conectado</Badge>
              ) : enabled ? (
                <Badge variant="blue">Disponível</Badge>
              ) : (
                <Badge variant="amber">Configuração pendente</Badge>
              )}
            </div>

            {connected ? (
              <div className="mt-2 space-y-1 break-words text-sm text-slate-600">
                <p>
                  ORCID autenticado: <strong>{connection.orcidId}</strong>
                </p>
                {connection.authenticatedName && (
                  <p>Identidade confirmada: {connection.authenticatedName}</p>
                )}
                <p>
                  Consentimento registado em {formatDateTime(connection.connectedAt)}.
                </p>
              </div>
            ) : (
              <p className="mt-2 break-words text-sm leading-6 text-slate-600">
                {configuration?.message ||
                  "Autentique o investigador no ORCID para obter um identificador validado, sem digitação manual."}
              </p>
            )}
          </div>
        </div>

        <div className="flex w-full min-w-0 flex-wrap gap-2 2xl:w-auto 2xl:shrink-0">
          {connected ? (
            <PrimaryButton
              variant="danger"
              icon={Unlink}
              loading={actionLoading}
              onClick={handleDisconnect}
            >
              Desconectar
            </PrimaryButton>
          ) : (
            <PrimaryButton
              icon={ExternalLink}
              loading={actionLoading}
              disabled={!enabled}
              onClick={handleConnect}
            >
              Conectar com ORCID
            </PrimaryButton>
          )}
        </div>
      </div>

      {error && (
        <p className="mt-3 break-words rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-red-700">
          {error}
        </p>
      )}

      <p className="mt-3 break-words text-xs leading-5 text-slate-500">
        O fluxo usa OAuth 2.0 com consentimento explícito. A plataforma armazena os
        tokens de forma cifrada e não solicita a senha ORCID do pesquisador.
      </p>
    </div>
  );
}
