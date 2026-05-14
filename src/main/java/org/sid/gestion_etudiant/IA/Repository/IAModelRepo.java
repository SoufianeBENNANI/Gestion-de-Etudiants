package org.sid.gestion_etudiant.IA.Repository;

import org.sid.gestion_etudiant.IA.Entity.IAModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IAModelRepo extends JpaRepository<IAModel, Long> {

    Optional<IAModel> findTopByArchivedFalseOrderByCreatedAtDesc();

    List<IAModel> findByArchivedFalse();

    List<IAModel> findByArchivedTrue();

    List<IAModel> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);
}