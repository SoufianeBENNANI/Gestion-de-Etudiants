package org.sid.gestion_etudiant.Optionnel.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Metier.dto.CreateTeacherRequest;
import org.sid.gestion_etudiant.Metier.dto.CreatedAccountResponse;
import org.sid.gestion_etudiant.Optionnel.DTO.TeacherDTO;
import org.sid.gestion_etudiant.Optionnel.Service.TeacherService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/Add")
    public ResponseEntity<CreatedAccountResponse> addTeacher(
            @Valid @RequestBody CreateTeacherRequest request
    ) {
        CreatedAccountResponse response =
                teacherService.createTeacherAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/AllTeachers")
    public List<TeacherDTO> getAllTeachers() {

        return teacherService.getAllTeachers();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/Search/{id}")
    public TeacherDTO getTeacherById(
            @PathVariable Long id
    ) {

        return teacherService.getTeacherById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/Search")
    public List<TeacherDTO> getTeachersByNom(
            @RequestParam String nom
    ) {

        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Le champ 'nom' est obligatoire"
            );
        }

        return teacherService.getTeachersByNom(nom);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/keycloak/{keycloakId}")
    public TeacherDTO getTeacherByKeycloakId(
            @PathVariable String keycloakId
    ) {

        return teacherService.getTeacherByKeycloakId(keycloakId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Update/{id}")
    public TeacherDTO updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherDTO dto
    ) {

        return teacherService.updateTeacher(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/Delete/{id}")
    public ResponseEntity<String> deleteTeacher(
            @PathVariable Long id
    ) {

        String message =
                teacherService.deleteTeacher(id);

        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Archive")
    public List<TeacherDTO> getArchivedTeachers() {

        return teacherService.getArchivedTeachers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<TeacherDTO> restoreTeacher(
            @PathVariable Long id
    ) {

        TeacherDTO restoredTeacher =
                teacherService.restoreTeacher(id);

        return ResponseEntity.ok(restoredTeacher);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generateTeachersPdf() {

        byte[] pdf =
                teacherService.generateTeachersPdf();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=teachers.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}