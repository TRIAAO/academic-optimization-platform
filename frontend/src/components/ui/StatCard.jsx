export default function StatCard({ title, value, description, icon: Icon }) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-slate-500">{title}</p>
          <p className="mt-3 text-3xl font-bold tracking-tight text-slate-950">
            {value}
          </p>
        </div>

        {Icon && (
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-blue-700">
            <Icon className="h-6 w-6" />
          </div>
        )}
      </div>

      {description && (
        <p className="mt-4 text-sm leading-6 text-slate-500">{description}</p>
      )}
    </div>
  );
}