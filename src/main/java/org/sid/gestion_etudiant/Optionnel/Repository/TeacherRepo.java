package org.sid.gestion_etudiant.Optionnel.Repository;

import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeacherRepo extends JpaRepository<Teacher, Long> {

    List<Teacher> findByArchivedFalse();

    List<Teacher> findByArchivedTrue();

    List<Teacher> findByNomContainingIgnoreCaseAndArchivedFalse(
            String nom
    );

    List<Teacher> findByArchivedTrueAndArchivedAtBefore(
            LocalDateTime date
    );

    Optional<Teacher> findByKeycloakId(String keycloakId);

    boolean existsByEmailIgnoreCase(String email);
}