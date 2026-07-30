    package org.sid.gestion_etudiant.Optionnel.Entity;
    
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    
    import java.time.LocalDateTime;
    
    @Entity
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class Teacher {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        private String nom;
    
        private String prenom;
    
        private String email;
    
        private String specialite;
    
        @Builder.Default
        @Column(nullable = false)
        private boolean archived = false;
    
        private LocalDateTime archivedAt;
    
        @Column(name = "keycloak_id", unique = true)
        private String keycloakId;
    
        @ManyToOne
        @JoinColumn(name = "departement_id")
        private Departement departement;
    }