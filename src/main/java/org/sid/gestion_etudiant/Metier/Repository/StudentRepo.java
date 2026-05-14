package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StudentRepo extends JpaRepository<Student, Long> {

    List<Student> findByNomContainsIgnoreCase(String nom);

    List<Student> findByNomContainsIgnoreCaseAndArchivedFalse(String nom);

    List<Student> findByArchivedFalse();

    List<Student> findByArchivedTrue();

    List<Student> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);
}