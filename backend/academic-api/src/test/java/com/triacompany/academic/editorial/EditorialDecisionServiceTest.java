package com.triacompany.academic.editorial;

import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EditorialDecisionServiceTest {

    private EditorialDecisionRepository editorialDecisionRepository;
    private ResearcherRepository researcherRepository;
    private OpenAlexWorkRepository openAlexWorkRepository;
    private EditorialDecisionService service;
    private Researcher researcher;
    private OpenAlexWork work;

    @BeforeEach
    void setUp() {
        editorialDecisionRepository = mock(EditorialDecisionRepository.class);
        researcherRepository = mock(ResearcherRepository.class);
        openAlexWorkRepository = mock(OpenAlexWorkRepository.class);
        service = new EditorialDecisionService(
                editorialDecisionRepository,
                researcherRepository,
                openAlexWorkRepository
        );

        UUID researcherId = UUID.randomUUID();
        researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Pesquisador editorial")
                .build();
        work = OpenAlexWork.builder()
                .id(UUID.randomUUID())
                .researcher(researcher)
                .openAlexId("https://openalex.org/W123")
                .title("Obra confirmada com abstract")
                .reviewStatus(PublicationReviewStatus.CONFIRMED)
                .build();

        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
        when(researcherRepository.existsById(researcherId)).thenReturn(true);
        when(openAlexWorkRepository.findById(work.getId())).thenReturn(Optional.of(work));
        when(editorialDecisionRepository.findByResearcherIdAndOpenAlexWorkId(researcherId, work.getId()))
                .thenReturn(Optional.empty());
        when(editorialDecisionRepository.save(any(EditorialDecision.class))).thenAnswer(invocation -> {
            EditorialDecision decision = invocation.getArgument(0);
            decision.prePersist();
            return decision;
        });
    }

    @Test
    void allowsAnInProgressDecisionWithPartialChecklist() {
        EditorialDecisionResponse response = service.save(
                researcher.getId(),
                work.getId(),
                request(EditorialDecisionStatus.UNDER_REVIEW, false, null, null),
                "admin@imetro.ao"
        );

        assertThat(response.status()).isEqualTo(EditorialDecisionStatus.UNDER_REVIEW);
        assertThat(response.confirmedCriteria()).isZero();
        assertThat(response.reviewedBy()).isEqualTo("admin@imetro.ao");
    }

    @Test
    void blocksApprovalUntilAllGovernanceCriteriaAreConfirmed() {
        SaveEditorialDecisionRequest request = request(
                EditorialDecisionStatus.APPROVED,
                false,
                "https://periodico.exemplo.org",
                null
        );

        assertThatThrownBy(() -> service.save(
                researcher.getId(),
                work.getId(),
                request,
                "admin@imetro.ao"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("seis critérios editoriais");
    }

    @Test
    void storesAnApprovedDecisionWithCompleteTraceability() {
        EditorialDecisionResponse response = service.save(
                researcher.getId(),
                work.getId(),
                request(
                        EditorialDecisionStatus.APPROVED,
                        true,
                        "https://periodico.exemplo.org/submissions",
                        "Critérios confirmados no site oficial."
                ),
                "admin@imetro.ao"
        );

        assertThat(response.status()).isEqualTo(EditorialDecisionStatus.APPROVED);
        assertThat(response.confirmedCriteria()).isEqualTo(6);
        assertThat(response.officialUrl()).isEqualTo("https://periodico.exemplo.org/submissions");
        assertThat(response.issns()).containsExactly("1234-5678", "8765-4321");
    }

    private SaveEditorialDecisionRequest request(
            EditorialDecisionStatus status,
            boolean allCriteria,
            String officialUrl,
            String notes
    ) {
        return new SaveEditorialDecisionRequest(
                "Revista de Ciência Aplicada",
                "Editora Acadêmica",
                List.of("1234-5678", "8765-4321"),
                69,
                officialUrl,
                status,
                allCriteria,
                allCriteria,
                allCriteria,
                allCriteria,
                allCriteria,
                allCriteria,
                notes
        );
    }
}
