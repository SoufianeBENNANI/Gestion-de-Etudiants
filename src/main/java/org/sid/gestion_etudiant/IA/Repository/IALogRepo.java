package org.sid.gestion_etudiant.IA.Repository;

import org.sid.gestion_etudiant.IA.Entity.IALog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IALogRepo extends JpaRepository<IALog, Long> {
}
