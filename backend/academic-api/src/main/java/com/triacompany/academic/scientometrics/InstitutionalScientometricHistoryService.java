package com.triacompany.academic.scientometrics;

import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class InstitutionalScientometricHistoryService {

    private static final int DEFAULT_MONTHS = 12;
    private static final int MIN_MONTHS = 6;
    private static final int MAX_MONTHS = 60;
    private static final int DEFAULT_STALE_AFTER_MONTHS = 12;

    private final ScientometricMetricRepository scientometricMetricRepository;
    private final ResearcherRepository researcherRepository;

    @Transactional(readOnly = true)
    public InstitutionalScientometricHistoryResponse buildHistory(
            Integer months,
            Integer staleAfterMonths
    ) {
        int normalizedMonths = clamp(
                months == null ? DEFAULT_MONTHS : months,
                MIN_MONTHS,
                MAX_MONTHS
        );
        int normalizedStaleAfterMonths = clamp(
                staleAfterMonths == null ? DEFAULT_STALE_AFTER_MONTHS : staleAfterMonths,
                1,
                MAX_MONTHS
        );
        LocalDate today = LocalDate.now();

        return buildHistory(
                normalizedMonths,
                normalizedStaleAfterMonths,
                today,
                LocalDateTime.now()
        );
    }

    InstitutionalScientometricHistoryResponse buildHistory(
            int months,
            int staleAfterMonths,
            LocalDate today,
            LocalDateTime generatedAt
    ) {
        int normalizedMonths = clamp(months, MIN_MONTHS, MAX_MONTHS);
        int normalizedStaleAfterMonths = clamp(staleAfterMonths, 1, MAX_MONTHS);

        List<Researcher> activeResearchers = researcherRepository.findAll()
                .stream()
                .filter(researcher -> Boolean.TRUE.equals(researcher.getActive()))
                .toList();

        Set<UUID> activeResearcherIds = activeResearchers.stream()
                .map(Researcher::getId)
                .collect(HashSet::new, Set::add, Set::addAll);

        Comparator<ScientometricMetric> metricComparator = Comparator
                .comparing(ScientometricMetric::getSnapshotDate)
                .thenComparing(
                        metric -> metric.getCreatedAt() == null
                                ? LocalDateTime.MIN
                                : metric.getCreatedAt()
                );

        List<ScientometricMetric> metrics = scientometricMetricRepository.findAll()
                .stream()
                .filter(metric -> metric.getResearcher() != null)
                .filter(metric -> metric.getResearcher().getId() != null)
                .filter(metric -> activeResearcherIds.contains(metric.getResearcher().getId()))
                .filter(metric -> metric.getSnapshotDate() != null)
                .sorted(metricComparator)
                .toList();

        Map<UUID, List<ScientometricMetric>> metricsByResearcher = new HashMap<>();
        for (ScientometricMetric metric : metrics) {
            metricsByResearcher
                    .computeIfAbsent(metric.getResearcher().getId(), ignored -> new ArrayList<>())
                    .add(metric);
        }

        YearMonth finalMonth = YearMonth.from(today);
        YearMonth firstMonth = finalMonth.minusMonths(normalizedMonths - 1L);
        List<InstitutionalScientometricHistoryPoint> timeline = new ArrayList<>();

        for (int monthIndex = 0; monthIndex < normalizedMonths; monthIndex++) {
            YearMonth month = firstMonth.plusMonths(monthIndex);
            LocalDate periodStart = month.atDay(1);
            LocalDate periodEnd = month.atEndOfMonth();
            List<ScientometricMetric> latestMetrics = new ArrayList<>();

            for (Researcher researcher : activeResearchers) {
                ScientometricMetric latest = latestAtOrBefore(
                        metricsByResearcher.getOrDefault(researcher.getId(), List.of()),
                        periodEnd
                );
                if (latest != null) {
                    latestMetrics.add(latest);
                }
            }

            timeline.add(toPoint(
                    periodStart,
                    periodEnd,
                    activeResearchers.size(),
                    latestMetrics
            ));
        }

        LocalDate latestSnapshotDate = metrics.stream()
                .map(ScientometricMetric::getSnapshotDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        long researchersWithMetrics = activeResearchers.stream()
                .filter(researcher -> !metricsByResearcher
                        .getOrDefault(researcher.getId(), List.of())
                        .isEmpty())
                .count();

        long researchersWithoutMetrics = activeResearchers.size() - researchersWithMetrics;
        LocalDate recentThreshold = today.minusMonths(normalizedStaleAfterMonths);

        long researchersWithoutRecentMetrics = activeResearchers.stream()
                .filter(researcher -> {
                    List<ScientometricMetric> researcherMetrics = metricsByResearcher
                            .getOrDefault(researcher.getId(), List.of());
                    if (researcherMetrics.isEmpty()) {
                        return true;
                    }
                    ScientometricMetric latest = researcherMetrics.get(researcherMetrics.size() - 1);
                    return latest.getSnapshotDate().isBefore(recentThreshold);
                })
                .count();

        InstitutionalScientometricHistoryPoint currentPoint = timeline.isEmpty()
                ? null
                : timeline.get(timeline.size() - 1);

        return new InstitutionalScientometricHistoryResponse(
                normalizedMonths,
                normalizedStaleAfterMonths,
                firstMonth.atDay(1),
                finalMonth.atEndOfMonth(),
                latestSnapshotDate,
                (long) activeResearchers.size(),
                researchersWithMetrics,
                researchersWithoutMetrics,
                researchersWithoutRecentMetrics,
                currentPoint == null ? 0 : currentPoint.metricCoverageRate(),
                currentPoint == null ? 0 : currentPoint.institutionalEmailVerificationRate(),
                generatedAt,
                List.copyOf(timeline)
        );
    }

    private InstitutionalScientometricHistoryPoint toPoint(
            LocalDate periodStart,
            LocalDate periodEnd,
            int totalActiveResearchers,
            List<ScientometricMetric> latestMetrics
    ) {
        int researchersMeasured = latestMetrics.size();
        int verifiedEmails = (int) latestMetrics.stream()
                .filter(metric -> Boolean.TRUE.equals(metric.getInstitutionalEmailVerified()))
                .filter(metric -> metric.getVerifiedEmail() != null)
                .filter(metric -> !metric.getVerifiedEmail().isBlank())
                .count();

        return new InstitutionalScientometricHistoryPoint(
                periodStart,
                periodEnd,
                researchersMeasured,
                percentage(researchersMeasured, totalActiveResearchers),
                sum(latestMetrics, ScientometricMetric::getCitationsTotal),
                sum(latestMetrics, ScientometricMetric::getCitationsLastSixYears),
                average(latestMetrics, ScientometricMetric::getHIndexTotal),
                average(latestMetrics, ScientometricMetric::getHIndexLastSixYears),
                average(latestMetrics, ScientometricMetric::getDIndex),
                average(latestMetrics, ScientometricMetric::getI10IndexTotal),
                average(latestMetrics, ScientometricMetric::getI10IndexLastSixYears),
                verifiedEmails,
                percentage(verifiedEmails, researchersMeasured)
        );
    }

    private ScientometricMetric latestAtOrBefore(
            List<ScientometricMetric> metrics,
            LocalDate cutoff
    ) {
        for (int index = metrics.size() - 1; index >= 0; index--) {
            ScientometricMetric metric = metrics.get(index);
            if (!metric.getSnapshotDate().isAfter(cutoff)) {
                return metric;
            }
        }
        return null;
    }

    private long sum(
            List<ScientometricMetric> metrics,
            Function<ScientometricMetric, Integer> extractor
    ) {
        return metrics.stream()
                .map(extractor)
                .filter(value -> value != null)
                .mapToLong(Integer::longValue)
                .sum();
    }

    private Double average(
            List<ScientometricMetric> metrics,
            Function<ScientometricMetric, Integer> extractor
    ) {
        double average = metrics.stream()
                .map(extractor)
                .filter(value -> value != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(average)) {
            return null;
        }
        return Math.round(average * 10.0) / 10.0;
    }

    private int percentage(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round((value * 100.0) / total);
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
