package org.sid.gestion_etudiant.Metier.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.enums.PayementStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Payement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private PayementStatus status;

    @Column(nullable = false)
    private boolean archived = false;

    private LocalDateTime archivedAt;

    @ManyToOne
    @JsonIgnore
    private Student student;
}