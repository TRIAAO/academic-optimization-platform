package com.triacompany.academic.dashboard;

public record DashboardMetricResponse(
        String code,
        String label,
        Integer value,
        String status,
        String description
) {
}