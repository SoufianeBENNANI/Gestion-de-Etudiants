package org.sid.gestion_etudiant.Metier.Service;

import org.sid.gestion_etudiant.Metier.dto.GradeDTO;

import java.util.List;

public interface GradeService {

    GradeDTO addGrade(GradeDTO gradeDTO);

    List<GradeDTO> getAllGrade();

    List<GradeDTO> getArchivedGrades();

    GradeDTO getGradeById(Long id);

    GradeDTO updateGrade(Long id, GradeDTO gradeDTO);

    String deleteGrade(Long id);

    GradeDTO restoreGrade(Long id);

    void deleteOldArchivedGrades();
}