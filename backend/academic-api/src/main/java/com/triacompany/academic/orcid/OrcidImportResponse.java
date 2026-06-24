package com.triacompany.academic.orcid;

import java.util.List;

public record OrcidImportResponse(
        OrcidImportLogResponse log,
        List<OrcidWorkResponse> importedWorks
) {
}