const DEVELOPER_EMAIL_DOMAINS = ["@triacompany.com"];

const DEVELOPER_ROLES = [
  "DEVELOPER",
  "DEV",
  "TECHNICAL_ADMIN",
  "SUPER_ADMIN",
  "SYSTEM_ADMIN",
  "TRIA_ADMIN"
];

function normalize(value) {
  return String(value || "").trim().toLowerCase();
}

export function isDeveloperUser(user) {
  if (!user) return false;

  const email = normalize(user.email || user.username || user.userEmail);
  const role = String(user.role || user.profile || user.authority || "").trim();

  const isTriaEmail = DEVELOPER_EMAIL_DOMAINS.some((domain) =>
    email.endsWith(domain)
  );

  const hasDeveloperRole = DEVELOPER_ROLES.some(
    (developerRole) => developerRole.toUpperCase() === role.toUpperCase()
  );

  return isTriaEmail || hasDeveloperRole;
}

export function canViewTechnicalArea(user) {
  return isDeveloperUser(user);
}

export function canViewModule(user, module) {
  if (!module?.technical) {
    return true;
  }

  return canViewTechnicalArea(user);
}