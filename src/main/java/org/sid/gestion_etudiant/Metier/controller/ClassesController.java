package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import org.sid.gestion_etudiant.Metier.Service.ClassesService;
import org.sid.gestion_etudiant.Metier.dto.ClassesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Classes")
public class ClassesController {

    @Autowired
    private ClassesService classesService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/Ajouter")
    public ClassesDTO create(@Valid @RequestBody ClassesDTO classesDTO) {
        return classesService.addClasses(classesDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/AllClasses")
    public List<ClassesDTO> getAll() {
        return classesService.getAllClasses();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Archive")
    public List<ClassesDTO> getArchivedClasses() {
        return classesService.getArchivedClasses();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/Recherche/{id}")
    public ClassesDTO getById(@PathVariable Long id) {
        return classesService.getClasseById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Modifier/{id}")
    public ClassesDTO update(
            @PathVariable Long id,
            @Valid @RequestBody ClassesDTO classesDTO
    ) {
        return classesService.updateClasses(id, classesDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteClasses(@PathVariable Long id) {
        String message = classesService.deleteClasses(id);
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<ClassesDTO> restoreClasses(@PathVariable Long id) {
        ClassesDTO restoredClass = classesService.restoreClasses(id);
        return ResponseEntity.ok(restoredClass);
    }
}