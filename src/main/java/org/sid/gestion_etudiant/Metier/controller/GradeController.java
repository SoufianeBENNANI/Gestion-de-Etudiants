package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/Ajouter")
    public GradeDTO create(@Valid @RequestBody GradeDTO gradeDTO) {
        return gradeService.addGrade(gradeDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/AllGrades")
    public List<GradeDTO> getAll() {
        return gradeService.getAllGrade();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @GetMapping("/Archive")
    public List<GradeDTO> getArchivedGrades() {
        return gradeService.getArchivedGrades();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/Recherche/{id}")
    public GradeDTO getById(@PathVariable Long id) {
        return gradeService.getGradeById(id);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/Modifier/{id}")
    public GradeDTO update(
            @PathVariable Long id,
            @Valid @RequestBody GradeDTO gradeDTO
    ) {
        return gradeService.updateGrade(id, gradeDTO);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteGrade(@PathVariable Long id) {
        String message = gradeService.deleteGrade(id);
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<GradeDTO> restoreGrade(@PathVariable Long id) {
        GradeDTO restoredGrade = gradeService.restoreGrade(id);
        return ResponseEntity.ok(restoredGrade);
    }
}