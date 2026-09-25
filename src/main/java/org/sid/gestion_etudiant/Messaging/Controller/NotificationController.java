package org.sid.gestion_etudiant.Messaging.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Notification.Dto.NotificationRequest;
import org.sid.gestion_etudiant.Notification.Entity.Notification;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.sid.gestion_etudiant.Notification.Service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public String sendNotification(
            @Valid @RequestBody NotificationRequest request,
            Authentication authentication
    ) {

        RecipientRole senderRole =
                extractSenderRole(authentication);

        String senderEmail =
                extractSenderEmail(authentication);

        notificationService.sendNotification(
                senderEmail,
                senderRole,
                request.getRecipientEmail(),
                request.getRecipientRole(),
                request.getSubject(),
                request.getMessage(),
                request.getEntity(),
                request.getAction(),
                request.getEntityId()
        );

        return "Notification envoyée de "
                + senderRole
                + " vers "
                + request.getRecipientRole();
    }

    @GetMapping("/me")
    public List<Notification> myNotifications(
            Authentication authentication
    ) {

        requireAdmin(authentication);

        String email =
                extractSenderEmail(
                        authentication
                );

        return notificationService
                .getMyNotifications(
                        email
                );
    }

    @GetMapping("/me/unread")
    public List<Notification> unread(
            Authentication authentication
    ) {

        requireAdmin(authentication);

        String email =
                extractSenderEmail(
                        authentication
                );

        return notificationService
                .getUnreadNotifications(
                        email
                );
    }

    @GetMapping("/me/unread/count")
    public long countUnread(
            Authentication authentication
    ) {

        requireAdmin(authentication);

        String email =
                extractSenderEmail(
                        authentication
                );

        return notificationService
                .countUnread(
                        email
                );
    }

    @PutMapping("/{id}/read")
    public Notification markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {

        requireAdmin(authentication);

        String email =
                extractSenderEmail(
                        authentication
                );

        return notificationService
                .markAsRead(
                        id,
                        email
                );
    }

    private RecipientRole extractSenderRole(
            Authentication authentication
    ) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Utilisateur non authentifié."
            );
        }

        for (
                GrantedAuthority grantedAuthority :
                authentication.getAuthorities()
        ) {

            String authority =
                    grantedAuthority.getAuthority();

            if (!authority.startsWith("ROLE_")) {
                continue;
            }

            String roleName =
                    authority.substring(
                            "ROLE_".length()
                    );

            try {

                return RecipientRole.valueOf(
                        roleName.toUpperCase()
                );

            } catch (
                    IllegalArgumentException ignored
            ) {
                // Continuer
            }
        }

        throw new IllegalStateException(
                "Aucun rôle valide trouvé dans le token Keycloak."
        );
    }

    private String extractSenderEmail(
            Authentication authentication
    ) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Utilisateur non authentifié."
            );
        }

        if (
                authentication.getPrincipal()
                        instanceof Jwt jwt
        ) {

            String email =
                    jwt.getClaimAsString(
                            "email"
                    );

            if (
                    email != null &&
                            !email.isBlank()
            ) {

                return email;
            }


            String preferredUsername =
                    jwt.getClaimAsString(
                            "preferred_username"
                    );

            if (
                    preferredUsername != null &&
                            !preferredUsername.isBlank()
            ) {

                return preferredUsername;
            }
        }

        return authentication.getName();
    }

    private void requireAdmin(
            Authentication authentication
    ) {

        if (authentication == null) {
            throw new AccessDeniedException(
                    "Utilisateur non authentifié."
            );
        }

        boolean admin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equalsIgnoreCase(
                                                        "ROLE_ADMIN"
                                                )
                        );

        if (!admin) {

            throw new AccessDeniedException(
                    "Accès réservé à ADMIN."
            );
        }
    }
}