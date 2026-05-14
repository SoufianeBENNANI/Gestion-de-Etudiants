package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import org.sid.gestion_etudiant.Metier.Service.CoursesService;
import org.sid.gestion_etudiant.Metier.dto.CoursesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Courses")
public class CoursesController {

    @Autowired
    private CoursesService coursesService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/Ajouter")
    public CoursesDTO create(@Valid @RequestBody CoursesDTO coursesDTO) {
        return coursesService.addCourses(coursesDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/AllCourses")
    public List<CoursesDTO> getAll() {
        return coursesService.getAllCourses();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Archive")
    public List<CoursesDTO> getArchivedCourses() {
        return coursesService.getArchivedCourses();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/Recherche/{id}")
    public CoursesDTO getById(@PathVariable Long id) {
        return coursesService.getCoursesById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Modifier/{id}")
    public CoursesDTO update(
            @PathVariable Long id,
            @Valid @RequestBody CoursesDTO coursesDTO
    ) {
        return coursesService.updateCourses(id, coursesDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteCourses(@PathVariable Long id) {
        String message = coursesService.deleteCourses(id);
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<CoursesDTO> restoreCourses(@PathVariable Long id) {
        CoursesDTO restoredCourse = coursesService.restoreCourses(id);
        return ResponseEntity.ok(restoredCourse);
    }
}