package org.sid.gestion_etudiant.Optionnel.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherDTO {

    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String specialite;

    private String departementNom;

    private boolean archived;

    private LocalDateTime archivedAt;
}