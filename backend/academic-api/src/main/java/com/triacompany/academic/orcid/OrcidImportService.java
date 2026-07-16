package com.triacompany.academic.orcid;

import com.fasterxml.jackson.databind.JsonNode;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrcidImportService {

    private final ResearcherRepository researcherRepository;
    private final OrcidClient orcidClient;
    private final OrcidWorkRepository orcidWorkRepository;
    private final OrcidImportLogRepository orcidImportLogRepository;

    @Transactional
    public OrcidImportResponse importWorks(UUID researcherId) {
        Researcher researcher = researcherRepository.findById(researcherId)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));

        String orcidId = OrcidId.normalize(researcher.getOrcidId());

        if (orcidId == null) {
            throw new IllegalArgumentException("Este pesquisador não possui ORCID informado.");
        }

        OrcidImportLog log = OrcidImportLog.builder()
                .researcher(researcher)
                .orcidId(orcidId)
                .status(OrcidImportStatus.FAILED)
                .message("Importação iniciada.")
                .totalFound(0)
                .totalImported(0)
                .startedAt(LocalDateTime.now())
                .build();

        log = orcidImportLogRepository.save(log);

        try {
            JsonNode response = orcidClient.fetchWorks(orcidId);
            List<OrcidWork> parsedWorks = parseWorks(response, researcher, log, orcidId);

            int totalFound = parsedWorks.size();
            List<OrcidWork> importedWorks = new ArrayList<>();

            for (OrcidWork parsedWork : parsedWorks) {
                if (parsedWork.getPutCode() != null &&
                        orcidWorkRepository.existsByResearcherIdAndPutCode(researcher.getId(), parsedWork.getPutCode())) {
                    continue;
                }

                OrcidWork saved = orcidWorkRepository.save(parsedWork);
                importedWorks.add(saved);
            }

            log.setStatus(OrcidImportStatus.SUCCESS);
            log.setMessage("Importação ORCID concluída com sucesso.");
            log.setTotalFound(totalFound);
            log.setTotalImported(importedWorks.size());
            log.setFinishedAt(LocalDateTime.now());

            OrcidImportLog savedLog = orcidImportLogRepository.save(log);

            return new OrcidImportResponse(
                    OrcidImportLogResponse.fromEntity(savedLog),
                    importedWorks.stream().map(OrcidWorkResponse::fromEntity).toList()
            );
        } catch (RuntimeException exception) {
            log.setStatus(OrcidImportStatus.FAILED);
            log.setMessage(exception.getMessage());
            log.setFinishedAt(LocalDateTime.now());
            orcidImportLogRepository.save(log);

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<OrcidWorkResponse> findWorksByResearcher(UUID researcherId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }

        return orcidWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId)
                .stream()
                .map(OrcidWorkResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrcidImportLogResponse> findImportLogsByResearcher(UUID researcherId) {
        if (!researcherRepository.existsById(researcherId)) {
            throw new IllegalArgumentException("Pesquisador não encontrado.");
        }

        return orcidImportLogRepository.findByResearcherIdOrderByStartedAtDesc(researcherId)
                .stream()
                .map(OrcidImportLogResponse::fromEntity)
                .toList();
    }

    private List<OrcidWork> parseWorks(
            JsonNode response,
            Researcher researcher,
            OrcidImportLog log,
            String orcidId
    ) {
        List<OrcidWork> works = new ArrayList<>();

        JsonNode groups = response.path("group");

        if (!groups.isArray()) {
            return works;
        }

        for (JsonNode group : groups) {
            JsonNode summaries = group.path("work-summary");

            if (!summaries.isArray()) {
                continue;
            }

            for (JsonNode summary : summaries) {
                String title = text(summary, "title", "title", "value");

                if (title == null || title.isBlank()) {
                    continue;
                }

                OrcidWork work = OrcidWork.builder()
                        .researcher(researcher)
                        .importLog(log)
                        .orcidId(orcidId)
                        .putCode(text(summary, "put-code"))
                        .title(title)
                        .workType(text(summary, "type"))
                        .publicationYear(integer(summary, "publication-date", "year", "value"))
                        .publicationMonth(integer(summary, "publication-date", "month", "value"))
                        .publicationDay(integer(summary, "publication-date", "day", "value"))
                        .journalTitle(text(summary, "journal-title", "value"))
                        .doi(extractDoi(summary))
                        .externalUrl(text(summary, "url", "value"))
                        .sourceName(text(summary, "source", "source-name", "value"))
                        .rawSource("ORCID")
                        .build();

                works.add(work);
            }
        }

        return works;
    }

    private String extractDoi(JsonNode summary) {
        JsonNode externalIds = summary.path("external-ids").path("external-id");

        if (!externalIds.isArray()) {
            return null;
        }

        for (JsonNode externalId : externalIds) {
            String type = text(externalId, "external-id-type");

            if (type != null && type.equalsIgnoreCase("doi")) {
                return normalizeNullable(text(externalId, "external-id-value"));
            }
        }

        return null;
    }

    private String text(JsonNode node, String... path) {
        JsonNode current = node;

        for (String part : path) {
            current = current.path(part);
        }

        if (current.isMissingNode() || current.isNull()) {
            return null;
        }

        if (current.isNumber()) {
            return current.asText();
        }

        String value = current.asText(null);
        return normalizeNullable(value);
    }

    private Integer integer(JsonNode node, String... path) {
        String value = text(node, path);

        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
