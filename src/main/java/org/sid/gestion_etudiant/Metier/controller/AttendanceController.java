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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Attendance")
@AllArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final KafkaProducerService kafkaProducerService;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/Ajouter")
    public AttendanceDTO create(@Valid @RequestBody AttendanceDTO attendanceDTO) {
        AttendanceDTO savedAttendance = attendanceService.addAttendance(attendanceDTO);
        sendEvent(EventAction.CREATED, savedAttendance.getId(), "Attendance created successfully");
        return savedAttendance;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/AllAttendance")
    public List<AttendanceDTO> getAll() {
        return attendanceService.getAllAttendances();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
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
    public AttendanceDTO update(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceDTO attendanceDTO
    ) {
        AttendanceDTO updatedAttendance = attendanceService.updateAttendance(id, attendanceDTO);
        sendEvent(EventAction.UPDATED, id, "Attendance updated successfully");
        return updatedAttendance;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteAttendance(@PathVariable Long id) {
        String message = attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<AttendanceDTO> restoreAttendance(@PathVariable Long id) {
        AttendanceDTO restoredAttendance = attendanceService.restoreAttendance(id);
        return ResponseEntity.ok(restoredAttendance);
    }

    private void sendEvent(EventAction action, Long entityId, String message) {
        AppEvent event = new AppEvent();
        event.setEntity(EventEntity.ATTENDANCE);
        event.setAction(action);
        event.setEntityId(entityId);
        event.setMessage(message);
        kafkaProducerService.sendEvent(event);
    }
}