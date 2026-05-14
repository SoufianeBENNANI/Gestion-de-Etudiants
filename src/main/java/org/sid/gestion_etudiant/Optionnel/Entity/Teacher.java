package org.sid.gestion_etudiant.Optionnel.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.Entity.Courses;

import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;
}
