package org.sid.gestion_etudiant.Metier.mapper;

import org.sid.gestion_etudiant.Metier.Entity.Grade;
import org.sid.gestion_etudiant.Metier.dto.GradeDTO;

public class GradeMapper {

    public static GradeDTO toDto(Grade grade) {
        if (grade == null) {
            return null;
        }

        GradeDTO gradeDTO = new GradeDTO();

        gradeDTO.setId(grade.getId());
        gradeDTO.setNote(grade.getNote());
        gradeDTO.setSemestre(grade.getSemestre());
        gradeDTO.setArchived(grade.isArchived());
        gradeDTO.setArchivedAt(grade.getArchivedAt());

        if (grade.getStudent() != null) {
            gradeDTO.setStudentId(grade.getStudent().getId());

            String prenom = grade.getStudent().getPrenom() == null
                    ? ""
                    : grade.getStudent().getPrenom();

            String nom = grade.getStudent().getNom() == null
                    ? ""
                    : grade.getStudent().getNom();

            String fullName = (prenom + " " + nom).trim();
            gradeDTO.setStudentName(fullName.isEmpty() ? "N/A" : fullName);
        }

        if (grade.getCourses() != null) {
            gradeDTO.setCourseId(grade.getCourses().getId());

            String courseName = grade.getCourses().getNom() == null
                    ? "N/A"
                    : grade.getCourses().getNom();

            gradeDTO.setCourseName(courseName);
        }

        return gradeDTO;
    }

    public static Grade toEntity(GradeDTO gradeDTO) {
        if (gradeDTO == null) {
            return null;
        }

        Grade grade = new Grade();

        grade.setNote(gradeDTO.getNote());
        grade.setSemestre(gradeDTO.getSemestre());

        grade.setArchived(gradeDTO.isArchived());
        grade.setArchivedAt(gradeDTO.getArchivedAt());

        return grade;
    }
}