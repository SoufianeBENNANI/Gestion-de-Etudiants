package org.sid.gestion_etudiant.Metier.mapper;

import org.sid.gestion_etudiant.Metier.Entity.ClassesEntity;
import org.sid.gestion_etudiant.Metier.dto.ClassesDTO;

public class ClassesMapper {

    public static ClassesDTO toDTO(ClassesEntity classesEntity) {
        ClassesDTO classesDTO = new ClassesDTO();

        classesDTO.setId(classesEntity.getId());
        classesDTO.setNom(classesEntity.getNom());
        classesDTO.setNiveau(classesEntity.getNiveau());
        classesDTO.setAnnee(classesEntity.getAnnee());
        classesDTO.setArchived(classesEntity.isArchived());
        classesDTO.setArchivedAt(classesEntity.getArchivedAt());

        return classesDTO;
    }

    public static ClassesEntity toEntity(ClassesDTO classesDTO) {
        ClassesEntity classesEntity = new ClassesEntity();

        classesEntity.setNom(classesDTO.getNom());
        classesEntity.setNiveau(classesDTO.getNiveau());
        classesEntity.setAnnee(classesDTO.getAnnee());
        classesEntity.setArchived(classesDTO.isArchived());
        classesEntity.setArchivedAt(classesDTO.getArchivedAt());

        return classesEntity;
    }
}