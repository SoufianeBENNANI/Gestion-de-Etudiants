package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.AttendanceService;
import org.sid.gestion_etudiant.Metier.dto.AttendanceDTO;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/Attendance")
@AllArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final KafkaProducerService kafkaProducerService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/Ajouter")
    public AttendanceDTO create(
            @Valid @RequestBody AttendanceDTO attendanceDTO,
            Authentication authentication
    ) {
        AttendanceDTO savedAttendance =
                attendanceService.addAttendance(attendanceDTO);

        String message =
                buildAttendanceMessage(
                        "a enregistré",
                        savedAttendance,
                        authentication
                );

        sendEvent(
                EventAction.CREATED,
                savedAttendance.getId(),
                message,
                authentication
        );

        return savedAttendance;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/AllAttendance")
    public List<AttendanceDTO> getAll() {
        return attendanceService.getAllAttendances();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/Archive")
    public List<AttendanceDTO> getArchivedAttendances() {
        return attendanceService.getArchivedAttendances();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/Recherche/{id}")
    public AttendanceDTO getById(
            @PathVariable Long id
    ) {
        return attendanceService.getAttendanceById(id);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/Modifier/{id}")
    public AttendanceDTO update(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceDTO attendanceDTO,
            Authentication authentication
    ) {
        AttendanceDTO updatedAttendance =
                attendanceService.updateAttendance(
                        id,
                        attendanceDTO
                );

        String message =
                buildAttendanceMessage(
                        "a modifié",
                        updatedAttendance,
                        authentication
                );

        sendEvent(
                EventAction.UPDATED,
                updatedAttendance.getId(),
                message,
                authentication
        );

        return updatedAttendance;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteAttendance(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AttendanceDTO attendance =
                attendanceService.getAttendanceById(id);

        String serviceMessage =
                attendanceService.deleteAttendance(id);

        String notificationMessage =
                buildAttendanceMessage(
                        "a supprimé",
                        attendance,
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
    public ResponseEntity<AttendanceDTO> restoreAttendance(
            @PathVariable Long id,
            Authentication authentication
    ) {
        AttendanceDTO restoredAttendance =
                attendanceService.restoreAttendance(id);

        String message =
                buildAttendanceMessage(
                        "a restauré",
                        restoredAttendance,
                        authentication
                );

        sendEvent(
                EventAction.RESTORED,
                restoredAttendance.getId(),
                message,
                authentication
        );

        return ResponseEntity.ok(
                restoredAttendance
        );
    }

    private String buildAttendanceMessage(
            String action,
            AttendanceDTO attendance,
            Authentication authentication
    ) {
        String studentName =
                buildStudentName(attendance);

        String attendanceDate =
                attendance.getDate() != null
                        ? attendance.getDate().format(
                        DATE_FORMATTER
                )
                        : "date non renseignée";

        RecipientRole senderRole =
                extractSenderRole(authentication);

        String senderEmail =
                extractSenderEmail(authentication);

        return "Le "
                + senderRole
                + " "
                + senderEmail
                + " "
                + action
                + " la présence de l'étudiant "
                + studentName
                + " pour le "
                + attendanceDate
                + ".";
    }

    private String buildStudentName(
            AttendanceDTO attendance
    ) {
        String prenom =
                attendance.getStudentPrenom() != null
                        ? attendance.getStudentPrenom()
                        : "";

        String nom =
                attendance.getStudentNom() != null
                        ? attendance.getStudentNom()
                        : "";

        String fullName =
                (prenom + " " + nom).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        return attendance.getStudentId() != null
                ? "étudiant #" + attendance.getStudentId()
                : "étudiant non identifié";
    }

    private void sendEvent(
            EventAction action,
            Long entityId,
            String message,
            Authentication authentication
    ) {
        AppEvent event = new AppEvent();

        event.setEntity(
                EventEntity.ATTENDANCE
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