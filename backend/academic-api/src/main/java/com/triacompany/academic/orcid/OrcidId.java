package com.triacompany.academic.orcid;

import java.util.Locale;

public final class OrcidId {

    private OrcidId() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String compact = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("HTTPS://ORCID.ORG/", "")
                .replace("HTTP://ORCID.ORG/", "")
                .replaceFirst("[/?#].*$", "")
                .replaceAll("[\\s-]", "");

        if (!compact.matches("\\d{15}[0-9X]")) {
            throw new IllegalArgumentException(
                    "ORCID inválido. Informe os 16 caracteres do identificador."
            );
        }

        if (!hasValidChecksum(compact)) {
            throw new IllegalArgumentException(
                    "ORCID inválido. O dígito verificador não corresponde ao identificador informado."
            );
        }

        return compact.substring(0, 4)
                + "-" + compact.substring(4, 8)
                + "-" + compact.substring(8, 12)
                + "-" + compact.substring(12, 16);
    }

    private static boolean hasValidChecksum(String compact) {
        int total = 0;

        for (int index = 0; index < 15; index++) {
            total = (total + Character.digit(compact.charAt(index), 10)) * 2;
        }

        int result = (12 - (total % 11)) % 11;
        char expected = result == 10 ? 'X' : Character.forDigit(result, 10);
        return compact.charAt(15) == expected;
    }
}
