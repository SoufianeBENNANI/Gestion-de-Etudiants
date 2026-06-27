package org.sid.gestion_etudiant.Setting.SettingController;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Setting.Entity.Setting;
import org.sid.gestion_etudiant.Setting.SettingService.SettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@AllArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    public Setting getSettings() {
        return settingService.getSettings();
    }

    @PostMapping("/default")
    @PreAuthorize("hasRole('ADMIN')")
    public Setting createDefaultSettings() {
        return settingService.createDefaultSettings();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Setting updateSettings(
            @PathVariable Long id,
            @RequestBody Setting setting
    ) {
        return settingService.updateSettings(id, setting);
    }

    @PutMapping("/appearance/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','MANAGER')")
    public Setting updateAppearanceSettings(
            @PathVariable Long id,
            @RequestBody Setting setting
    ) {
        return settingService.updateAppearanceSettings(id, setting);
    }
}