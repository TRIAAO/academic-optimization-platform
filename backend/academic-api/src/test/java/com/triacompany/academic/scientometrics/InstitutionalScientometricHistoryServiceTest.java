package com.triacompany.academic.scientometrics;

import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstitutionalScientometricHistoryServiceTest {

    @Mock
    private ScientometricMetricRepository scientometricMetricRepository;

    @Mock
    private ResearcherRepository researcherRepository;

    @InjectMocks
    private InstitutionalScientometricHistoryService service;

    @Test
    void shouldBuildCarryForwardInstitutionalSeriesAndIgnoreInactiveResearchers() {
        Researcher first = researcher("Primeiro Pesquisador", true);
        Researcher second = researcher("Segundo Pesquisador", true);
        Researcher withoutMetrics = researcher("Sem Medição", true);
        Researcher inactive = researcher("Pesquisador Inativo", false);

        ScientometricMetric firstJanuary = metric(
                first,
                LocalDate.of(2026, 1, 10),
                100,
                60,
                10,
                6,
                8,
                4,
                8,
                true
        );
        ScientometricMetric firstJune = metric(
                first,
                LocalDate.of(2026, 6, 1),
                150,
                90,
                12,
                8,
                10,
                6,
                10,
                true
        );
        ScientometricMetric secondMarch = metric(
                second,
                LocalDate.of(2026, 3, 15),
                50,
                20,
                8,
                3,
                4,
                2,
                8,
                false
        );
        ScientometricMetric inactiveMetric = metric(
                inactive,
                LocalDate.of(2026, 7, 1),
                999,
                999,
                99,
                99,
                99,
                99,
                99,
                true
        );

        when(researcherRepository.findAll())
                .thenReturn(List.of(first, second, withoutMetrics, inactive));
        when(scientometricMetricRepository.findAll())
                .thenReturn(List.of(firstJanuary, firstJune, secondMarch, inactiveMetric));

        InstitutionalScientometricHistoryResponse response = service.buildHistory(
                6,
                6,
                LocalDate.of(2026, 7, 18),
                LocalDateTime.of(2026, 7, 18, 12, 0)
        );

        assertEquals(6, response.requestedMonths());
        assertEquals(3L, response.totalActiveResearchers());
        assertEquals(2L, response.researchersWithMetrics());
        assertEquals(1L, response.researchersWithoutMetrics());
        assertEquals(1L, response.researchersWithoutRecentMetrics());
        assertEquals(LocalDate.of(2026, 6, 1), response.latestSnapshotDate());
        assertEquals(6, response.timeline().size());

        InstitutionalScientometricHistoryPoint february = response.timeline().get(0);
        assertEquals(LocalDate.of(2026, 2, 1), february.periodStart());
        assertEquals(1, february.researchersMeasured());
        assertEquals(33, february.metricCoverageRate());
        assertEquals(100L, february.citationsTotal());

        InstitutionalScientometricHistoryPoint july = response.timeline().get(5);
        assertEquals(2, july.researchersMeasured());
        assertEquals(67, july.metricCoverageRate());
        assertEquals(200L, july.citationsTotal());
        assertEquals(110L, july.citationsLastSixYears());
        assertEquals(10.0, july.averageHIndexTotal());
        assertEquals(9.0, july.averageDIndex());
        assertEquals(1, july.institutionalEmailVerifiedResearchers());
        assertEquals(50, july.institutionalEmailVerificationRate());
    }

    @Test
    void shouldClampRequestedPeriodToMinimumSixMonths() {
        when(researcherRepository.findAll()).thenReturn(List.of());
        when(scientometricMetricRepository.findAll()).thenReturn(List.of());

        InstitutionalScientometricHistoryResponse response = service.buildHistory(
                3,
                12,
                LocalDate.of(2026, 7, 18),
                LocalDateTime.of(2026, 7, 18, 12, 0)
        );

        assertEquals(6, response.requestedMonths());
        assertEquals(6, response.timeline().size());
        assertEquals(0, response.currentMetricCoverageRate());
        assertEquals(0, response.currentInstitutionalEmailVerificationRate());
    }

    private Researcher researcher(String name, boolean active) {
        return Researcher.builder()
                .id(UUID.randomUUID())
                .fullName(name)
                .email(name.toLowerCase().replace(" ", ".") + "@universidade.ao")
                .country("Angola")
                .active(active)
                .build();
    }

    private ScientometricMetric metric(
            Researcher researcher,
            LocalDate snapshotDate,
            Integer citationsTotal,
            Integer citationsRecent,
            Integer hIndexTotal,
            Integer hIndexRecent,
            Integer i10Total,
            Integer i10Recent,
            Integer dIndex,
            boolean institutionalEmailVerified
    ) {
        return ScientometricMetric.builder()
                .id(UUID.randomUUID())
                .researcher(researcher)
                .source("MANUAL_GOOGLE_SCHOLAR")
                .citationsTotal(citationsTotal)
                .citationsLastSixYears(citationsRecent)
                .hIndexTotal(hIndexTotal)
                .hIndexLastSixYears(hIndexRecent)
                .i10IndexTotal(i10Total)
                .i10IndexLastSixYears(i10Recent)
                .dIndex(dIndex)
                .verifiedEmail(researcher.getEmail())
                .institutionalEmailVerified(institutionalEmailVerified)
                .snapshotDate(snapshotDate)
                .createdAt(snapshotDate.atStartOfDay())
                .build();
    }
}
