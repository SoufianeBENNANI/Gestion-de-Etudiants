package org.sid.gestion_etudiant.Metier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.enums.GenreStu;

import java.time.LocalDateTime;

@Data
public class StudentDTO {

    private Long id;

    @NotNull
    private String nom;

    private String prenom;

    @Email
    private String email;

    private LocalDateTime date_Naissance;

    private GenreStu genre;

    private String adresse;

    private String telephone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean archived;

    private LocalDateTime archivedAt;
}