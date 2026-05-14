package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Service.PayementService;
import org.sid.gestion_etudiant.Metier.dto.PayementDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Payement")
@AllArgsConstructor
public class PayementController {

    private final PayementService payementService;

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/Ajouter")
    public PayementDTO create(@Valid @RequestBody PayementDTO payementDTO) {
        return payementService.addPayement(payementDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','MANAGER')")
    @GetMapping("/AllPayement")
    public List<PayementDTO> getAll() {
        return payementService.getAllPayements();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/Archive")
    public List<PayementDTO> getArchivedPayements() {
        return payementService.getArchivedPayements();
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','MANAGER')")
    @GetMapping("/Recherche/{id}")
    public PayementDTO getById(@PathVariable Long id) {
        return payementService.getPayementById(id);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/Modifier/{id}")
    public PayementDTO update(
            @PathVariable Long id,
            @Valid @RequestBody PayementDTO payementDTO
    ) {
        return payementService.updatePayement(id, payementDTO);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deletePayement(@PathVariable Long id) {
        String message = payementService.deletePayement(id);
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<PayementDTO> restorePayement(@PathVariable Long id) {
        PayementDTO restoredPayement = payementService.restorePayement(id);
        return ResponseEntity.ok(restoredPayement);
    }
}