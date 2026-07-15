export default function PageHeader({
  eyebrow,
  title,
  description,
  actions,
  children
}) {
  return (
    <section className="rounded-[1.75rem] border border-slate-200 bg-white p-5 shadow-sm sm:rounded-[2rem] sm:p-8">
      <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0">
          {eyebrow && (
            <p className="text-[11px] font-black uppercase tracking-[0.28em] text-blue-700 sm:text-xs">
              {eyebrow}
            </p>
          )}

          <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-950 sm:text-3xl">
            {title}
          </h2>

          {description && (
            <p className="mt-3 max-w-3xl text-sm leading-7 text-slate-500 sm:text-base">
              {description}
            </p>
          )}
        </div>

        {actions && (
          <div className="flex w-full flex-wrap gap-3 sm:w-auto sm:justify-end">
            {actions}
          </div>
        )}
      </div>

      {children && <div className="mt-6">{children}</div>}
    </section>
  );
}