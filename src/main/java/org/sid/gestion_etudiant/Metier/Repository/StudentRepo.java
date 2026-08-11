package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudentRepo extends JpaRepository<Student, Long> {

    List<Student> findByNomContainsIgnoreCaseAndArchivedFalse(String nom);

    List<Student> findByArchivedFalse();

    List<Student> findByArchivedTrue();

    List<Student> findByArchivedTrueAndArchivedAtBefore(
            LocalDateTime date
    );

    Optional<Student> findByPrenomIgnoreCaseAndNomIgnoreCase(
            String prenom,
            String nom
    );

    Optional<Student> findByNomIgnoreCase(String nom);

    Optional<Student> findByKeycloakId(String keycloakId);

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}