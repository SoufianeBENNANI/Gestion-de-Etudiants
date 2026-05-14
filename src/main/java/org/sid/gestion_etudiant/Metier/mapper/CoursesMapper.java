package org.sid.gestion_etudiant.Metier.mapper;

import org.sid.gestion_etudiant.Metier.Entity.Courses;
import org.sid.gestion_etudiant.Metier.dto.CoursesDTO;

public class CoursesMapper {

    public static CoursesDTO toDto(Courses courses) {
        CoursesDTO coursesDTO = new CoursesDTO();

        coursesDTO.setId(courses.getId());
        coursesDTO.setNom(courses.getNom());
        coursesDTO.setDescription(courses.getDescription());
        coursesDTO.setCredits(courses.getCredits());
        coursesDTO.setArchived(courses.isArchived());
        coursesDTO.setArchivedAt(courses.getArchivedAt());

        return coursesDTO;
    }

    public static Courses toEntity(CoursesDTO coursesDTO) {
        Courses courses = new Courses();

        courses.setNom(coursesDTO.getNom());
        courses.setDescription(coursesDTO.getDescription());
        courses.setCredits(coursesDTO.getCredits());
        courses.setArchived(coursesDTO.isArchived());
        courses.setArchivedAt(coursesDTO.getArchivedAt());

        return courses;
    }
}