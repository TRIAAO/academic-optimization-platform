package com.triacompany.academic.openalex;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class OpenAlexAuthorId {

    private OpenAlexAuthorId() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Author ID OpenAlex é obrigatório.");
        }

        String normalized = URLDecoder.decode(value.trim(), StandardCharsets.UTF_8)
                .toUpperCase(Locale.ROOT)
                .replace("HTTPS://OPENALEX.ORG/", "")
                .replace("HTTP://OPENALEX.ORG/", "")
                .replaceFirst("[/?#].*$", "")
                .trim();

        if (!normalized.matches("A\\d+")) {
            throw new IllegalArgumentException(
                    "Author ID OpenAlex inválido. Use o formato A123456789."
            );
        }

        return normalized;
    }

    public static String url(String value) {
        return "https://openalex.org/" + normalize(value);
    }
}
