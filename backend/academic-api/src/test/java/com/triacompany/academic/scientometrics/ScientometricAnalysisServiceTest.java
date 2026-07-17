package com.triacompany.academic.scientometrics;

import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScientometricAnalysisServiceTest {

    @Mock
    private ScientometricMetricRepository scientometricMetricRepository;

    @Mock
    private ResearcherRepository researcherRepository;

    @InjectMocks
    private ScientometricAnalysisService service;

    private UUID researcherId;
    private Researcher researcher;

    @BeforeEach
    void setUp() {
        researcherId = UUID.randomUUID();
        researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Pesquisador Teste")
                .email("pesquisador@universidade.ao")
                .institution("Universidade Metropolitana de Angola")
                .country("Angola")
                .active(true)
                .build();

        ReflectionTestUtils.setField(
                service,
                "institutionalEmailDomains",
                "universidade.ao,imetroangola.com"
        );
        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
    }

    @Test
    void shouldDetectHighDIndexDeviationAndActiveVitality() {
        ScientometricMetric latest = metric(
                LocalDate.of(2026, 7, 1),
                100,
                70,
                10,
                6,
                8,
                4,
                5,
                "pesquisador@universidade.ao",
                true
        );
        ScientometricMetric previous = metric(
                LocalDate.of(2026, 1, 1),
                80,
                50,
                8,
                4,
                7,
                3,
                5,
                "pesquisador@universidade.ao",
                true
        );

        when(scientometricMetricRepository
                .findByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(researcherId))
                .thenReturn(List.of(latest, previous));

        ScientometricAnalysisResponse response = service.analyze(researcherId);

        assertEquals("CRITICAL", response.dIndexAssessment().status());
        assertEquals(50, response.dIndexAssessment().deviationPercent());
        assertEquals("ACTIVE", response.vitalityAssessment().status());
        assertEquals(63, response.vitalityAssessment().score());
        assertEquals("GROWING", response.vitalityAssessment().trend());
        assertEquals(20, response.vitalityAssessment().citationsDelta());
        assertEquals("VERIFIED", response.institutionalEmailAssessment().status());
        assertTrue(response.alerts().stream()
                .anyMatch(alert -> "D_INDEX_DEVIATION_HIGH".equals(alert.code())));
    }

    @Test
    void shouldFlagInstitutionalEmailMismatchAndLowVitality() {
        ScientometricMetric latest = metric(
                LocalDate.of(2026, 7, 1),
                100,
                10,
                10,
                2,
                10,
                1,
                9,
                "pesquisador@gmail.com",
                true
        );

        when(scientometricMetricRepository
                .findByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(researcherId))
                .thenReturn(List.of(latest));

        ScientometricAnalysisResponse response = service.analyze(researcherId);

        assertEquals("STAGNANT", response.vitalityAssessment().status());
        assertEquals(13, response.vitalityAssessment().score());
        assertEquals("INCONSISTENT", response.institutionalEmailAssessment().status());
        assertTrue(response.alerts().stream()
                .anyMatch(alert -> "INSTITUTIONAL_EMAIL_MISMATCH".equals(alert.code())));
        assertTrue(response.alerts().stream()
                .anyMatch(alert -> "VITALITY_LOW".equals(alert.code())));
    }

    @Test
    void shouldReturnNoDataAssessmentWhenResearcherHasNoMeasurements() {
        when(scientometricMetricRepository
                .findByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(researcherId))
                .thenReturn(List.of());

        ScientometricAnalysisResponse response = service.analyze(researcherId);

        assertEquals(0, response.measurementCount());
        assertEquals("NOT_AVAILABLE", response.dIndexAssessment().status());
        assertEquals("NOT_AVAILABLE", response.vitalityAssessment().status());
        assertEquals("NOT_INFORMED", response.institutionalEmailAssessment().status());
        assertEquals("NO_MEASUREMENTS", response.alerts().get(0).code());
    }

    private ScientometricMetric metric(
            LocalDate snapshotDate,
            Integer citationsTotal,
            Integer citationsRecent,
            Integer hIndexTotal,
            Integer hIndexRecent,
            Integer i10Total,
            Integer i10Recent,
            Integer dIndex,
            String verifiedEmail,
            boolean institutionalVerified
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
                .verifiedEmail(verifiedEmail)
                .institutionalEmailVerified(institutionalVerified)
                .snapshotDate(snapshotDate)
                .build();
    }
}
