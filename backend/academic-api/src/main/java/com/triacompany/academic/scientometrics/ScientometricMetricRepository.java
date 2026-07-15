package com.triacompany.academic.scientometrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScientometricMetricRepository extends JpaRepository<ScientometricMetric, UUID> {

    List<ScientometricMetric> findByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(UUID researcherId);

    Optional<ScientometricMetric> findTopByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(UUID researcherId);

    boolean existsByResearcher_IdAndSourceIgnoreCaseAndSnapshotDate(
            UUID researcherId,
            String source,
            LocalDate snapshotDate
    );

    @Query("""
            SELECT COUNT(metric) > 0
            FROM ScientometricMetric metric
            WHERE metric.researcher.id = :researcherId
              AND LOWER(metric.source) = LOWER(:source)
              AND metric.snapshotDate = :snapshotDate
              AND metric.id <> :metricId
            """)
    boolean existsDuplicateSnapshotExcludingId(
            @Param("researcherId") UUID researcherId,
            @Param("source") String source,
            @Param("snapshotDate") LocalDate snapshotDate,
            @Param("metricId") UUID metricId
    );
}