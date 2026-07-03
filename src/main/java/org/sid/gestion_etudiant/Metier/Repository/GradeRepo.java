package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GradeRepo extends JpaRepository<Grade, Long> {

    List<Grade> findByArchivedFalse();

    List<Grade> findByArchivedTrue();

    List<Grade> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);

    @Query("SELECT AVG(g.note) FROM Grade g WHERE g.student.id = :studentId AND g.archived = false")
    Double calculateAverageByStudentId(@Param("studentId") Long studentId);


}