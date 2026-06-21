package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.GradeService;
import org.sid.gestion_etudiant.Metier.dto.GradeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public GradeDTO create(@Valid @RequestBody GradeDTO gradeDTO) {
        GradeDTO savedGrade = gradeService.addGrade(gradeDTO);

        sendEvent(EventAction.CREATED, savedGrade.getId(), "Grade created successfully");

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
    public GradeDTO getById(@PathVariable Long id) {
        return gradeService.getGradeById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/Modifier/{id}")
    public GradeDTO update(
            @PathVariable Long id,
            @Valid @RequestBody GradeDTO gradeDTO
    ) {
        GradeDTO updatedGrade = gradeService.updateGrade(id, gradeDTO);

        sendEvent(EventAction.UPDATED, id, "Grade updated successfully");

        return updatedGrade;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteGrade(@PathVariable Long id) {
        String message = gradeService.deleteGrade(id);

        sendEvent(EventAction.DELETED, id, "Grade deleted successfully");

        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<GradeDTO> restoreGrade(@PathVariable Long id) {
        GradeDTO restoredGrade = gradeService.restoreGrade(id);

        sendEvent(EventAction.RESTORED, id, "Grade restored successfully");

        return ResponseEntity.ok(restoredGrade);
    }

    private void sendEvent(EventAction action, Long entityId, String message) {
        AppEvent event = new AppEvent();
        event.setEntity(EventEntity.GRADE);
        event.setAction(action);
        event.setEntityId(entityId);
        event.setMessage(message);

        kafkaProducerService.sendEvent(event);
    }
}