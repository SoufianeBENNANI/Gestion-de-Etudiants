package org.sid.gestion_etudiant.Metier.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatedAccountResponse {

    private Long id;

    private String keycloakId;

    private String email;

    private String temporaryPassword;
}