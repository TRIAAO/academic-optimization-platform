package com.triacompany.academic.editorial;

final class EditorialEvidenceLevel {

    private EditorialEvidenceLevel() {
    }

    static String from(int relevanceScore, boolean hasSelectedWork) {
        if (!hasSelectedWork) {
            return "SEM_EVIDENCIA";
        }
        if (relevanceScore >= 75) {
            return "FORTE";
        }
        if (relevanceScore >= 45) {
            return "MODERADA";
        }
        return "INICIAL";
    }
}
