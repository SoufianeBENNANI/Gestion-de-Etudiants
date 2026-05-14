package org.sid.gestion_etudiant.Metier.ServiceImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Metier.Entity.ClassesEntity;
import org.sid.gestion_etudiant.Metier.Repository.ClassesRepo;
import org.sid.gestion_etudiant.Metier.Service.ClassesService;
import org.sid.gestion_etudiant.Metier.dto.ClassesDTO;
import org.sid.gestion_etudiant.Metier.exception.ClassesNotFoundException;
import org.sid.gestion_etudiant.Metier.mapper.ClassesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ClassesServiceImpl implements ClassesService {

    private final ClassesRepo classesRepo;

    @Override
    public ClassesDTO addClasses(ClassesDTO classesDTO) {
        ClassesEntity classesEntity = ClassesMapper.toEntity(classesDTO);

        classesEntity.setArchived(false);
        classesEntity.setArchivedAt(null);

        return ClassesMapper.toDTO(classesRepo.save(classesEntity));
    }

    @Override
    public List<ClassesDTO> getAllClasses() {
        return classesRepo.findByArchivedFalse()
                .stream()
                .map(ClassesMapper::toDTO)
                .toList();
    }

    @Override
    public List<ClassesDTO> getArchivedClasses() {
        return classesRepo.findByArchivedTrue()
                .stream()
                .map(ClassesMapper::toDTO)
                .toList();
    }

    @Override
    public ClassesDTO getClasseById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Le champ ID est obligatoire");
        }

        ClassesEntity classes = classesRepo.findById(id)
                .orElseThrow(() -> new ClassesNotFoundException("Classe non trouvée avec id : " + id));

        return ClassesMapper.toDTO(classes);
    }

    @Override
    public ClassesDTO updateClasses(Long id, ClassesDTO classesDTO) {
        ClassesEntity classesEntity = classesRepo.findById(id)
                .orElseThrow(() -> new ClassesNotFoundException("Classe Not Found"));

        if (classesEntity.isArchived()) {
            throw new RuntimeException("Impossible de modifier une classe archivée. Restaurer d'abord.");
        }

        classesEntity.setNom(classesDTO.getNom());
        classesEntity.setNiveau(classesDTO.getNiveau());
        classesEntity.setAnnee(classesDTO.getAnnee());

        return ClassesMapper.toDTO(classesRepo.save(classesEntity));
    }

    @Override
    public String deleteClasses(Long id) {
        ClassesEntity classesEntity = classesRepo.findById(id)
                .orElseThrow(() -> new ClassesNotFoundException("Classe non trouvée avec l'id : " + id));

        if (classesEntity.isArchived()) {
            return "Classe déjà archivée avec id : " + id;
        }

        classesEntity.setArchived(true);
        classesEntity.setArchivedAt(LocalDateTime.now());

        classesRepo.save(classesEntity);

        return "Classe archivée avec succès. Elle sera supprimée définitivement après 7 jours.";
    }

    @Override
    public ClassesDTO restoreClasses(Long id) {
        ClassesEntity classesEntity = classesRepo.findById(id)
                .orElseThrow(() -> new ClassesNotFoundException("Classe non trouvée avec l'id : " + id));

        if (!classesEntity.isArchived()) {
            throw new RuntimeException("Cette classe n'est pas archivée.");
        }

        classesEntity.setArchived(false);
        classesEntity.setArchivedAt(null);

        return ClassesMapper.toDTO(classesRepo.save(classesEntity));
    }

    @Override
    @Transactional
    public void deleteOldArchivedClasses() {
        LocalDateTime limitDate = LocalDateTime.now().minusDays(7);

        List<ClassesEntity> oldArchivedClasses =
                classesRepo.findByArchivedTrueAndArchivedAtBefore(limitDate);

        classesRepo.deleteAll(oldArchivedClasses);
    }
}