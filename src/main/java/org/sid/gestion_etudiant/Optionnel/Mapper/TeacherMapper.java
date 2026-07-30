package org.sid.gestion_etudiant.Optionnel.Mapper;

import org.sid.gestion_etudiant.Optionnel.DTO.TeacherDTO;
import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;

public class TeacherMapper {

    public static Teacher toEntity(TeacherDTO teacherDTO) {
        Teacher teacher = new Teacher();

        teacher.setId(teacherDTO.getId());
        teacher.setNom(teacherDTO.getNom());
        teacher.setPrenom(teacherDTO.getPrenom());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setSpecialite(teacherDTO.getSpecialite());
        teacher.setArchived(teacherDTO.isArchived());
        teacher.setArchivedAt(teacherDTO.getArchivedAt());

        return teacher;
    }

    public static TeacherDTO toDto(Teacher teacher) {
        TeacherDTO teacherDTO = new TeacherDTO();

        teacherDTO.setId(teacher.getId());
        teacherDTO.setNom(teacher.getNom());
        teacherDTO.setPrenom(teacher.getPrenom());
        teacherDTO.setEmail(teacher.getEmail());
        teacherDTO.setSpecialite(teacher.getSpecialite());
        teacherDTO.setArchived(teacher.isArchived());
        teacherDTO.setArchivedAt(teacher.getArchivedAt());

        if (teacher.getDepartement() != null) {
            teacherDTO.setDepartementNom(teacher.getDepartement().getNom());
        }

        return teacherDTO;
    }
}