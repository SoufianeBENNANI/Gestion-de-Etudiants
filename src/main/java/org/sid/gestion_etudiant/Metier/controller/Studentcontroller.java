package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import org.sid.gestion_etudiant.Metier.Service.StudentService;
import org.sid.gestion_etudiant.Metier.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/Students")
public class Studentcontroller {

    @Autowired
    private StudentService studentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/Ajouter")
    public StudentDTO create(@Valid @RequestBody StudentDTO studentDTO){
        return studentService.addStudent(studentDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','MANAGER')")
    @GetMapping("/AllStudents")
    public List<StudentDTO> getAll(){
        return studentService.getAllStudents();
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @GetMapping("/Recherche")
    public List<StudentDTO> getByNom(@RequestParam String nom) {

        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le champ 'nom' est obligatoire");
        }
        return studentService.getStudentsByNom(nom);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Modifier/{id}")
    public StudentDTO update(@PathVariable Long id,@Valid @RequestBody StudentDTO studentDTO) {
        return studentService.updateStudent(id, studentDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        String message = studentService.deleteStudent(id);
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
        return ResponseEntity.ok(restoredStudent);
    }

}
