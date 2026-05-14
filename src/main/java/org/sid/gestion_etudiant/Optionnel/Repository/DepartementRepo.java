package org.sid.gestion_etudiant.Optionnel.Repository;

import org.sid.gestion_etudiant.Optionnel.Entity.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepo extends JpaRepository<Departement, Long> {
}
