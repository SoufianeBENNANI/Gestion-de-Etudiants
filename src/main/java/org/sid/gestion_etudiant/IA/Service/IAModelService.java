package org.sid.gestion_etudiant.IA.Service;

import org.sid.gestion_etudiant.IA.Entity.IAModel;

import java.util.List;

public interface IAModelService {

    IAModel create(IAModel model);

    List<IAModel> getAll();

    List<IAModel> getArchivedModels();

    IAModel getById(Long id);

    IAModel update(Long id, IAModel model);

    String delete(Long id);

    IAModel restore(Long id);

    void deleteOldArchivedModels();
}