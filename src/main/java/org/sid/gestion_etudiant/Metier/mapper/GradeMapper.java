package org.sid.gestion_etudiant.Metier.mapper;

import org.sid.gestion_etudiant.Metier.Entity.Grade;
import org.sid.gestion_etudiant.Metier.dto.GradeDTO;

public class GradeMapper {

    public static GradeDTO toDto(Grade grade) {
        GradeDTO gradeDTO = new GradeDTO();

        gradeDTO.setId(grade.getId());
        gradeDTO.setNote(grade.getNote());
        gradeDTO.setSemestre(grade.getSemestre());
        gradeDTO.setArchived(grade.isArchived());
        gradeDTO.setArchivedAt(grade.getArchivedAt());

        if (grade.getStudent() != null) {
            gradeDTO.setStudentId(grade.getStudent().getId());
        }

        if (grade.getCourses() != null) {
            gradeDTO.setCourseId(grade.getCourses().getId());
        }

        return gradeDTO;
    }

    public static Grade toEntity(GradeDTO gradeDTO) {
        Grade grade = new Grade();

        grade.setNote(gradeDTO.getNote());
        grade.setSemestre(gradeDTO.getSemestre());
        grade.setArchived(gradeDTO.isArchived());
        grade.setArchivedAt(gradeDTO.getArchivedAt());

        return grade;
    }
}