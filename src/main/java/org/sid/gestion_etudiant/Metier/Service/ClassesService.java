package org.sid.gestion_etudiant.Metier.Service;

import org.sid.gestion_etudiant.Metier.dto.ClassesDTO;

import java.util.List;

public interface ClassesService {

    ClassesDTO addClasses(ClassesDTO classesDTO);

    List<ClassesDTO> getAllClasses();

    List<ClassesDTO> getArchivedClasses();

    ClassesDTO getClasseById(Long id);

    ClassesDTO updateClasses(Long id, ClassesDTO classesDTO);

    String deleteClasses(Long id);

    ClassesDTO restoreClasses(Long id);

    void deleteOldArchivedClasses();
}