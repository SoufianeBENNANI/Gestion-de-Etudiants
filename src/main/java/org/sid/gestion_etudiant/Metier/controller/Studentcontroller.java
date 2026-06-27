package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.StudentService;
import org.sid.gestion_etudiant.Metier.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/Students")
public class Studentcontroller {

    @Autowired
    private StudentService studentService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/Ajouter")
    public StudentDTO create(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO savedStudent = studentService.addStudent(studentDTO);

        sendEvent(EventAction.CREATED, savedStudent.getId(), "Student created successfully");

        return savedStudent;
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/AllStudents")
    public List<StudentDTO> getAll() {
        return studentService.getAllStudents();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/Recherche")
    public List<StudentDTO> getByNom(@RequestParam String nom) {

        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le champ 'nom' est obligatoire");
        }

        return studentService.getStudentsByNom(nom);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Modifier/{id}")
    public StudentDTO update(@PathVariable Long id, @Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);

        sendEvent(EventAction.UPDATED, id, "Student updated successfully");

        return updatedStudent;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        String message = studentService.deleteStudent(id);

        sendEvent(EventAction.DELETED, id, "Student deleted successfully");

        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Archive")
    public List<StudentDTO> getArchivedStudents() {
        return studentService.getArchivedStudents();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<StudentDTO> restoreStudent(@PathVariable Long id) {
        StudentDTO restoredStudent = studentService.restoreStudent(id);

        sendEvent(EventAction.RESTORED, id, "Student restored successfully");

        return ResponseEntity.ok(restoredStudent);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/DownloadPdf")
    public ResponseEntity<byte[]> downloadStudentsPdf() {
        byte[] pdf = studentService.generateStudentsPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=liste_etudiants.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void sendEvent(EventAction action, Long entityId, String message) {
        AppEvent event = new AppEvent();
        event.setEntity(EventEntity.STUDENT);
        event.setAction(action);
        event.setEntityId(entityId);
        event.setMessage(message);

        kafkaProducerService.sendEvent(event);
    }
}