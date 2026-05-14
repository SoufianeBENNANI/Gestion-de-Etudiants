    package org.sid.gestion_etudiant.Metier.Entity;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import jakarta.persistence.*;
    import lombok.Data;
    import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
    import org.sid.gestion_etudiant.Metier.enums.GenreStu;

    import java.time.LocalDateTime;
    import java.util.List;

    @Entity
    @Data
    public class Student {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String nom;
        private String prenom;
        private String email;
        private LocalDateTime date_Naissance;

        @Enumerated(EnumType.STRING)
        private GenreStu genre;

        private String adresse;
        private String telephone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        @Column(nullable = false)
        private boolean archived = false;

        private LocalDateTime archivedAt;


        @PrePersist
        public void onCreate() {
            createdAt = LocalDateTime.now();
        }

        @PreUpdate
        public void onUpdate() {
            updatedAt = LocalDateTime.now();
        }

        @ManyToMany
        @JoinTable(
                name = "student_class",
                joinColumns = @JoinColumn(name = "student_id"),
                inverseJoinColumns = @JoinColumn(name = "class_id")
        )
        private List<ClassesEntity> classes;

        @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE, orphanRemoval = true)
        private List<Grade> grades;

        @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE, orphanRemoval = true)
        private List<Attendance> attendances;

        @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE, orphanRemoval = true)
        private List<Payement> payments;

        @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE, orphanRemoval = true)
        @JsonIgnore
        private List<StudentIAPrediction> predictions;
    }