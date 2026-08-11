package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Payement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PayementRepo extends JpaRepository<Payement, Long> {

    List<Payement> findByArchivedFalse();

    List<Payement> findByArchivedTrue();

    List<Payement> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);

    @Query("""
            SELECT p
            FROM Payement p
            WHERE p.archived = false
              AND (p.student.keycloakId = :keycloakId OR (:email IS NOT NULL AND LOWER(p.student.email) = LOWER(:email)))
            ORDER BY p.date DESC
            """)
    List<Payement> findMyPayements(@Param("keycloakId") String keycloakId, @Param("email") String email);
}