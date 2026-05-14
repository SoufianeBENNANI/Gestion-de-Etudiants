package org.sid.gestion_etudiant.Optionnel.Mapper;

import org.sid.gestion_etudiant.Optionnel.DTO.TeacherDTO;
import org.sid.gestion_etudiant.Optionnel.Entity.Teacher;

public class TeacherMapper {

    public static Teacher toEntity(TeacherDTO teacherDTO){
        Teacher teacher = new Teacher();
        teacher.setNom(teacherDTO.getNom());
        teacher.setPrenom(teacherDTO.getPrenom());
        teacher.setEmail(teacherDTO.getEmail());
        teacher.setSpecialite(teacherDTO.getSpecialite());
        return teacher;
    }

    public static TeacherDTO toDto(Teacher teacher){
        TeacherDTO teacherDTO = new TeacherDTO();
        teacherDTO.setNom(teacher.getNom());
        teacherDTO.setPrenom(teacher.getPrenom());
        teacherDTO.setEmail(teacher.getEmail());
        teacherDTO.setSpecialite(teacher.getSpecialite());

        if (teacher.getDepartement() != null) {
            teacherDTO.setDepartementId(teacher.getDepartement().getId());
        }

        return teacherDTO;
    }
}
