package org.sid.gestion_etudiant.Optionnel.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
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

    private boolean archived = false;

    private LocalDateTime archivedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "departement")
    private List<Teacher> teachers = new ArrayList<>();
}