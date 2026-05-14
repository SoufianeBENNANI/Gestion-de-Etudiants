package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Courses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CoursesRepo extends JpaRepository<Courses, Long> {

    List<Courses> findByArchivedFalse();

    List<Courses> findByArchivedTrue();

    List<Courses> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);
}