package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GradeRepo extends JpaRepository<Grade, Long> {

    List<Grade> findByArchivedFalse();

    List<Grade> findByArchivedTrue();

    List<Grade> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);
}