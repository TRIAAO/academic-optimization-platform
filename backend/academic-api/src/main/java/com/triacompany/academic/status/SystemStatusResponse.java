package com.triacompany.academic.status;

import java.time.LocalDateTime;
import java.util.List;

public record SystemStatusResponse(
        String application,
        String company,
        String version,
        String environment,
        String overallStatus,
        Long uptimeSeconds,
        List<SystemComponentStatusResponse> components,
        LocalDateTime checkedAt
) {
}