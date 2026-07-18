import { LineChart } from "lucide-react";

const CHART_WIDTH = 760;
const CHART_HEIGHT = 300;
const PADDING = {
  top: 24,
  right: 24,
  bottom: 52,
  left: 58
};

function isFiniteNumber(value) {
  return Number.isFinite(Number(value));
}

function formatMonth(value) {
  if (!value) return "";

  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("pt-AO", {
    month: "short",
    year: "2-digit"
  })
    .format(date)
    .replace(" de ", " ");
}

function defaultFormatValue(value) {
  return new Intl.NumberFormat("pt-AO", {
    maximumFractionDigits: 1
  }).format(Number(value || 0));
}

export default function InstitutionalLineChart({
  title,
  description,
  data = [],
  series = [],
  fixedMax,
  valueSuffix = "",
  formatValue = defaultFormatValue
}) {
  const plotWidth = CHART_WIDTH - PADDING.left - PADDING.right;
  const plotHeight = CHART_HEIGHT - PADDING.top - PADDING.bottom;
  const allValues = data.flatMap((point) =>
    series
      .map((item) => point?.[item.key])
      .filter((value) => isFiniteNumber(value))
      .map(Number)
  );
  const computedMax = allValues.length > 0 ? Math.max(...allValues) : 0;
  const maxValue = Math.max(Number(fixedMax || 0), computedMax, 1);
  const labelStep = Math.max(1, Math.ceil(data.length / 6));

  const xForIndex = (index) => {
    if (data.length <= 1) return PADDING.left + plotWidth / 2;
    return PADDING.left + (index / (data.length - 1)) * plotWidth;
  };

  const yForValue = (value) => {
    const normalized = Math.max(0, Math.min(Number(value || 0), maxValue));
    return PADDING.top + plotHeight - (normalized / maxValue) * plotHeight;
  };

  return (
    <section className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h3 className="font-black text-slate-950">{title}</h3>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">
            {description}
          </p>
        </div>

        <div className="flex flex-wrap gap-3" aria-label="Legenda do gráfico">
          {series.map((item) => (
            <div key={item.key} className="flex items-center gap-2 text-xs font-bold text-slate-600">
              <span
                className="h-2.5 w-2.5 rounded-full"
                style={{ backgroundColor: item.color }}
                aria-hidden="true"
              />
              {item.label}
            </div>
          ))}
        </div>
      </div>

      {data.length === 0 ? (
        <div className="mt-6 flex min-h-64 flex-col items-center justify-center rounded-2xl border border-dashed border-slate-200 bg-slate-50 text-center">
          <LineChart className="h-8 w-8 text-slate-400" />
          <p className="mt-3 text-sm font-bold text-slate-700">
            Ainda não existem pontos históricos.
          </p>
        </div>
      ) : (
        <div className="mt-5 overflow-x-auto">
          <svg
            viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
            className="min-w-[680px]"
            role="img"
            aria-label={title}
          >
            <title>{title}</title>

            {[0, 1, 2, 3, 4].map((gridIndex) => {
              const ratio = gridIndex / 4;
              const y = PADDING.top + ratio * plotHeight;
              const value = maxValue * (1 - ratio);

              return (
                <g key={gridIndex}>
                  <line
                    x1={PADDING.left}
                    x2={CHART_WIDTH - PADDING.right}
                    y1={y}
                    y2={y}
                    stroke="currentColor"
                    className="text-slate-200"
                    strokeDasharray="4 6"
                  />
                  <text
                    x={PADDING.left - 10}
                    y={y + 4}
                    textAnchor="end"
                    fontSize="11"
                    fill="currentColor"
                    className="text-slate-500"
                  >
                    {formatValue(value)}{valueSuffix}
                  </text>
                </g>
              );
            })}

            {data.map((point, index) => {
              const shouldShow =
                index === 0 ||
                index === data.length - 1 ||
                index % labelStep === 0;

              if (!shouldShow) return null;

              return (
                <text
                  key={`${point.periodStart}-${index}`}
                  x={xForIndex(index)}
                  y={CHART_HEIGHT - 18}
                  textAnchor="middle"
                  fontSize="11"
                  fill="currentColor"
                  className="text-slate-500"
                >
                  {formatMonth(point.periodStart)}
                </text>
              );
            })}

            {series.map((item) => {
              const validPoints = data
                .map((point, index) => ({
                  index,
                  value: point?.[item.key],
                  periodStart: point?.periodStart
                }))
                .filter((point) => isFiniteNumber(point.value));

              const path = validPoints
                .map((point, index) => {
                  const prefix = index === 0 ? "M" : "L";
                  return `${prefix} ${xForIndex(point.index)} ${yForValue(point.value)}`;
                })
                .join(" ");

              return (
                <g key={item.key}>
                  {path && (
                    <path
                      d={path}
                      fill="none"
                      stroke={item.color}
                      strokeWidth="3"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  )}

                  {validPoints.map((point) => (
                    <circle
                      key={`${item.key}-${point.periodStart}`}
                      cx={xForIndex(point.index)}
                      cy={yForValue(point.value)}
                      r="4"
                      fill={item.color}
                      stroke="white"
                      strokeWidth="2"
                    >
                      <title>
                        {item.label}: {formatValue(point.value)}{valueSuffix} — {formatMonth(point.periodStart)}
                      </title>
                    </circle>
                  ))}
                </g>
              );
            })}
          </svg>
        </div>
      )}
    </section>
  );
}
