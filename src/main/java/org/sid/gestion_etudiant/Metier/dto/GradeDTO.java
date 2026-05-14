package org.sid.gestion_etudiant.Metier.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GradeDTO {

    private Long id;

    private Double note;

    private String semestre;

    private Long studentId;

    private Long courseId;

    private boolean archived;

    private LocalDateTime archivedAt;
}