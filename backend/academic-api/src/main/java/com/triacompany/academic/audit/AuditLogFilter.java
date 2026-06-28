package com.triacompany.academic.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();

        if (!isWriteMethod(method)) {
            return true;
        }

        String path = request.getRequestURI();

        return path == null
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/static")
                || path.startsWith("/favicon");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Exception capturedException = null;

        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            capturedException = exception;
            throw exception;
        } finally {
            recordAuditLog(request, response, capturedException);
        }
    }

    private void recordAuditLog(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) {
        try {
            String endpoint = request.getRequestURI();
            String method = request.getMethod();
            int httpStatus = response.getStatus();

            AuditEventStatus status = resolveStatus(httpStatus, exception);
            String action = resolveAction(method, endpoint);
            String module = resolveModule(endpoint);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String actorEmail = authentication != null && authentication.isAuthenticated()
                    ? authentication.getName()
                    : null;

            String actorRole = authentication != null && authentication.isAuthenticated()
                    ? authentication.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","))
                    : null;

            auditLogService.record(
                    actorEmail,
                    actorRole,
                    action,
                    module,
                    method,
                    endpoint,
                    resolveTargetType(endpoint),
                    resolveTargetId(endpoint),
                    status,
                    httpStatus,
                    resolveIpAddress(request),
                    request.getHeader("User-Agent"),
                    resolveMessage(status, httpStatus, exception)
            );
        } catch (Exception ignored) {
            // A auditoria nunca deve derrubar a requisição principal.
        }
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private AuditEventStatus resolveStatus(int httpStatus, Exception exception) {
        if (exception != null || httpStatus >= 500) {
            return AuditEventStatus.SERVER_ERROR;
        }

        if (httpStatus >= 400) {
            return AuditEventStatus.CLIENT_ERROR;
        }

        return AuditEventStatus.SUCCESS;
    }

    private String resolveAction(String method, String endpoint) {
        String path = endpoint == null ? "" : endpoint.toLowerCase(Locale.ROOT);

        if (path.contains("/auth/login")) {
            return "AUTH_LOGIN";
        }

        if (path.contains("/auth/register")) {
            return "AUTH_REGISTER";
        }

        if (path.contains("/orcid") && path.contains("/import")) {
            return "ORCID_IMPORT";
        }

        if (path.contains("/orcid") && path.contains("/sync-profile")) {
            return "ORCID_SYNC_PROFILE";
        }

        if (path.contains("/openalex") && path.contains("/import-works")) {
            return "OPENALEX_IMPORT_WORKS";
        }

        if (path.contains("/openalex") && path.contains("/confirm")) {
            return "OPENALEX_CONFIRM_WORK";
        }

        if (path.contains("/openalex") && path.contains("/reject")) {
            return "OPENALEX_REJECT_WORK";
        }

        if (path.contains("/crossref") && path.contains("/validate")) {
            return "CROSSREF_VALIDATE_WORK";
        }

        if (path.contains("/optimization-reports") && path.contains("/pdf")) {
            return "REPORT_EXPORT_PDF";
        }

        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "PATCH" -> "PARTIAL_UPDATE";
            case "DELETE" -> "DELETE";
            default -> "UNKNOWN_ACTION";
        };
    }

    private String resolveModule(String endpoint) {
        String path = endpoint == null ? "" : endpoint.toLowerCase(Locale.ROOT);

        if (path.contains("/auth")) {
            return "AUTH";
        }

        if (path.contains("/researchers")) {
            return "RESEARCHERS";
        }

        if (path.contains("/academic-profiles")) {
            return "ACADEMIC_PROFILES";
        }

        if (path.contains("/orcid")) {
            return "ORCID";
        }

        if (path.contains("/openalex")) {
            return "OPENALEX";
        }

        if (path.contains("/crossref")) {
            return "CROSSREF";
        }

        if (path.contains("/optimization-reports")) {
            return "OPTIMIZATION_REPORTS";
        }

        if (path.contains("/google-scholar-checklists")) {
            return "GOOGLE_SCHOLAR_CHECKLISTS";
        }

        if (path.contains("/institutional-dashboard")) {
            return "INSTITUTIONAL_DASHBOARD";
        }

        return "GENERAL";
    }

    private String resolveTargetType(String endpoint) {
        String module = resolveModule(endpoint);

        return switch (module) {
            case "RESEARCHERS" -> "RESEARCHER";
            case "ACADEMIC_PROFILES" -> "ACADEMIC_PROFILE";
            case "ORCID" -> "ORCID_RESOURCE";
            case "OPENALEX" -> "OPENALEX_WORK";
            case "CROSSREF" -> "CROSSREF_VALIDATION";
            case "OPTIMIZATION_REPORTS" -> "OPTIMIZATION_REPORT";
            case "GOOGLE_SCHOLAR_CHECKLISTS" -> "GOOGLE_SCHOLAR_CHECKLIST";
            case "INSTITUTIONAL_DASHBOARD" -> "INSTITUTIONAL_DASHBOARD";
            case "AUTH" -> "AUTH_EVENT";
            default -> null;
        };
    }

    private String resolveTargetId(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return null;
        }

        String[] parts = endpoint.split("/");

        for (String part : parts) {
            if (looksLikeUuid(part)) {
                return part;
            }
        }

        return null;
    }

    private boolean looksLikeUuid(String value) {
        return value != null
                && value.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private String resolveMessage(
            AuditEventStatus status,
            int httpStatus,
            Exception exception
    ) {
        if (exception != null) {
            return "Ação registrada com exceção: " + exception.getClass().getSimpleName();
        }

        return switch (status) {
            case SUCCESS -> "Ação executada com sucesso.";
            case CLIENT_ERROR -> "Ação retornou erro de cliente. HTTP " + httpStatus + ".";
            case SERVER_ERROR -> "Ação retornou erro interno. HTTP " + httpStatus + ".";
        };
    }
}