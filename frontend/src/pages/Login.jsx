import { useState } from "react";
import { AlertCircle, ArrowRight, LockKeyhole, Mail, ShieldCheck } from "lucide-react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import ThemeToggle from "../components/ui/ThemeToggle";
import { APP_CONFIG } from "../config/app";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, loading, isAuthenticated } = useAuth();

  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });

  const [error, setError] = useState("");

  const redirectTo = location.state?.from?.pathname || "/admin/dashboard";

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((current) => ({
      ...current,
      [name]: value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    try {
      await login(formData);
      navigate(redirectTo, { replace: true });
    } catch (apiError) {
      setError(
        apiError?.message ||
          "Não foi possível autenticar. Verifique o e-mail e a senha."
      );
    }
  }

  return (
    <main className="relative min-h-screen bg-slate-950">
      <div className="absolute right-4 top-4 z-30 sm:right-6 sm:top-6">
        <ThemeToggle className="border-white/15 bg-white/10 text-white backdrop-blur hover:border-white/30 hover:bg-white/15 hover:text-white dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100" />
      </div>

      <div className="grid min-h-screen lg:grid-cols-[1.1fr_0.9fr]">
        <section className="relative hidden overflow-hidden px-12 py-12 text-white lg:flex lg:flex-col lg:justify-between">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(37,99,235,0.45),_transparent_35%),radial-gradient(circle_at_bottom_right,_rgba(16,185,129,0.22),_transparent_30%)]" />

          <div className="relative z-10">
            <div className="inline-flex items-center gap-3 rounded-full border border-white/15 bg-white/10 px-4 py-2 text-sm text-blue-100 backdrop-blur">
              <ShieldCheck className="h-4 w-4" />
              Backend MVP validado em produção
            </div>

            <h1 className="mt-10 max-w-3xl text-5xl font-extrabold leading-tight tracking-tight">
              Painel Administrativo da Plataforma de Otimização Acadêmica.
            </h1>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300">
              Gestão institucional de pesquisadores, perfis acadêmicos, ORCID,
              OpenAlex, Crossref, relatórios, auditoria e status operacional para
              a {APP_CONFIG.organization}.
            </p>
          </div>

          <div className="relative z-10 rounded-3xl border border-white/10 bg-white/10 p-6 backdrop-blur">
            <p className="text-sm font-semibold text-white">
              Regra fixa do módulo Google Acadêmico
            </p>
            <p className="mt-3 text-sm leading-7 text-slate-300">
              {APP_CONFIG.googleScholarPolicy}
            </p>
          </div>
        </section>

        <section className="flex items-center justify-center bg-slate-100 px-4 py-20 dark:bg-slate-900 sm:px-6 lg:px-10 lg:py-10">
          <div className="w-full max-w-md">
            <div className="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-2xl shadow-slate-950/10 dark:border-slate-700 dark:bg-slate-800 dark:shadow-black/30">
              <div className="mb-8">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-600 text-white shadow-lg shadow-blue-700/30">
                  <ShieldCheck className="h-7 w-7" />
                </div>

                <h2 className="mt-6 text-2xl font-extrabold text-slate-950 dark:text-white">
                  Acesso administrativo
                </h2>

                <p className="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400">
                  Entre com o usuário administrador criado no backend.
                </p>
              </div>

              {error && (
                <div className="mb-6 flex gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-800 dark:border-red-500/30 dark:bg-red-500/10 dark:text-red-200">
                  <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />
                  <p>{error}</p>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                  <label
                    htmlFor="email"
                    className="text-sm font-semibold text-slate-700 dark:text-slate-200"
                  >
                    E-mail ou usuário
                  </label>

                  <div className="mt-2 flex items-center rounded-2xl border border-slate-200 bg-white px-4 focus-within:border-blue-500 focus-within:ring-4 focus-within:ring-blue-100 dark:border-slate-600 dark:bg-slate-900 dark:focus-within:border-blue-400 dark:focus-within:ring-blue-500/20">
                    <Mail className="h-5 w-5 text-slate-400" />
                    <input
                      id="email"
                      name="email"
                      type="text"
                      value={formData.email}
                      onChange={handleChange}
                      placeholder="admin@imetro.ao"
                      required
                      className="w-full border-0 bg-transparent px-3 py-3 text-slate-900 outline-none placeholder:text-slate-400 dark:text-white dark:placeholder:text-slate-500"
                    />
                  </div>
                </div>

                <div>
                  <label
                    htmlFor="password"
                    className="text-sm font-semibold text-slate-700 dark:text-slate-200"
                  >
                    Senha
                  </label>

                  <div className="mt-2 flex items-center rounded-2xl border border-slate-200 bg-white px-4 focus-within:border-blue-500 focus-within:ring-4 focus-within:ring-blue-100 dark:border-slate-600 dark:bg-slate-900 dark:focus-within:border-blue-400 dark:focus-within:ring-blue-500/20">
                    <LockKeyhole className="h-5 w-5 text-slate-400" />
                    <input
                      id="password"
                      name="password"
                      type="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Digite sua senha"
                      required
                      className="w-full border-0 bg-transparent px-3 py-3 text-slate-900 outline-none placeholder:text-slate-400 dark:text-white dark:placeholder:text-slate-500"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="flex min-h-11 w-full items-center justify-center gap-3 rounded-2xl bg-blue-700 px-5 py-3.5 text-sm font-bold text-white shadow-lg shadow-blue-700/30 transition hover:bg-blue-800 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-blue-500/30 disabled:cursor-not-allowed disabled:opacity-70 dark:bg-blue-600 dark:hover:bg-blue-500"
                >
                  {loading ? "Validando acesso..." : "Entrar no painel"}
                  <ArrowRight className="h-5 w-5" />
                </button>
              </form>

              <div className="mt-8 rounded-2xl bg-slate-50 p-4 text-xs leading-6 text-slate-500 dark:bg-slate-900 dark:text-slate-400">
                API produção:{" "}
                <span className="font-semibold text-slate-700 dark:text-slate-200">
                  {APP_CONFIG.apiBaseUrl}
                </span>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}
