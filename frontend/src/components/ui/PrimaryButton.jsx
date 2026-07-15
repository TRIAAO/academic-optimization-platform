import { Loader2 } from "lucide-react";

export default function PrimaryButton({
  children,
  type = "button",
  loading = false,
  disabled = false,
  icon: Icon,
  variant = "primary",
  onClick
}) {
  const variants = {
    primary: "bg-blue-700 text-white hover:bg-blue-800 shadow-blue-700/25",
    dark: "bg-slate-950 text-white hover:bg-slate-800 shadow-slate-950/20",
    light:
      "bg-white text-slate-800 border border-slate-200 hover:bg-slate-50 shadow-slate-950/5",
    danger: "bg-red-700 text-white hover:bg-red-800 shadow-red-700/20"
  };

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center gap-2 rounded-2xl px-4 py-2.5 text-sm font-bold shadow-sm transition disabled:cursor-not-allowed disabled:opacity-60 ${
        variants[variant] || variants.primary
      }`}
    >
      {loading ? (
        <Loader2 className="h-4 w-4 animate-spin" />
      ) : Icon ? (
        <Icon className="h-4 w-4" />
      ) : null}

      {children}
    </button>
  );
}