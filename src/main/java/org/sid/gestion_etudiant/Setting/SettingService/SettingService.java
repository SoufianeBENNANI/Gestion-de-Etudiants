package org.sid.gestion_etudiant.Setting.SettingService;

import org.sid.gestion_etudiant.Setting.Entity.Setting;

public interface SettingService {

    Setting getSettings();

    Setting updateSettings(Long id, Setting setting);

    Setting createDefaultSettings();

    Setting updateAppearanceSettings(Long id, Setting setting);
}