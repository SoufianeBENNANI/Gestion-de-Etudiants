package org.sid.gestion_etudiant.IA.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.Entity.Student;

import java.time.LocalDateTime;

@Entity
@Data
public class StudentIAPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double moyenne;

    private long absences;

    private String prediction;

    private Double scoreRisque;

    private String niveau;

    private String recommandation;

    private LocalDateTime date;

    private String status;

    @Column(name = "model_version")
    private String modelVersion;

    @ManyToOne
    @JsonIgnore
    private Student student;
}