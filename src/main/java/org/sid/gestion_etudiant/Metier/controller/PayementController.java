package org.sid.gestion_etudiant.Metier.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Metier.Service.PayementService;
import org.sid.gestion_etudiant.Metier.dto.PayementDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/Payement")
@AllArgsConstructor
public class PayementController {

    private final PayementService payementService;
    private final KafkaProducerService kafkaProducerService;

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/Ajouter")
    public PayementDTO create(@Valid @RequestBody PayementDTO payementDTO) {
        PayementDTO savedPayement = payementService.addPayement(payementDTO);
        sendEvent(EventAction.CREATED, savedPayement.getId(), "Payement created successfully");
        return savedPayement;
    }

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','MANAGER')")
    @GetMapping("/AllPayement")
    public List<PayementDTO> getAll() {
        return payementService.getAllPayements();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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
        PayementDTO updatedPayement = payementService.updatePayement(id, payementDTO);
        sendEvent(EventAction.UPDATED, id, "Payement updated successfully");
        return updatedPayement;
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> deletePayement(@PathVariable Long id) {
        String message = payementService.deletePayement(id);
        sendEvent(EventAction.DELETED, id, "Payement deleted successfully");
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<PayementDTO> restorePayement(@PathVariable Long id) {
        PayementDTO restoredPayement = payementService.restorePayement(id);
        sendEvent(EventAction.RESTORED, id, "Payement restored successfully");
        return ResponseEntity.ok(restoredPayement);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/DownloadPDF")
    public ResponseEntity<byte[]> downloadPayementsPdf() {
        byte[] pdf = payementService.generatePayementsPdf();
        sendEvent(EventAction.GENERATED, null, "Payements PDF generated successfully");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payements-list.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void sendEvent(EventAction action, Long entityId, String message) {
        AppEvent event = new AppEvent();
        event.setEntity(EventEntity.PAYEMENT);
        event.setAction(action);
        event.setEntityId(entityId);
        event.setMessage(message);
        kafkaProducerService.sendEvent(event);
    }
}