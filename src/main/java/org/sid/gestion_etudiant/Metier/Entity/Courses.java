package org.sid.gestion_etudiant.Metier.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Courses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;

    private int credits;

    @Column(nullable = false)
    private boolean archived = false;

    private LocalDateTime archivedAt;

    @OneToMany(mappedBy = "courses")
    @JsonIgnore
    private List<Grade> grades;

    @ManyToMany
    @JoinTable(
            name = "course_teacher",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    private List<Teacher> teachers;
}