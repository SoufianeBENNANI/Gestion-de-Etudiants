package org.sid.gestion_etudiant.Optionnel.Repository;

import org.sid.gestion_etudiant.Optionnel.Entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DepartementRepo extends JpaRepository<Departement, Long> {
    List<Departement> findByArchivedFalse();

    List<Departement> findByArchivedTrue();

    List<Departement> findByNomContainingIgnoreCaseAndArchivedFalse(String nom);

    List<Departement> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);

    Optional<Departement> findByNomIgnoreCaseAndArchivedFalse(String nom);
}
