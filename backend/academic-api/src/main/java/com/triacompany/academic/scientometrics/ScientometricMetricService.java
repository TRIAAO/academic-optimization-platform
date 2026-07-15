package com.triacompany.academic.scientometrics;

import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScientometricMetricService {

    private static final String DEFAULT_SOURCE = "MANUAL_GOOGLE_SCHOLAR";

    private final ScientometricMetricRepository scientometricMetricRepository;
    private final ResearcherRepository researcherRepository;

    @Transactional
    public ScientometricMetricResponse create(UUID researcherId, CreateScientometricMetricRequest request) {
        Researcher researcher = findResearcherById(researcherId);

        String source = normalizeSource(request.source());
        LocalDate snapshotDate = request.snapshotDate() != null ? request.snapshotDate() : LocalDate.now();

        if (scientometricMetricRepository.existsByResearcher_IdAndSourceIgnoreCaseAndSnapshotDate(
                researcherId,
                source,
                snapshotDate
        )) {
            throw new IllegalArgumentException("Já existe uma medição cientométrica para este pesquisador, fonte e data.");
        }

        ScientometricMetric metric = ScientometricMetric.builder()
                .researcher(researcher)
                .source(source)
                .googleScholarAuthorId(normalizeNullable(request.googleScholarAuthorId()))
                .googleScholarProfileUrl(normalizeNullable(request.googleScholarProfileUrl()))
                .hIndexTotal(request.hIndexTotal())
                .hIndexLastSixYears(request.hIndexLastSixYears())
                .i10IndexTotal(request.i10IndexTotal())
                .i10IndexLastSixYears(request.i10IndexLastSixYears())
                .citationsTotal(request.citationsTotal())
                .citationsLastSixYears(request.citationsLastSixYears())
                .dIndex(request.dIndex())
                .verifiedEmail(normalizeNullable(request.verifiedEmail()))
                .institutionalEmailVerified(Boolean.TRUE.equals(request.institutionalEmailVerified()))
                .interests(normalizeNullable(request.interests()))
                .notes(normalizeNullable(request.notes()))
                .snapshotDate(snapshotDate)
                .build();

        ScientometricMetric saved = scientometricMetricRepository.save(metric);

        return ScientometricMetricResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ScientometricMetricResponse> findByResearcher(UUID researcherId) {
        ensureResearcherExists(researcherId);

        return scientometricMetricRepository.findByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(researcherId)
                .stream()
                .map(ScientometricMetricResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScientometricMetricResponse findLatestByResearcher(UUID researcherId) {
        ensureResearcherExists(researcherId);

        ScientometricMetric metric = scientometricMetricRepository
                .findTopByResearcher_IdOrderBySnapshotDateDescCreatedAtDesc(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma métrica cientométrica encontrada para este pesquisador."));

        return ScientometricMetricResponse.fromEntity(metric);
    }

    @Transactional(readOnly = true)
    public ScientometricMetricResponse findById(UUID id) {
        ScientometricMetric metric = findMetricById(id);
        return ScientometricMetricResponse.fromEntity(metric);
    }

    @Transactional
    public ScientometricMetricResponse update(UUID id, UpdateScientometricMetricRequest request) {
        ScientometricMetric metric = findMetricById(id);

        if (request.source() != null) {
            metric.setSource(normalizeSource(request.source()));
        }

        if (request.googleScholarAuthorId() != null) {
            metric.setGoogleScholarAuthorId(normalizeNullable(request.googleScholarAuthorId()));
        }

        if (request.googleScholarProfileUrl() != null) {
            metric.setGoogleScholarProfileUrl(normalizeNullable(request.googleScholarProfileUrl()));
        }

        if (request.hIndexTotal() != null) {
            metric.setHIndexTotal(request.hIndexTotal());
        }

        if (request.hIndexLastSixYears() != null) {
            metric.setHIndexLastSixYears(request.hIndexLastSixYears());
        }

        if (request.i10IndexTotal() != null) {
            metric.setI10IndexTotal(request.i10IndexTotal());
        }

        if (request.i10IndexLastSixYears() != null) {
            metric.setI10IndexLastSixYears(request.i10IndexLastSixYears());
        }

        if (request.citationsTotal() != null) {
            metric.setCitationsTotal(request.citationsTotal());
        }

        if (request.citationsLastSixYears() != null) {
            metric.setCitationsLastSixYears(request.citationsLastSixYears());
        }

        if (request.dIndex() != null) {
            metric.setDIndex(request.dIndex());
        }

        if (request.verifiedEmail() != null) {
            metric.setVerifiedEmail(normalizeNullable(request.verifiedEmail()));
        }

        if (request.institutionalEmailVerified() != null) {
            metric.setInstitutionalEmailVerified(request.institutionalEmailVerified());
        }

        if (request.interests() != null) {
            metric.setInterests(normalizeNullable(request.interests()));
        }

        if (request.notes() != null) {
            metric.setNotes(normalizeNullable(request.notes()));
        }

        if (request.snapshotDate() != null) {
            metric.setSnapshotDate(request.snapshotDate());
        }

        if (scientometricMetricRepository.existsDuplicateSnapshotExcludingId(
                metric.getResearcher().getId(),
                metric.getSource(),
                metric.getSnapshotDate(),
                metric.getId()
        )) {
            throw new IllegalArgumentException("Já existe outra medição cientométrica para este pesquisador, fonte e data.");
        }

        ScientometricMetric saved = scientometricMetricRepository.save(metric);

        return ScientometricMetricResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(UUID id) {
        ScientometricMetric metric = findMetricById(id);
        scientometricMetricRepository.delete(metric);
    }

    private void ensureResearcherExists(UUID researcherId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }
    }

    private Researcher findResearcherById(UUID researcherId) {
        return researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
    }

    private ScientometricMetric findMetricById(UUID id) {
        return scientometricMetricRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Métrica cientométrica não encontrada."));
    }

    private String normalizeSource(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_SOURCE;
        }

        return value.trim().toUpperCase().replace(" ", "_");
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}