package org.sid.gestion_etudiant.IA.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.sid.gestion_etudiant.Metier.Entity.Student;

import java.time.LocalDateTime;

@Entity
@Data
public class IALog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String inputData;
    private String outputData;
    private LocalDateTime createdAt;

    @ManyToOne
    private Student student;
}
