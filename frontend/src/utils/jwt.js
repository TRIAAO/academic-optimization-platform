function decodeBase64Url(value) {
  const normalized = String(value || "")
    .replace(/-/g, "+")
    .replace(/_/g, "/");
  const paddingLength = (4 - (normalized.length % 4)) % 4;
  return atob(`${normalized}${"=".repeat(paddingLength)}`);
}

export function parseJwtPayload(token) {
  try {
    const payloadPart = String(token || "").split(".")[1];
    if (!payloadPart) {
      return {};
    }

    const jsonPayload = decodeURIComponent(
      decodeBase64Url(payloadPart)
        .split("")
        .map((character) =>
          `%${character.charCodeAt(0).toString(16).padStart(2, "0")}`
        )
        .join("")
    );

    const payload = JSON.parse(jsonPayload);
    return payload && typeof payload === "object" ? payload : {};
  } catch {
    return {};
  }
}

export function isTokenExpired(token, currentTime = Date.now()) {
  const payload = parseJwtPayload(token);
  const expiration = Number(payload?.exp);

  if (!Number.isFinite(expiration)) {
    return true;
  }

  return currentTime >= expiration * 1000;
}

export function extractToken(data) {
  return (
    data?.token ||
    data?.accessToken ||
    data?.access_token ||
    data?.jwt ||
    data?.bearerToken ||
    null
  );
}

export function extractUser(data, email, token) {
  const jwtPayload = token ? parseJwtPayload(token) : {};

  return (
    data?.user ||
    data?.profile ||
    data?.account || {
      name:
        data?.name ||
        jwtPayload?.name ||
        jwtPayload?.fullName ||
        jwtPayload?.sub ||
        "Administrador",
      email: data?.email || jwtPayload?.email || email,
      role:
        data?.role ||
        jwtPayload?.role ||
        jwtPayload?.authorities?.[0] ||
        jwtPayload?.roles?.[0] ||
        "ADMIN"
    }
  );
}
