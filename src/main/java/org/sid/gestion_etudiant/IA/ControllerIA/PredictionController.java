package org.sid.gestion_etudiant.IA.ControllerIA;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.DTO.StudentPerformanceDTO;
import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
import org.sid.gestion_etudiant.IA.Service.PredictionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@AllArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    // ADMIN + TEACHER
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/All")
    public List<StudentPerformanceDTO> getAll() {
        return predictionService.getAll();
    }

    // ADMIN + TEACHER
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/Performance")
    public List<StudentPerformanceDTO> getPerformance() {
        return predictionService.getPerformance();
    }

    // STUDENT
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my/{id}")
    public List<StudentIAPrediction> myPredictions(@PathVariable Long id) {
        return predictionService.getByStudentId(id);
    }
}