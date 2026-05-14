package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.Payement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PayementRepo extends JpaRepository<Payement, Long> {

    List<Payement> findByArchivedFalse();

    List<Payement> findByArchivedTrue();

    List<Payement> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);
}