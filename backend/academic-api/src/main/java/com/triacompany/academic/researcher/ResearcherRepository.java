package com.triacompany.academic.researcher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ResearcherRepository extends JpaRepository<Researcher, UUID> {

    Optional<Researcher> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}