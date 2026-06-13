package org.sid.gestion_etudiant.Setting.SettingRepo;

import org.sid.gestion_etudiant.Setting.Entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepo extends JpaRepository<Setting, Long> {

    Optional<Setting> findTopByOrderByIdDesc();
}