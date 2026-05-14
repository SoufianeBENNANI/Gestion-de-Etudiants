package org.sid.gestion_etudiant.IA.ServiceIAImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.Entity.IAModel;
import org.sid.gestion_etudiant.IA.Repository.IAModelRepo;
import org.sid.gestion_etudiant.IA.Service.IAModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class IAModelServiceImpl implements IAModelService {

    private final IAModelRepo modelRepo;

    @Override
    public IAModel create(IAModel model) {
        model.setArchived(false);
        model.setArchivedAt(null);

        if (model.getCreatedAt() == null) {
            model.setCreatedAt(LocalDateTime.now());
        }

        return modelRepo.save(model);
    }

    @Override
    public List<IAModel> getAll() {
        return modelRepo.findByArchivedFalse();
    }

    @Override
    public List<IAModel> getArchivedModels() {
        return modelRepo.findByArchivedTrue();
    }

    @Override
    public IAModel getById(Long id) {
        return modelRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("IA Model not found with id: " + id));
    }

    @Override
    public IAModel update(Long id, IAModel model) {
        IAModel existingModel = modelRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("IA Model not found with id: " + id));

        if (existingModel.isArchived()) {
            throw new RuntimeException("Impossible de modifier un modèle IA archivé. Restaurer d'abord.");
        }

        existingModel.setName(model.getName());
        existingModel.setVersion(model.getVersion());
        existingModel.setAccuracy(model.getAccuracy());

        return modelRepo.save(existingModel);
    }

    @Override
    public String delete(Long id) {
        IAModel model = modelRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("IA Model not found with id: " + id));

        if (model.isArchived()) {
            return "IA Model déjà archivé avec id : " + id;
        }

        model.setArchived(true);
        model.setArchivedAt(LocalDateTime.now());

        modelRepo.save(model);

        return "IA Model archivé avec succès. Il sera supprimé définitivement après 7 jours.";
    }

    @Override
    public IAModel restore(Long id) {
        IAModel model = modelRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("IA Model not found with id: " + id));

        if (!model.isArchived()) {
            throw new RuntimeException("Ce modèle IA n'est pas archivé.");
        }

        model.setArchived(false);
        model.setArchivedAt(null);

        return modelRepo.save(model);
    }

    @Override
    @Transactional
    public void deleteOldArchivedModels() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<IAModel> oldArchivedModels =
                modelRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        modelRepo.deleteAll(oldArchivedModels);
    }
}