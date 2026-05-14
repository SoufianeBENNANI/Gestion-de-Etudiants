package org.sid.gestion_etudiant.Optionnel.DTO;

import lombok.Data;

@Data
public class TeacherDTO {
    private String nom;
    private String prenom;
    private String email;
    private String specialite;

    private Long departementId;
}
