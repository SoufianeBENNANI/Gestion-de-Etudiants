package org.sid.gestion_etudiant.Security;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public String createUser(
            String firstname,
            String lastname,
            String email,
            String temporaryPassword,
            String groupName
    ) {

        RealmResource realmResource = keycloak.realm(realm);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstname);
        user.setLastName(lastname);
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential =
                new CredentialRepresentation();

        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(temporaryPassword);
        credential.setTemporary(true);

        user.setCredentials(List.of(credential));

        try (Response response =
                     realmResource.users().create(user)) {

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new IllegalStateException(
                        "Erreur de création Keycloak. Code HTTP : "
                                + response.getStatus()
                );
            }

            URI location = response.getLocation();

            if (location == null) {
                throw new IllegalStateException(
                        "Keycloak n'a pas retourné l'identifiant utilisateur"
                );
            }

            String path = location.getPath();

            String keycloakId =
                    path.substring(path.lastIndexOf('/') + 1);

            addUserToGroup(
                    realmResource,
                    keycloakId,
                    groupName
            );

            return keycloakId;
        }
    }

    public void deleteUser(String keycloakId) {

        if (keycloakId == null || keycloakId.isBlank()) {
            return;
        }

        keycloak.realm(realm)
                .users()
                .get(keycloakId)
                .remove();
    }

    private void addUserToGroup(
            RealmResource realmResource,
            String keycloakId,
            String groupName
    ) {

        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException(
                    "Le groupe Keycloak est obligatoire"
            );
        }

        GroupRepresentation group =
                realmResource.groups()
                        .groups()
                        .stream()
                        .filter(item ->
                                groupName.equalsIgnoreCase(
                                        item.getName()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Groupe Keycloak introuvable : "
                                                + groupName
                                )
                        );

        realmResource.users()
                .get(keycloakId)
                .joinGroup(group.getId());
    }
}