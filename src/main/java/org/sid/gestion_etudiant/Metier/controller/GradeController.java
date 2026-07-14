package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.GradeService;
import org.sid.gestion_etudiant.Metier.dto.GradeDTO;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Grade")
@AllArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final KafkaProducerService kafkaProducerService;

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/Ajouter")
    public GradeDTO create(
            @Valid @RequestBody GradeDTO gradeDTO,
            Authentication authentication
    ) {
        GradeDTO savedGrade =
                gradeService.addGrade(gradeDTO);

        String message =
                buildMessage(
                        "La note #" + savedGrade.getId()
                                + " a été ajoutée",
                        authentication
                );

        sendEvent(
                EventAction.CREATED,
                savedGrade.getId(),
                message,
                authentication
        );

        return savedGrade;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/AllGrades")
    public List<GradeDTO> getAll() {
        return gradeService.getAllGrade();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/Archive")
    public List<GradeDTO> getArchivedGrades() {
        return gradeService.getArchivedGrades();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/Recherche/{id}")
    public GradeDTO getById(
            @PathVariable Long id
    ) {
        return gradeService.getGradeById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/Modifier/{id}")
    public GradeDTO update(
            @PathVariable Long id,
            @Valid @RequestBody GradeDTO gradeDTO,
            Authentication authentication
    ) {
        GradeDTO updatedGrade =
                gradeService.updateGrade(
                        id,
                        gradeDTO
                );

        String message =
                buildMessage(
                        "La note #" + id
                                + " a été modifiée",
                        authentication
                );

        sendEvent(
                EventAction.UPDATED,
                id,
                message,
                authentication
        );

        return updatedGrade;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteGrade(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String serviceMessage =
                gradeService.deleteGrade(id);

        String notificationMessage =
                buildMessage(
                        "La note #" + id
                                + " a été supprimée",
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

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<GradeDTO> restoreGrade(
            @PathVariable Long id,
            Authentication authentication
    ) {
        GradeDTO restoredGrade =
                gradeService.restoreGrade(id);

        String message =
                buildMessage(
                        "La note #" + id
                                + " a été restaurée",
                        authentication
                );

        sendEvent(
                EventAction.RESTORED,
                id,
                message,
                authentication
        );

        return ResponseEntity.ok(
                restoredGrade
        );
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
                + senderEmail
                + ".";
    }

    private void sendEvent(
            EventAction action,
            Long entityId,
            String message,
            Authentication authentication
    ) {
        AppEvent event = new AppEvent();

        event.setEntity(
                EventEntity.GRADE
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

        event.setRecipientRole(
                RecipientRole.ADMIN
        );

        event.setRecipientEmail(null);

        kafkaProducerService.sendEvent(event);
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