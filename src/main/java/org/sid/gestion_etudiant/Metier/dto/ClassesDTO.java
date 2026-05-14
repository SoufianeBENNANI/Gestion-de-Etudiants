package org.sid.gestion_etudiant.Metier.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.enums.NiveauClasse;

import java.time.LocalDateTime;

@Data
public class ClassesDTO {

    private Long id;

    @NotNull
    private String nom;

    private NiveauClasse niveau;

    private String annee;

    private boolean archived;

    private LocalDateTime archivedAt;
}