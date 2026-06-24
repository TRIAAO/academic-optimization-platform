package com.triacompany.academic.researcher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "researchers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Researcher {

    @Id
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 180)
    private String fullName;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 180)
    private String institution;

    @Column(length = 180)
    private String department;

    @Column(name = "academic_title", length = 120)
    private String academicTitle;

    @Column(name = "orcid_id", length = 50)
    private String orcidId;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (country == null || country.isBlank()) {
            country = "Angola";
        }

        if (active == null) {
            active = true;
        }

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}