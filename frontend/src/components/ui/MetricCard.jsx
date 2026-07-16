const TONES = {
  blue: "bg-blue-50 text-blue-700 ring-blue-100",
  emerald: "bg-emerald-50 text-emerald-700 ring-emerald-100",
  amber: "bg-amber-50 text-amber-700 ring-amber-100",
  violet: "bg-violet-50 text-violet-700 ring-violet-100",
  rose: "bg-rose-50 text-rose-700 ring-rose-100",
  red: "bg-red-50 text-red-700 ring-red-100",
  slate: "bg-slate-100 text-slate-600 ring-slate-200"
};

const VALUE_SIZES = {
  sm: "text-base leading-5",
  md: "text-2xl leading-7",
  lg: "text-3xl leading-8"
};

export default function MetricCard({
  title,
  value,
  description,
  icon: Icon,
  tone,
  healthy,
  valueSize = "md"
}) {
  const resolvedTone = tone || (healthy === undefined ? "blue" : healthy ? "emerald" : "slate");
  const toneClasses = TONES[resolvedTone] || TONES.blue;
  const valueClasses = VALUE_SIZES[valueSize] || VALUE_SIZES.md;

  return (
    <article className="h-full min-w-0 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-slate-300 hover:shadow-md">
      <div className="flex min-w-0 items-center gap-2.5">
        {Icon && (
          <div className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl ring-1 ${toneClasses}`}>
            <Icon className="h-[18px] w-[18px]" />
          </div>
        )}

        <p className="min-w-0 text-[11px] font-black uppercase leading-4 tracking-[0.08em] text-slate-500">
          {title}
        </p>
      </div>

      <p className={`mt-3 break-words font-black tracking-tight text-slate-950 ${valueClasses}`}>
        {value}
      </p>

      {description && (
        <p
          className="mt-1 line-clamp-2 text-xs leading-4 text-slate-500"
          title={typeof description === "string" ? description : undefined}
        >
          {description}
        </p>
      )}
    </article>
  );
}
