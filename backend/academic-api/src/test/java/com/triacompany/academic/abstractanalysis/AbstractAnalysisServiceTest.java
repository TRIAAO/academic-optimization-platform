package com.triacompany.academic.abstractanalysis;

import com.triacompany.academic.openalex.OpenAlexWork;
import com.triacompany.academic.openalex.OpenAlexWorkRepository;
import com.triacompany.academic.openalex.PublicationReviewStatus;
import com.triacompany.academic.profile.AcademicProfileRepository;
import com.triacompany.academic.researcher.Researcher;
import com.triacompany.academic.researcher.ResearcherRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractAnalysisServiceTest {

    @Test
    void reportsOriginalAbstractsOutsideThePtEnLanguageClassification() {
        ResearcherRepository researcherRepository = mock(ResearcherRepository.class);
        AcademicProfileRepository academicProfileRepository = mock(AcademicProfileRepository.class);
        OpenAlexWorkRepository openAlexWorkRepository = mock(OpenAlexWorkRepository.class);
        AbstractAnalysisService service = new AbstractAnalysisService(
                researcherRepository,
                academicProfileRepository,
                openAlexWorkRepository
        );
        UUID researcherId = UUID.randomUUID();
        Researcher researcher = Researcher.builder()
                .id(researcherId)
                .fullName("Pesquisador com cobertura parcial")
                .build();

        OpenAlexWork englishAbstract = confirmedWork(researcher, "English abstract", "en");
        OpenAlexWork unclassifiedAbstract = confirmedWork(researcher, "Abstract sem idioma", null);
        OpenAlexWork missingAbstract = confirmedWork(researcher, null, null);

        when(researcherRepository.findById(researcherId)).thenReturn(Optional.of(researcher));
        when(academicProfileRepository.findByResearcherId(researcherId)).thenReturn(Optional.empty());
        when(openAlexWorkRepository.findByResearcherIdOrderByPublicationYearDescTitleAsc(researcherId))
                .thenReturn(List.of(englishAbstract, unclassifiedAbstract, missingAbstract));

        AbstractAnalysisResponse response = service.analyze(researcherId);

        assertThat(response.confirmedWorks()).isEqualTo(3);
        assertThat(response.worksWithAbstract()).isEqualTo(2);
        assertThat(response.englishCoveragePercentage()).isEqualTo(33);
        assertThat(response.portugueseCoveragePercentage()).isZero();
        assertThat(response.unclassifiedLanguageAbstracts()).isEqualTo(1);
        assertThat(response.missingAbstracts()).isEqualTo(1);
    }

    private OpenAlexWork confirmedWork(Researcher researcher, String abstractText, String language) {
        return OpenAlexWork.builder()
                .id(UUID.randomUUID())
                .researcher(researcher)
                .openAlexId("https://openalex.org/" + UUID.randomUUID())
                .title("Obra confirmada")
                .abstractText(abstractText)
                .abstractLanguage(language)
                .reviewStatus(PublicationReviewStatus.CONFIRMED)
                .build();
    }
}
