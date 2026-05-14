package org.sid.gestion_etudiant.Metier.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.enums.NiveauClasse;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class ClassesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Enumerated(EnumType.STRING)
    private NiveauClasse niveau;

    private String annee;

    @Column(nullable = false)
    private boolean archived = false;

    private LocalDateTime archivedAt;

    @ManyToMany(mappedBy = "classes")
    private List<Student> students;
}