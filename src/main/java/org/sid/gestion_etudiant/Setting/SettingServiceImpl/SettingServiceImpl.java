package org.sid.gestion_etudiant.Setting.SettingServiceImpl;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Setting.Entity.Language;
import org.sid.gestion_etudiant.Setting.Entity.Setting;
import org.sid.gestion_etudiant.Setting.Entity.ThemeMode;
import org.sid.gestion_etudiant.Setting.SettingRepo.SettingRepo;
import org.sid.gestion_etudiant.Setting.SettingService.SettingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingRepo settingRepo;

    @Override
    public Setting getSettings() {
        List<Setting> settings = settingRepo.findAll();

        if (settings.isEmpty()) {
            return createDefaultSettings();
        }

        return settings.get(0);
    }

    @Override
    public Setting createDefaultSettings() {
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
                .orElseThrow(() -> new RuntimeException("Settings not found"));

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