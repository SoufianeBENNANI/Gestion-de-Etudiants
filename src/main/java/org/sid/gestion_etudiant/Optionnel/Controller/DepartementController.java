package org.sid.gestion_etudiant.Optionnel.Controller;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Optionnel.DTO.DepartementDTO;
import org.sid.gestion_etudiant.Optionnel.Service.DepartementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@AllArgsConstructor
@CrossOrigin("*")
public class DepartementController {

    private final DepartementService departementService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/Add")
    public DepartementDTO addDepartment(@RequestBody DepartementDTO dto) {
        return departementService.addDepartment(dto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/AllDepartments")
    public List<DepartementDTO> getAllDepartments() {
        return departementService.getAllDepartments();
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/search")
    public List<DepartementDTO> getDepartmentsByNom(@RequestParam String nom) {
        return departementService.getDepartmentsByNom(nom);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/Update/{id}")
    public DepartementDTO updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartementDTO dto
    ) {
        return departementService.updateDepartment(id, dto);
    }


    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/Delete/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        return departementService.deleteDepartment(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/Archive")
    public List<DepartementDTO> getArchivedDepartments() {
        return departementService.getArchivedDepartments();
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/Restaurer/{id}")
    public DepartementDTO restoreDepartment(@PathVariable Long id) {
        return departementService.restoreDepartment(id);
    }
}