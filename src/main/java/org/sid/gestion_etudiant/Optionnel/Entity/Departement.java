package org.sid.gestion_etudiant.Optionnel.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.Entity.Student;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Departement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "departement")
    private List<Teacher> teachers;
}
