package org.sid.gestion_etudiant.Metier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author Soufiane
 */
public record CreateTeacherRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'adresse email n'est pas valide")
        String email,

        @NotBlank(message = "La spécialité est obligatoire")
        String specialite,

        @NotBlank(message = "Le nom du département est obligatoire")
        String departementNom

) {
}