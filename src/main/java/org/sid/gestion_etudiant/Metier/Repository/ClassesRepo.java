package org.sid.gestion_etudiant.Metier.Repository;

import org.sid.gestion_etudiant.Metier.Entity.ClassesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClassesRepo extends JpaRepository<ClassesEntity, Long> {

    List<ClassesEntity> findByArchivedFalse();

    List<ClassesEntity> findByArchivedTrue();

    List<ClassesEntity> findByArchivedTrueAndArchivedAtBefore(LocalDateTime date);
}