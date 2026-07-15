export function formatNumber(value) {
  const number = Number(value || 0);

  return new Intl.NumberFormat("pt-AO").format(number);
}

export function formatDateTime(value) {
  if (!value || value === "Não informado") {
    return "Não informado";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("pt-AO", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(date);
}

export function formatStatus(status) {
  if (!status) {
    return "Não informado";
  }

  const normalized = String(status).toUpperCase();

  if (["UP", "ONLINE", "OK", "HEALTHY"].includes(normalized)) {
    return "Online";
  }

  if (["DOWN", "OFFLINE", "ERROR", "FAILED"].includes(normalized)) {
    return "Indisponível";
  }

  return status;
}