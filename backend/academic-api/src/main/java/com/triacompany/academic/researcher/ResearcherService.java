package com.triacompany.academic.researcher;

import com.triacompany.academic.orcid.OrcidId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResearcherService {

    private final ResearcherRepository researcherRepository;

    @Transactional
    public ResearcherResponse create(CreateResearcherRequest request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        if (researcherRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Já existe um pesquisador cadastrado com este e-mail.");
        }

        Researcher researcher = Researcher.builder()
                .fullName(request.fullName().trim())
                .email(normalizedEmail)
                .phone(normalizeNullable(request.phone()))
                .institution(normalizeNullable(request.institution()))
                .department(normalizeNullable(request.department()))
                .academicTitle(normalizeNullable(request.academicTitle()))
                .orcidId(OrcidId.normalize(request.orcidId()))
                .country("Angola")
                .active(true)
                .build();

        Researcher saved = researcherRepository.save(researcher);

        return ResearcherResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ResearcherResponse> findAll() {
        return researcherRepository.findAll()
                .stream()
                .map(ResearcherResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResearcherResponse findById(UUID id) {
        Researcher researcher = findResearcherById(id);
        return ResearcherResponse.fromEntity(researcher);
    }

    @Transactional
    public ResearcherResponse update(UUID id, UpdateResearcherRequest request) {
        Researcher researcher = findResearcherById(id);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            researcher.setFullName(request.fullName().trim());
        }

        if (request.phone() != null) {
            researcher.setPhone(normalizeNullable(request.phone()));
        }

        if (request.institution() != null) {
            researcher.setInstitution(normalizeNullable(request.institution()));
        }

        if (request.department() != null) {
            researcher.setDepartment(normalizeNullable(request.department()));
        }

        if (request.academicTitle() != null) {
            researcher.setAcademicTitle(normalizeNullable(request.academicTitle()));
        }

        if (request.orcidId() != null) {
            researcher.setOrcidId(OrcidId.normalize(request.orcidId()));
        }

        Researcher saved = researcherRepository.save(researcher);

        return ResearcherResponse.fromEntity(saved);
    }

    @Transactional
    public void deactivate(UUID id) {
        Researcher researcher = findResearcherById(id);
        researcher.setActive(false);
        researcherRepository.save(researcher);
    }

    private Researcher findResearcherById(UUID id) {
        return researcherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pesquisador não encontrado."));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
