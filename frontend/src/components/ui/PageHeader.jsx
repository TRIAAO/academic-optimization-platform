export default function PageHeader({
  eyebrow,
  title,
  description,
  actions,
  children
}) {
  return (
    <section className="rounded-[2rem] border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
        <div>
          {eyebrow && (
            <p className="text-xs font-bold uppercase tracking-[0.28em] text-blue-700">
              {eyebrow}
            </p>
          )}

          <h2 className="mt-3 text-2xl font-black tracking-tight text-slate-950 sm:text-3xl">
            {title}
          </h2>

          {description && (
            <p className="mt-3 max-w-3xl text-sm leading-7 text-slate-500">
              {description}
            </p>
          )}
        </div>

        {actions && <div className="flex flex-wrap gap-3">{actions}</div>}
      </div>

      {children && <div className="mt-6">{children}</div>}
    </section>
  );
}