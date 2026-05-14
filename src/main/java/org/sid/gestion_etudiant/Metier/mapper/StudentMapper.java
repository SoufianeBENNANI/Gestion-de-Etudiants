package org.sid.gestion_etudiant.Metier.mapper;

import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.dto.StudentDTO;

public class StudentMapper {

    public static StudentDTO toDTO(Student student) {
        StudentDTO dto = new StudentDTO();

        dto.setId(student.getId());
        dto.setNom(student.getNom());
        dto.setPrenom(student.getPrenom());
        dto.setEmail(student.getEmail());
        dto.setDate_Naissance(student.getDate_Naissance());
        dto.setGenre(student.getGenre());
        dto.setAdresse(student.getAdresse());
        dto.setTelephone(student.getTelephone());
        dto.setCreatedAt(student.getCreatedAt());
        dto.setUpdatedAt(student.getUpdatedAt());

        dto.setArchived(student.isArchived());
        dto.setArchivedAt(student.getArchivedAt());

        return dto;
    }

    public static Student toEntity(StudentDTO dto) {
        Student student = new Student();

        student.setNom(dto.getNom());
        student.setPrenom(dto.getPrenom());
        student.setEmail(dto.getEmail());
        student.setDate_Naissance(dto.getDate_Naissance());
        student.setGenre(dto.getGenre());
        student.setAdresse(dto.getAdresse());
        student.setTelephone(dto.getTelephone());

        student.setArchived(dto.isArchived());
        student.setArchivedAt(dto.getArchivedAt());

        return student;
    }
}