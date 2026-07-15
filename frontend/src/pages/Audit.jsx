import { useEffect, useMemo, useState } from "react";
import { History, RefreshCw, Search } from "lucide-react";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import ErrorState from "../components/ui/ErrorState";
import LoadingState from "../components/ui/LoadingState";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import { auditService } from "../services/auditService";
import { formatDateTime } from "../utils/formatters";

function pickEventTitle(event) {
  return (
    event.action ||
    event.eventType ||
    event.operation ||
    event.activity ||
    "Evento de auditoria"
  );
}

function pickEventUser(event) {
  return (
    event.username ||
    event.userEmail ||
    event.actor ||
    event.performedBy ||
    event.user ||
    "Usuário não informado"
  );
}

function pickEventDate(event) {
  return (
    event.createdAt ||
    event.timestamp ||
    event.performedAt ||
    event.auditAt ||
    event.updatedAt
  );
}

export default function Audit() {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const filteredEvents = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) return events;

    return events.filter((event) => {
      return JSON.stringify(event || {})
        .toLowerCase()
        .includes(normalizedQuery);
    });
  }, [events, query]);

  async function loadEvents() {
    setLoading(true);
    setError("");

    try {
      const data = await auditService.findAll();
      setEvents(data);
      setSelectedEvent(data[0] || null);
    } catch (apiError) {
      setError(apiError?.message || "Não foi possível carregar auditoria.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadEvents();
  }, []);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Rastreabilidade"
        title="Auditoria"
        description="Histórico de eventos, rastreabilidade de ações, recursos alterados e usuários envolvidos."
        actions={
          <PrimaryButton variant="light" icon={RefreshCw} onClick={loadEvents}>
            Atualizar
          </PrimaryButton>
        }
      >
        <div className="flex items-center rounded-2xl border border-slate-200 bg-slate-50 px-4">
          <Search className="h-5 w-5 text-slate-400" />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            className="w-full bg-transparent px-3 py-3 text-sm outline-none"
            placeholder="Buscar por usuário, ação, recurso, status ou data..."
          />
        </div>
      </PageHeader>

      {loading && <LoadingState message="Carregando eventos de auditoria..." />}

      {!loading && error && (
        <ErrorState title="Erro ao carregar auditoria" message={error} />
      )}

      {!loading && !error && filteredEvents.length === 0 && (
        <EmptyState
          icon={History}
          title="Nenhum evento de auditoria encontrado"
          description="Não há registros para o filtro informado ou a API ainda não retornou eventos."
        />
      )}

      {!loading && !error && filteredEvents.length > 0 && (
        <section className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
          <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="mb-5 flex items-center justify-between">
              <h3 className="font-black text-slate-950">Eventos</h3>
              <Badge variant="blue">{filteredEvents.length} eventos</Badge>
            </div>

            <div className="space-y-3">
              {filteredEvents.map((event, index) => (
                <button
                  key={event.id || event.auditId || index}
                  type="button"
                  onClick={() => setSelectedEvent(event)}
                  className="w-full rounded-2xl border border-slate-200 bg-slate-50 p-4 text-left transition hover:border-blue-300 hover:bg-blue-50"
                >
                  <div className="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
                    <div>
                      <p className="font-black text-slate-950">
                        {pickEventTitle(event)}
                      </p>

                      <p className="mt-1 text-sm text-slate-500">
                        {pickEventUser(event)}
                      </p>
                    </div>

                    <Badge variant="slate">
                      {formatDateTime(pickEventDate(event))}
                    </Badge>
                  </div>
                </button>
              ))}
            </div>
          </div>

          <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="font-black text-slate-950">Detalhe do evento</h3>

            {!selectedEvent ? (
              <p className="mt-3 text-sm text-slate-500">
                Selecione um evento para visualizar os detalhes.
              </p>
            ) : (
              <pre className="mt-5 max-h-[720px] overflow-auto whitespace-pre-wrap rounded-2xl bg-slate-950 p-5 text-xs leading-6 text-slate-100">
                {JSON.stringify(selectedEvent, null, 2)}
              </pre>
            )}
          </div>
        </section>
      )}
    </div>
  );
}