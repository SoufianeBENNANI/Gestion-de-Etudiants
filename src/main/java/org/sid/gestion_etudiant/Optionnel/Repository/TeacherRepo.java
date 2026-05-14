package org.sid.gestion_etudiant.Optionnel.Repository;

import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepo extends JpaRepository<Teacher, Long> {
}
