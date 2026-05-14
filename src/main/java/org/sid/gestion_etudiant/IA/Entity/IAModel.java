package org.sid.gestion_etudiant.IA.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class IAModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String version;

    private Double accuracy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean archived = false;

    private LocalDateTime archivedAt;
}