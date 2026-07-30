package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.StudentService;
import org.sid.gestion_etudiant.Metier.dto.CreateStudentRequest;
import org.sid.gestion_etudiant.Metier.dto.CreatedAccountResponse;
import org.sid.gestion_etudiant.Metier.dto.StudentDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/Students")
@RequiredArgsConstructor
public class Studentcontroller {

    private final StudentService studentService;
    private final KafkaProducerService kafkaProducerService;
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/Ajouter")
    public ResponseEntity<CreatedAccountResponse> createStudent(
            @Valid @RequestBody CreateStudentRequest request
    ) {
        CreatedAccountResponse response =
                studentService.createStudentAccount(request);

        sendEvent(
                EventAction.CREATED,
                response.getId(),
                "Student account created successfully"
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Endpoint inchangé : GET /api/Students/AllStudents
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/AllStudents")
    public ResponseEntity<List<StudentDTO>> getAll() {
        return ResponseEntity.ok(
                studentService.getAllStudents()
        );
    }

    // Endpoint inchangé : GET /api/Students/Recherche?nom=...
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/Recherche")
    public ResponseEntity<List<StudentDTO>> getByNom(
            @RequestParam String nom
    ) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException(
                    "Le champ 'nom' est obligatoire"
            );
        }

        return ResponseEntity.ok(
                studentService.getStudentsByNom(nom.trim())
        );
    }

    // Endpoint inchangé : PUT /api/Students/Modifier/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Modifier/{id}")
    public ResponseEntity<StudentDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO studentDTO
    ) {
        StudentDTO updatedStudent =
                studentService.updateStudent(id, studentDTO);

        sendEvent(
                EventAction.UPDATED,
                id,
                "Student updated successfully"
        );

        return ResponseEntity.ok(updatedStudent);
    }

    // Endpoint inchangé : DELETE /api/Students/Supprimer/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteStudent(
            @PathVariable Long id
    ) {
        String message = studentService.deleteStudent(id);

        sendEvent(
                EventAction.DELETED,
                id,
                "Student deleted successfully"
        );

        return ResponseEntity.ok(message);
    }

    // Endpoint inchangé : GET /api/Students/Archive
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Archive")
    public ResponseEntity<List<StudentDTO>> getArchivedStudents() {
        return ResponseEntity.ok(
                studentService.getArchivedStudents()
        );
    }

    // Endpoint inchangé : PUT /api/Students/Restaurer/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<StudentDTO> restoreStudent(
            @PathVariable Long id
    ) {
        StudentDTO restoredStudent =
                studentService.restoreStudent(id);

        sendEvent(
                EventAction.RESTORED,
                id,
                "Student restored successfully"
        );

        return ResponseEntity.ok(restoredStudent);
    }

    // Endpoint inchangé : GET /api/Students/DownloadPdf
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/DownloadPdf")
    public ResponseEntity<byte[]> downloadStudentsPdf() {
        byte[] pdf = studentService.generateStudentsPdf();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=liste_etudiants.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void sendEvent(
            EventAction action,
            Long entityId,
            String message
    ) {
        AppEvent event = new AppEvent();

        event.setEntity(EventEntity.STUDENT);
        event.setAction(action);
        event.setEntityId(entityId);
        event.setMessage(message);

        kafkaProducerService.sendEvent(event);
    }
}