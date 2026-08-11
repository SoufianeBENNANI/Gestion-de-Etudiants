package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.PayementService;
import org.sid.gestion_etudiant.Metier.dto.PayementDTO;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Payement")
@AllArgsConstructor
public class PayementController {

    private final PayementService payementService;
    private final KafkaProducerService kafkaProducerService;

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public List<PayementDTO> getMyPayements(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return payementService.getMyPayements(
                jwt.getSubject(),
                jwt.getClaimAsString("email")
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/Ajouter")
    public PayementDTO create(
            @Valid @RequestBody PayementDTO payementDTO,
            Authentication authentication
    ) {
        PayementDTO savedPayement =
                payementService.addPayement(payementDTO);

        String message =
                buildMessage(
                        "Le paiement #" + savedPayement.getId()
                                + " a été créé",
                        authentication
                );

        sendEvent(
                EventAction.CREATED,
                savedPayement.getId(),
                message,
                authentication
        );

        return savedPayement;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/AllPayement")
    public List<PayementDTO> getAll() {
        return payementService.getAllPayements();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/Archive")
    public List<PayementDTO> getArchivedPayements() {
        return payementService.getArchivedPayements();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/Recherche/{id}")
    public PayementDTO getById(
            @PathVariable Long id
    ) {
        return payementService.getPayementById(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/Modifier/{id}")
    public PayementDTO update(
            @PathVariable Long id,
            @Valid @RequestBody PayementDTO payementDTO,
            Authentication authentication
    ) {
        PayementDTO updatedPayement =
                payementService.updatePayement(
                        id,
                        payementDTO
                );

        String message =
                buildMessage(
                        "Le paiement #" + id
                                + " a été modifié",
                        authentication
                );

        sendEvent(
                EventAction.UPDATED,
                id,
                message,
                authentication
        );

        return updatedPayement;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deletePayement(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String serviceMessage =
                payementService.deletePayement(id);

        String notificationMessage =
                buildMessage(
                        "Le paiement #" + id
                                + " a été supprimé",
                        authentication
                );

        sendEvent(
                EventAction.DELETED,
                id,
                notificationMessage,
                authentication
        );

        return ResponseEntity.ok(
                serviceMessage
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<PayementDTO> restorePayement(
            @PathVariable Long id,
            Authentication authentication
    ) {
        PayementDTO restoredPayement =
                payementService.restorePayement(id);

        String message =
                buildMessage(
                        "Le paiement #" + id
                                + " a été restauré",
                        authentication
                );

        sendEvent(
                EventAction.RESTORED,
                id,
                message,
                authentication
        );

        return ResponseEntity.ok(
                restoredPayement
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/DownloadPDF")
    public ResponseEntity<byte[]> downloadPayementsPdf(
            Authentication authentication
    ) {
        byte[] pdf =
                payementService.generatePayementsPdf();

        String message =
                buildMessage(
                        "Le PDF des paiements a été généré",
                        authentication
                );

        sendEvent(
                EventAction.GENERATED,
                null,
                message,
                authentication
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=payements-list.pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }

    private void sendEvent(
            EventAction action,
            Long entityId,
            String message,
            Authentication authentication
    ) {
        AppEvent event = new AppEvent();

        event.setEntity(
                EventEntity.PAYEMENT
        );

        event.setAction(action);
        event.setEntityId(entityId);
        event.setMessage(message);

        event.setSenderEmail(
                extractSenderEmail(authentication)
        );

        event.setSenderRole(
                extractSenderRole(authentication)
        );

        /*
         * Toutes les activités de paiement
         * sont affichées dans la cloche ADMIN.
         */
        event.setRecipientRole(
                RecipientRole.ADMIN
        );

        event.setRecipientEmail(null);

        kafkaProducerService.sendEvent(event);
    }

    private String buildMessage(
            String actionMessage,
            Authentication authentication
    ) {
        RecipientRole senderRole =
                extractSenderRole(authentication);

        String senderEmail =
                extractSenderEmail(authentication);

        return actionMessage
                + " par le "
                + senderRole
                + " : "
                + senderEmail;
    }

    private RecipientRole extractSenderRole(
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new IllegalStateException(
                    "Authentification introuvable."
            );
        }

        return authentication
                .getAuthorities()
                .stream()
                .map(authority ->
                        authority.getAuthority()
                )
                .filter(authority ->
                        authority.startsWith("ROLE_")
                )
                .map(authority ->
                        authority.substring(
                                "ROLE_".length()
                        )
                )
                .map(String::toUpperCase)
                .map(roleName -> {
                    try {
                        return RecipientRole.valueOf(
                                roleName
                        );
                    } catch (
                            IllegalArgumentException exception
                    ) {
                        return null;
                    }
                })
                .filter(role -> role != null)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Aucun rôle valide trouvé dans le token."
                        )
                );
    }

    private String extractSenderEmail(
            Authentication authentication
    ) {
        if (
                authentication != null &&
                        authentication.getPrincipal()
                                instanceof Jwt jwt
        ) {
            String email =
                    jwt.getClaimAsString("email");

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

        return authentication != null
                ? authentication.getName()
                : "system";
    }
}