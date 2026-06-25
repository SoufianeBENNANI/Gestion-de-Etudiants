package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.AttendanceService;
import org.sid.gestion_etudiant.Metier.dto.AttendanceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/Ajouter")
    public AttendanceDTO create(@Valid @RequestBody AttendanceDTO attendanceDTO,
                                @AuthenticationPrincipal Jwt jwt) {

        AttendanceDTO savedAttendance = attendanceService.addAttendance(attendanceDTO);

        String teacherName = getTeacherName(jwt);

        String message = buildAttendanceMessage(
                teacherName,
                "a créé",
                savedAttendance
        );

        sendEvent(EventAction.CREATED, savedAttendance.getId(), message);

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
    public AttendanceDTO getById(@PathVariable Long id) {
        return attendanceService.getAttendanceById(id);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/Modifier/{id}")
    public AttendanceDTO update(@PathVariable Long id,
                                @Valid @RequestBody AttendanceDTO attendanceDTO,
                                @AuthenticationPrincipal Jwt jwt) {

        AttendanceDTO updatedAttendance =
                attendanceService.updateAttendance(id, attendanceDTO);

        String teacherName = getTeacherName(jwt);

        String message = buildAttendanceMessage(
                teacherName,
                "a modifié",
                updatedAttendance
        );

        sendEvent(EventAction.UPDATED, updatedAttendance.getId(), message);

        return updatedAttendance;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteAttendance(@PathVariable Long id,
                                                   @AuthenticationPrincipal Jwt jwt) {

        AttendanceDTO attendance = attendanceService.getAttendanceById(id);

        String responseMessage = attendanceService.deleteAttendance(id);

        String teacherName = getTeacherName(jwt);

        String message = buildAttendanceMessage(
                teacherName,
                "a supprimé",
                attendance
        );

        sendEvent(EventAction.DELETED, id, message);

        return ResponseEntity.ok(responseMessage);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<AttendanceDTO> restoreAttendance(@PathVariable Long id,
                                                           @AuthenticationPrincipal Jwt jwt) {

        AttendanceDTO restoredAttendance =
                attendanceService.restoreAttendance(id);

        String teacherName = getTeacherName(jwt);

        String message = buildAttendanceMessage(
                teacherName,
                "a restauré",
                restoredAttendance
        );

        sendEvent(EventAction.RESTORED, restoredAttendance.getId(), message);

        return ResponseEntity.ok(restoredAttendance);
    }

    private String getTeacherName(Jwt jwt) {

        if (jwt == null) {
            return "teacher inconnu";
        }

        String teacherName = jwt.getClaimAsString("name");

        if (teacherName == null || teacherName.isBlank()) {
            teacherName = jwt.getClaimAsString("preferred_username");
        }

        if (teacherName == null || teacherName.isBlank()) {
            teacherName = jwt.getSubject();
        }

        return teacherName;
    }

    private String buildAttendanceMessage(String teacherName,
                                          String action,
                                          AttendanceDTO attendance) {

        String studentName = buildStudentName(attendance);

        String attendanceDate = attendance.getDate() != null
                ? attendance.getDate().format(DATE_FORMATTER)
                : "date inconnue";

        return "Le teacher " + teacherName + " "
                + action
                + " une attendance concernant le student "
                + studentName
                + ". La date de cette absence est "
                + attendanceDate;
    }

    private String buildStudentName(AttendanceDTO attendance) {

        String prenom = attendance.getStudentPrenom() != null
                ? attendance.getStudentPrenom()
                : "";

        String nom = attendance.getStudentNom() != null
                ? attendance.getStudentNom()
                : "";

        String fullName = (prenom + " " + nom).trim();

        if (!fullName.isBlank()) {
            return fullName;
        }

        return "ID " + attendance.getStudentId();
    }

    private void sendEvent(EventAction action, Long entityId, String message) {

        AppEvent event = new AppEvent();

        event.setEntity(EventEntity.ATTENDANCE);
        event.setAction(action);
        event.setEntityId(entityId);
        event.setMessage(message);

        System.out.println("===== KAFKA ATTENDANCE EVENT =====");
        System.out.println("Entity : " + event.getEntity());
        System.out.println("Action : " + event.getAction());
        System.out.println("ID     : " + event.getEntityId());
        System.out.println("Msg    : " + event.getMessage());
        System.out.println("==================================");

        kafkaProducerService.sendEvent(event);
    }
}