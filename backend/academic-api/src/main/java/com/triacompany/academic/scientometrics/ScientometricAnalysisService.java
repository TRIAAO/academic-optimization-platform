package com.triacompany.academic.scientometrics;

import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScientometricAnalysisService {

    private final ScientometricMetricRepository scientometricMetricRepository;
    private final ResearcherRepository researcherRepository;

    @Value("${app.scientometrics.institutional-email-domains:universidade.ao,imetroangola.com}")
    private String institutionalEmailDomains;

    @Transactional(readOnly = true)
    public ScientometricAnalysisResponse analyze(UUID researcherId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        List<ScientometricMetric> metrics = scientometricMetricRepository
                .findByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(researcherId);

        if (metrics.isEmpty()) {
            ScientometricAnalysisResponse.ScientometricAlert noDataAlert =
                    new ScientometricAnalysisResponse.ScientometricAlert(
                            "NO_MEASUREMENTS",
                            "INFO",
                            "Sem medições cientométricas",
                            "Ainda não existem dados suficientes para calcular D-index, vitalidade ou validação institucional do e-mail.",
                            "Registe a primeira medição cientométrica do pesquisador."
                    );

            return new ScientometricAnalysisResponse(
                    researcherId,
                    researcher.getFullName(),
                    null,
                    0,
                    unavailableDIndex(),
                    unavailableVitality(),
                    unavailableEmail(),
                    List.of(noDataAlert)
            );
        }

        ScientometricMetric latest = metrics.get(0);
        ScientometricMetric previous = metrics.size() > 1 ? metrics.get(1) : null;
        List<ScientometricAnalysisResponse.ScientometricAlert> alerts = new ArrayList<>();

        ScientometricAnalysisResponse.DIndexAssessment dIndexAssessment =
                assessDIndex(latest, alerts);
        ScientometricAnalysisResponse.VitalityAssessment vitalityAssessment =
                assessVitality(latest, previous, alerts);
        ScientometricAnalysisResponse.InstitutionalEmailAssessment emailAssessment =
                assessInstitutionalEmail(latest, alerts);

        return new ScientometricAnalysisResponse(
                researcherId,
                researcher.getFullName(),
                latest.getSnapshotDate(),
                metrics.size(),
                dIndexAssessment,
                vitalityAssessment,
                emailAssessment,
                List.copyOf(alerts)
        );
    }

    private ScientometricAnalysisResponse.DIndexAssessment assessDIndex(
            ScientometricMetric latest,
            List<ScientometricAnalysisResponse.ScientometricAlert> alerts
    ) {
        Integer hIndex = latest.getHIndexTotal();
        Integer dIndex = latest.getDIndex();

        if (hIndex == null || dIndex == null) {
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "D_INDEX_INCOMPLETE",
                    "INFO",
                    "Comparação H-index × D-index incompleta",
                    "A análise disciplinar requer os dois indicadores na mesma medição.",
                    "Informe H-index total e D-index na próxima atualização."
            ));
            return unavailableDIndex();
        }

        int denominator = Math.max(hIndex, 1);
        int deviationPercent = (int) Math.round(
                (Math.abs(hIndex - dIndex) * 100.0) / denominator
        );

        String status;
        String explanation;

        if (deviationPercent > 30) {
            status = "CRITICAL";
            explanation = "A diferença supera o limite de 30% e exige revisão das áreas de interesse e da classificação disciplinar.";
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "D_INDEX_DEVIATION_HIGH",
                    "HIGH",
                    "Desvio elevado entre H-index e D-index",
                    "O desvio calculado é de " + deviationPercent + "%.",
                    "Revise as áreas de interesse, a disciplina principal e possíveis obras atribuídas fora do escopo."
            ));
        } else if (deviationPercent > 15) {
            status = "ATTENTION";
            explanation = "A diferença está abaixo do limite crítico, mas merece acompanhamento na próxima medição.";
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "D_INDEX_DEVIATION_MODERATE",
                    "MEDIUM",
                    "Desvio moderado entre H-index e D-index",
                    "O desvio calculado é de " + deviationPercent + "%.",
                    "Confirme se as áreas declaradas representam corretamente a produção científica atual."
            ));
        } else {
            status = "ALIGNED";
            explanation = "H-index e D-index estão alinhados dentro da faixa de tolerância institucional.";
        }

        return new ScientometricAnalysisResponse.DIndexAssessment(
                hIndex,
                dIndex,
                deviationPercent,
                status,
                explanation
        );
    }

    private ScientometricAnalysisResponse.VitalityAssessment assessVitality(
            ScientometricMetric latest,
            ScientometricMetric previous,
            List<ScientometricAnalysisResponse.ScientometricAlert> alerts
    ) {
        Integer citationsPercent = recentPercent(
                latest.getCitationsLastSixYears(),
                latest.getCitationsTotal(),
                "Citações",
                alerts
        );
        Integer hIndexPercent = recentPercent(
                latest.getHIndexLastSixYears(),
                latest.getHIndexTotal(),
                "H-index",
                alerts
        );
        Integer i10Percent = recentPercent(
                latest.getI10IndexLastSixYears(),
                latest.getI10IndexTotal(),
                "i10-index",
                alerts
        );

        Integer score = weightedVitalityScore(citationsPercent, hIndexPercent, i10Percent);
        String status;
        String explanation;

        if (score == null) {
            status = "NOT_AVAILABLE";
            explanation = "Não há pares total/últimos 6 anos suficientes para calcular a vitalidade científica.";
        } else if (score >= 60) {
            status = "ACTIVE";
            explanation = "A produção recente representa uma parcela forte do impacto acumulado.";
        } else if (score >= 35) {
            status = "RECOVERING";
            explanation = "A atividade recente é intermediária e deve ser acompanhada nas próximas medições.";
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "VITALITY_RECOVERING",
                    "MEDIUM",
                    "Vitalidade científica intermediária",
                    "O score de vitalidade dos últimos 6 anos é " + score + "/100.",
                    "Acompanhe a evolução anual e priorize a atualização de obras e citações recentes."
            ));
        } else {
            status = "STAGNANT";
            explanation = "A parcela de impacto recente é baixa em relação ao histórico acumulado.";
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "VITALITY_LOW",
                    "HIGH",
                    "Baixa vitalidade nos últimos 6 anos",
                    "O score de vitalidade dos últimos 6 anos é " + score + "/100.",
                    "Revise a atualização do perfil, a cobertura de obras recentes e a estratégia de publicação."
            ));
        }

        Integer citationsDelta = delta(latest.getCitationsTotal(), previous == null ? null : previous.getCitationsTotal());
        Integer hIndexDelta = delta(latest.getHIndexTotal(), previous == null ? null : previous.getHIndexTotal());
        Integer i10Delta = delta(latest.getI10IndexTotal(), previous == null ? null : previous.getI10IndexTotal());
        String trend = classifyTrend(previous, citationsDelta, hIndexDelta, i10Delta, alerts);

        return new ScientometricAnalysisResponse.VitalityAssessment(
                score,
                status,
                citationsPercent,
                hIndexPercent,
                i10Percent,
                trend,
                citationsDelta,
                hIndexDelta,
                i10Delta,
                explanation
        );
    }

    private ScientometricAnalysisResponse.InstitutionalEmailAssessment assessInstitutionalEmail(
            ScientometricMetric latest,
            List<ScientometricAnalysisResponse.ScientometricAlert> alerts
    ) {
        String email = normalizeEmail(latest.getVerifiedEmail());
        boolean declaredVerified = Boolean.TRUE.equals(latest.getInstitutionalEmailVerified());

        if (email == null) {
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "VERIFIED_EMAIL_MISSING",
                    "MEDIUM",
                    "E-mail verificado não informado",
                    "A medição não possui um e-mail verificado associado ao perfil público.",
                    "Registe o e-mail verificado e confirme se pertence a um domínio institucional oficial."
            ));
            return unavailableEmail();
        }

        String domain = extractDomain(email);
        boolean recognizedDomain = isRecognizedInstitutionalDomain(domain);
        String status;
        String explanation;

        if (declaredVerified && recognizedDomain) {
            status = "VERIFIED";
            explanation = "O e-mail foi declarado verificado e pertence a um domínio institucional reconhecido.";
        } else if (declaredVerified) {
            status = "INCONSISTENT";
            explanation = "O e-mail foi marcado como institucional, mas o domínio não está na lista oficial configurada.";
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "INSTITUTIONAL_EMAIL_MISMATCH",
                    "HIGH",
                    "Inconsistência na verificação institucional",
                    "O domínio " + domain + " não corresponde aos domínios institucionais reconhecidos.",
                    "Confirme o domínio oficial ou corrija o estado de verificação da medição."
            ));
        } else if (recognizedDomain) {
            status = "DOMAIN_MATCH_PENDING";
            explanation = "O domínio é institucional, mas a verificação humana ainda não foi confirmada.";
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "INSTITUTIONAL_EMAIL_PENDING",
                    "MEDIUM",
                    "Domínio institucional pendente de validação",
                    "O e-mail pertence a um domínio reconhecido, porém ainda não está marcado como verificado.",
                    "Confirme manualmente a evidência pública antes de marcar o e-mail como verificado."
            ));
        } else {
            status = "NON_INSTITUTIONAL";
            explanation = "O domínio informado não corresponde a um domínio institucional reconhecido.";
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "NON_INSTITUTIONAL_EMAIL",
                    "MEDIUM",
                    "E-mail não institucional",
                    "O domínio " + domain + " não está na lista institucional configurada.",
                    "Solicite um e-mail institucional público ou mantenha o estado como não verificado."
            ));
        }

        return new ScientometricAnalysisResponse.InstitutionalEmailAssessment(
                email,
                domain,
                declaredVerified,
                recognizedDomain,
                status,
                explanation
        );
    }

    private Integer recentPercent(
            Integer recent,
            Integer total,
            String label,
            List<ScientometricAnalysisResponse.ScientometricAlert> alerts
    ) {
        if (recent == null || total == null || total <= 0) {
            return null;
        }

        if (recent > total) {
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "RECENT_EXCEEDS_TOTAL_" + label.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'),
                    "HIGH",
                    "Indicador recente superior ao total",
                    label + " dos últimos 6 anos supera o valor total informado.",
                    "Revise os valores da medição e a fonte utilizada."
            ));
        }

        return Math.max(0, Math.min(100, (int) Math.round((recent * 100.0) / total)));
    }

    private Integer weightedVitalityScore(
            Integer citationsPercent,
            Integer hIndexPercent,
            Integer i10Percent
    ) {
        double weightedSum = 0.0;
        double weightTotal = 0.0;

        if (citationsPercent != null) {
            weightedSum += citationsPercent * 0.50;
            weightTotal += 0.50;
        }
        if (hIndexPercent != null) {
            weightedSum += hIndexPercent * 0.30;
            weightTotal += 0.30;
        }
        if (i10Percent != null) {
            weightedSum += i10Percent * 0.20;
            weightTotal += 0.20;
        }

        if (weightTotal == 0.0) {
            return null;
        }

        return (int) Math.round(weightedSum / weightTotal);
    }

    private String classifyTrend(
            ScientometricMetric previous,
            Integer citationsDelta,
            Integer hIndexDelta,
            Integer i10Delta,
            List<ScientometricAnalysisResponse.ScientometricAlert> alerts
    ) {
        if (previous == null) {
            return "NO_HISTORY";
        }

        boolean hasNegative = isNegative(citationsDelta) || isNegative(hIndexDelta) || isNegative(i10Delta);
        boolean hasPositive = isPositive(citationsDelta) || isPositive(hIndexDelta) || isPositive(i10Delta);

        if (hasNegative) {
            alerts.add(new ScientometricAnalysisResponse.ScientometricAlert(
                    "CUMULATIVE_METRIC_DECREASE",
                    "HIGH",
                    "Redução em indicador acumulado",
                    "A medição mais recente apresenta redução em pelo menos um indicador acumulado.",
                    "Revise a origem dos dados, possíveis duplicidades removidas e alterações no perfil público."
            ));
            return "REVIEW";
        }

        return hasPositive ? "GROWING" : "STABLE";
    }

    private Integer delta(Integer latest, Integer previous) {
        if (latest == null || previous == null) {
            return null;
        }
        return latest - previous;
    }

    private boolean isNegative(Integer value) {
        return value != null && value < 0;
    }

    private boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return "domínio inválido";
        }
        return email.substring(atIndex + 1);
    }

    private boolean isRecognizedInstitutionalDomain(String domain) {
        if (domain == null || domain.isBlank() || "domínio inválido".equals(domain)) {
            return false;
        }

        return configuredInstitutionalDomains().stream()
                .anyMatch(officialDomain -> domain.equals(officialDomain)
                        || domain.endsWith("." + officialDomain));
    }

    private Set<String> configuredInstitutionalDomains() {
        return Arrays.stream(institutionalEmailDomains.split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private ScientometricAnalysisResponse.DIndexAssessment unavailableDIndex() {
        return new ScientometricAnalysisResponse.DIndexAssessment(
                null,
                null,
                null,
                "NOT_AVAILABLE",
                "H-index total e D-index precisam estar informados na mesma medição."
        );
    }

    private ScientometricAnalysisResponse.VitalityAssessment unavailableVitality() {
        return new ScientometricAnalysisResponse.VitalityAssessment(
                null,
                "NOT_AVAILABLE",
                null,
                null,
                null,
                "NO_HISTORY",
                null,
                null,
                null,
                "Não há dados suficientes para calcular a vitalidade científica."
        );
    }

    private ScientometricAnalysisResponse.InstitutionalEmailAssessment unavailableEmail() {
        return new ScientometricAnalysisResponse.InstitutionalEmailAssessment(
                null,
                null,
                false,
                false,
                "NOT_INFORMED",
                "Nenhum e-mail verificado foi informado na medição mais recente."
        );
    }
}
