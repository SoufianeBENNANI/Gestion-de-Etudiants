package org.sid.gestion_etudiant.IA.Repository;

import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentIAPredictionRepo extends JpaRepository<StudentIAPrediction, Long> {

    List<StudentIAPrediction> findByStudentId(Long studentId);
    Optional<StudentIAPrediction> findTopByStudentIdOrderByDateDesc(Long studentId);
}