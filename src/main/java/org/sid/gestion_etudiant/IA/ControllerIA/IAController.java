package org.sid.gestion_etudiant.IA.ControllerIA;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
import org.sid.gestion_etudiant.IA.Service.IAService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
public class IAController {

    private final IAService iaService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/predict/{id}")
    public StudentIAPrediction predict(@PathVariable Long id) {
        return iaService.predict(id);
    }
}