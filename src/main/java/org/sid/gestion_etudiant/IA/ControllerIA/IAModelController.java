package org.sid.gestion_etudiant.IA.ControllerIA;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.Entity.IAModel;
import org.sid.gestion_etudiant.IA.Service.IAModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/models")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class IAModelController {

    private final IAModelService iaModelService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/Ajouter")
    public ResponseEntity<IAModel> create(@RequestBody IAModel model) {
        IAModel createdModel = iaModelService.create(model);
        return ResponseEntity.ok(createdModel);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/AllModels")
    public ResponseEntity<List<IAModel>> getAll() {
        List<IAModel> models = iaModelService.getAll();
        return ResponseEntity.ok(models);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Archive")
    public ResponseEntity<List<IAModel>> getArchive() {
        List<IAModel> archivedModels = iaModelService.getArchivedModels();
        return ResponseEntity.ok(archivedModels);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/Recherche/{id}")
    public ResponseEntity<IAModel> getById(@PathVariable Long id) {
        IAModel model = iaModelService.getById(id);
        return ResponseEntity.ok(model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Modifier/{id}")
    public ResponseEntity<IAModel> update(
            @PathVariable Long id,
            @RequestBody IAModel model
    ) {
        IAModel updatedModel = iaModelService.update(id, model);
        return ResponseEntity.ok(updatedModel);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/Supprimer/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String message = iaModelService.delete(id);
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/Restaurer/{id}")
    public ResponseEntity<IAModel> restore(@PathVariable Long id) {
        IAModel restoredModel = iaModelService.restore(id);
        return ResponseEntity.ok(restoredModel);
    }
}