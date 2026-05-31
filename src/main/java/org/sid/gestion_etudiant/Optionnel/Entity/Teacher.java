package org.sid.gestion_etudiant.Optionnel.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String specialite;

    private boolean archived = false;

    private LocalDateTime archivedAt;

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;
}