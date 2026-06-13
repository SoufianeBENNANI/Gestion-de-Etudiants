package org.sid.gestion_etudiant.Setting.SettingServiceImpl;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Setting.Entity.Language;
import org.sid.gestion_etudiant.Setting.Entity.Setting;
import org.sid.gestion_etudiant.Setting.Entity.ThemeMode;
import org.sid.gestion_etudiant.Setting.SettingRepo.SettingRepo;
import org.sid.gestion_etudiant.Setting.SettingService.SettingService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingRepo settingRepo;

    @Override
    public Setting getSettings() {
        return settingRepo.findTopByOrderByIdDesc()
                .orElseGet(this::createDefaultSettings);
    }

    @Override
    public Setting createDefaultSettings() {
        Setting existingSetting = settingRepo.findTopByOrderByIdDesc()
                .orElse(null);

        if (existingSetting != null) {
            return existingSetting;
        }

        Setting setting = new Setting();

        setting.setSchoolName("School");
        setting.setSchoolEmail("contact@school.com");
        setting.setSchoolPhone("");
        setting.setSchoolAddress("");
        setting.setLogoUrl("");
        setting.setPrimaryColor("#2563eb");
        setting.setSecondaryColor("#0f172a");
        setting.setThemeMode(ThemeMode.LIGHT);
        setting.setLanguage(Language.EN);
        setting.setNotificationsEnabled(true);
        setting.setMaintenanceMode(false);

        return settingRepo.save(setting);
    }

    @Override
    public Setting updateSettings(Long id, Setting settingData) {
        Setting setting = settingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Settings not found with id: " + id));

        setting.setSchoolName(settingData.getSchoolName());
        setting.setSchoolEmail(settingData.getSchoolEmail());
        setting.setSchoolPhone(settingData.getSchoolPhone());
        setting.setSchoolAddress(settingData.getSchoolAddress());
        setting.setLogoUrl(settingData.getLogoUrl());
        setting.setPrimaryColor(settingData.getPrimaryColor());
        setting.setSecondaryColor(settingData.getSecondaryColor());
        setting.setThemeMode(settingData.getThemeMode());
        setting.setLanguage(settingData.getLanguage());
        setting.setNotificationsEnabled(settingData.getNotificationsEnabled());
        setting.setMaintenanceMode(settingData.getMaintenanceMode());

        return settingRepo.save(setting);
    }
}