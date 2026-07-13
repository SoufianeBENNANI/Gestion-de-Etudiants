package org.sid.gestion_etudiant.Security;

import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class JwtUserHelper {

    public String getName(Jwt jwt) {

        if (jwt == null) {
            return "Utilisateur inconnu";
        }

        String name = jwt.getClaimAsString("name");

        if (isEmpty(name)) {
            name = buildFullName(jwt);
        }

        if (isEmpty(name)) {
            name = jwt.getClaimAsString("preferred_username");
        }

        if (isEmpty(name)) {
            name = jwt.getClaimAsString("email");
        }

        if (isEmpty(name)) {
            name = jwt.getSubject();
        }

        return isEmpty(name)
                ? "Utilisateur inconnu"
                : name;
    }

    public String getEmail(Jwt jwt) {

        if (jwt == null) {
            return null;
        }

        String email = jwt.getClaimAsString("email");

        if (isEmpty(email)) {
            String username =
                    jwt.getClaimAsString("preferred_username");

            /*
             * On utilise preferred_username seulement
             * lorsqu'il ressemble à une adresse email.
             */
            if (username != null && username.contains("@")) {
                email = username;
            }
        }

        return isEmpty(email) ? null : email;
    }

    public RecipientRole getRole(Jwt jwt) {

        List<String> roles = getRoles(jwt);

        /*
         * Ordre de priorité si un utilisateur
         * possède plusieurs rôles.
         */
        if (containsRole(roles, "ADMIN")) {
            return RecipientRole.ADMIN;
        }

        if (containsRole(roles, "TEACHER")) {
            return RecipientRole.TEACHER;
        }

        if (containsRole(roles, "MANAGER")) {
            return RecipientRole.MANAGER;
        }

        if (containsRole(roles, "STUDENT")) {
            return RecipientRole.STUDENT;
        }

        return null;
    }

    public List<String> getRoles(Jwt jwt) {

        if (jwt == null) {
            return Collections.emptyList();
        }

        List<String> roles = new ArrayList<>();

        addRealmRoles(jwt, roles);
        addClientRoles(jwt, roles);

        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .toList();
    }

    public boolean hasRole(
            Jwt jwt,
            RecipientRole expectedRole
    ) {
        if (expectedRole == null) {
            return false;
        }

        return getRole(jwt) == expectedRole;
    }

    public boolean hasRole(
            Jwt jwt,
            String expectedRole
    ) {
        if (expectedRole == null || expectedRole.isBlank()) {
            return false;
        }

        return containsRole(
                getRoles(jwt),
                expectedRole
        );
    }

    public String getSubject(Jwt jwt) {

        if (jwt == null) {
            return null;
        }

        return jwt.getSubject();
    }

    public String getPreferredUsername(Jwt jwt) {

        if (jwt == null) {
            return null;
        }

        String username =
                jwt.getClaimAsString("preferred_username");

        return isEmpty(username) ? null : username;
    }

    private void addRealmRoles(
            Jwt jwt,
            List<String> roles
    ) {
        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        if (realmAccess == null) {
            return;
        }

        Object rolesObject =
                realmAccess.get("roles");

        addRolesFromObject(
                rolesObject,
                roles
        );
    }

    private void addClientRoles(
            Jwt jwt,
            List<String> roles
    ) {
        Map<String, Object> resourceAccess =
                jwt.getClaim("resource_access");

        if (resourceAccess == null) {
            return;
        }

        for (Object clientAccessObject :
                resourceAccess.values()) {

            if (!(clientAccessObject
                    instanceof Map<?, ?> clientAccess)) {
                continue;
            }

            Object clientRolesObject =
                    clientAccess.get("roles");

            addRolesFromObject(
                    clientRolesObject,
                    roles
            );
        }
    }

    private void addRolesFromObject(
            Object rolesObject,
            List<String> destination
    ) {
        if (!(rolesObject instanceof List<?> roleList)) {
            return;
        }

        for (Object roleObject : roleList) {
            if (roleObject instanceof String role
                    && !role.isBlank()) {

                destination.add(role);
            }
        }
    }

    private String buildFullName(Jwt jwt) {

        String firstName =
                jwt.getClaimAsString("given_name");

        String lastName =
                jwt.getClaimAsString("family_name");

        String fullName =
                ((firstName != null ? firstName : "")
                        + " "
                        + (lastName != null ? lastName : ""))
                        .trim();

        return fullName.isBlank()
                ? null
                : fullName;
    }

    private boolean containsRole(
            List<String> roles,
            String expectedRole
    ) {
        return roles.stream()
                .anyMatch(role ->
                        role.equalsIgnoreCase(expectedRole)
                                || role.equalsIgnoreCase(
                                "ROLE_" + expectedRole
                        )
                );
    }

    private boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}