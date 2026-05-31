package org.sid.gestion_etudiant.Optionnel.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartementDTO {

    private Long id;

    private String nom;

    private String description;

    private boolean archived;

    private LocalDateTime archivedAt;
}