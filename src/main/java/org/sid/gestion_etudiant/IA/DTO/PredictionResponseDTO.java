package org.sid.gestion_etudiant.IA.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PredictionResponseDTO {

    private Long id;

    private double moyenne;

    private long absences;

    private String prediction;

    private Double scoreRisque;

    private String niveau;

    private String recommandation;

    private LocalDateTime date;

    private String status;

    private String modelVersion;

    private Long studentId;

    private String nom;

    private String prenom;

    private String email;
}