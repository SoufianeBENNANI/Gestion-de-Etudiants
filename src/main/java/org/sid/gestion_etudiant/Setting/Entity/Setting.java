package org.sid.gestion_etudiant.Setting.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String schoolName;

    private String schoolEmail;

    private String schoolPhone;

    private String schoolAddress;

    private String logoUrl;

    private String primaryColor;

    private String secondaryColor;

    @Enumerated(EnumType.STRING)
    private ThemeMode themeMode;

    @Enumerated(EnumType.STRING)
    private Language language;

    private Boolean notificationsEnabled;

    private Boolean maintenanceMode;
}