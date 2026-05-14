package org.sid.gestion_etudiant.Metier.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CoursesDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @Min(value = 1, message = "Les crédits doivent être supérieurs à 0")
    private int credits;

    private boolean archived;

    private LocalDateTime archivedAt;
}