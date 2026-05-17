package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Attendance;
import org.sid.gestion_etudiant.Metier.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepo extends JpaRepository<Attendance, Long> {

    List<Attendance> findByArchivedFalse();

    List<Attendance> findByArchivedTrue();

    List<Attendance> findByArchivedTrueAndArchivedAtBefore(LocalDateTime limitDate);

    boolean existsByStudentIdAndDateAndArchivedFalse(Long studentId, LocalDate date);

    boolean existsByStudentIdAndDateAndArchivedTrue(Long studentId, LocalDate date);

    List<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);
}