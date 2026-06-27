package com.triacompany.academic.openalex;

import java.util.List;
import java.util.UUID;

public record OpenAlexImportResponse(
        UUID researcherId,
        String researcherName,
        String searchName,
        Integer totalFound,
        Integer totalImported,
        List<OpenAlexWorkResponse> works
) {
}