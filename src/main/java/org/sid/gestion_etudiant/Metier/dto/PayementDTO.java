package org.sid.gestion_etudiant.Metier.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.enums.PayementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PayementDTO {

    private Long id;

    @NotNull(message = "Le montant est obligatoire")
    private Double amount;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le statut est obligatoire")
    private PayementStatus status;

    @NotNull(message = "L'ID de l'étudiant est obligatoire")
    private Long studentId;

    private boolean archived;

    private LocalDateTime archivedAt;
}