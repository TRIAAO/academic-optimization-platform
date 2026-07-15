import { useState } from "react";
import { AlertCircle, ArrowRight, LockKeyhole, Mail, ShieldCheck } from "lucide-react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
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
    <main className="min-h-screen bg-slate-950">
      <div className="grid min-h-screen lg:grid-cols-[1.1fr_0.9fr]">
        <section className="relative hidden overflow-hidden px-12 py-12 text-white lg:flex lg:flex-col lg:justify-between">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(37,99,235,0.45),_transparent_35%),radial-gradient(circle_at_bottom_right,_rgba(16,185,129,0.22),_transparent_30%)]" />

          <div className="relative z-10">
            <div className="inline-flex items-center gap-3 rounded-full border border-white/15 bg-white/10 px-4 py-2 text-sm text-blue-100 backdrop-blur">
              <ShieldCheck className="h-4 w-4" />
              Backend MVP validado em produção
            </div>

            <h1 className="mt-10 max-w-3xl text-5xl font-black leading-tight tracking-tight">
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

        <section className="flex items-center justify-center bg-slate-100 px-4 py-10 sm:px-6 lg:px-10">
          <div className="w-full max-w-md">
            <div className="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-2xl shadow-slate-950/10">
              <div className="mb-8">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-600 text-white shadow-lg shadow-blue-700/30">
                  <ShieldCheck className="h-7 w-7" />
                </div>

                <h2 className="mt-6 text-2xl font-black text-slate-950">
                  Acesso administrativo
                </h2>

                <p className="mt-2 text-sm leading-6 text-slate-500">
                  Entre com o usuário administrador criado no backend.
                </p>
              </div>

              {error && (
                <div className="mb-6 flex gap-3 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-800">
                  <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />
                  <p>{error}</p>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                  <label
                    htmlFor="email"
                    className="text-sm font-semibold text-slate-700"
                  >
                    E-mail ou usuário
                  </label>

                  <div className="mt-2 flex items-center rounded-2xl border border-slate-200 bg-white px-4 focus-within:border-blue-500 focus-within:ring-4 focus-within:ring-blue-100">
                    <Mail className="h-5 w-5 text-slate-400" />
                    <input
                      id="email"
                      name="email"
                      type="text"
                      value={formData.email}
                      onChange={handleChange}
                      placeholder="admin@imetro.ao"
                      required
                      className="w-full border-0 bg-transparent px-3 py-3 text-slate-900 outline-none placeholder:text-slate-400"
                    />
                  </div>
                </div>

                <div>
                  <label
                    htmlFor="password"
                    className="text-sm font-semibold text-slate-700"
                  >
                    Senha
                  </label>

                  <div className="mt-2 flex items-center rounded-2xl border border-slate-200 bg-white px-4 focus-within:border-blue-500 focus-within:ring-4 focus-within:ring-blue-100">
                    <LockKeyhole className="h-5 w-5 text-slate-400" />
                    <input
                      id="password"
                      name="password"
                      type="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Digite sua senha"
                      required
                      className="w-full border-0 bg-transparent px-3 py-3 text-slate-900 outline-none placeholder:text-slate-400"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="flex w-full items-center justify-center gap-3 rounded-2xl bg-blue-700 px-5 py-3.5 text-sm font-bold text-white shadow-lg shadow-blue-700/30 transition hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {loading ? "Validando acesso..." : "Entrar no painel"}
                  <ArrowRight className="h-5 w-5" />
                </button>
              </form>

              <div className="mt-8 rounded-2xl bg-slate-50 p-4 text-xs leading-6 text-slate-500">
                API produção:{" "}
                <span className="font-semibold text-slate-700">
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