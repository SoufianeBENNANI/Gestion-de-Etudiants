package org.sid.gestion_etudiant.Metier.dto;

import lombok.Data;
import org.sid.gestion_etudiant.Metier.enums.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceDTO {

    private Long id;

    private LocalDate date;

    private AttendanceStatus status;

    private Long studentId;

    private String studentNom;

    private String studentPrenom;

    private String studentEmail;

    private boolean archived;

    private LocalDateTime archivedAt;
}