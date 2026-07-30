package org.sid.gestion_etudiant.Metier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.sid.gestion_etudiant.Metier.enums.GenreStu;

import java.time.LocalDateTime;

public record CreateStudentRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'adresse email n'est pas valide")
        String email,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDateTime dateNaissance,

        GenreStu genre,

        String adresse,

        @Pattern(
                regexp = "^[0-9+()\\-\\s]{8,20}$",
                message = "Le numéro de téléphone n'est pas valide"
        )
        String telephone
) {
}