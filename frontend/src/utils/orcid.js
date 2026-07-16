export function formatOrcidInput(value) {
  const withoutUrl = String(value || "").replace(/^https?:\/\/orcid\.org\//i, "");
  const identifier = withoutUrl.split(/[/?#]/)[0];
  const compact = identifier.toUpperCase().replace(/[^0-9X]/g, "").slice(0, 16);
  return compact.match(/.{1,4}/g)?.join("-") || "";
}

export function isValidOrcid(value) {
  const compact = String(value || "").replace(/-/g, "").toUpperCase();
  if (!/^\d{15}[0-9X]$/.test(compact)) return false;

  let total = 0;
  for (let index = 0; index < 15; index += 1) {
    total = (total + Number(compact[index])) * 2;
  }

  const result = (12 - (total % 11)) % 11;
  const expected = result === 10 ? "X" : String(result);
  return compact[15] === expected;
}

export function selectCurrentOrcidAffiliation(summary) {
  const affiliations = [
    ...(summary?.employments || []),
    ...(summary?.educations || [])
  ];

  return affiliations.find((affiliation) => !affiliation.endDate)
    || affiliations[0]
    || null;
}
