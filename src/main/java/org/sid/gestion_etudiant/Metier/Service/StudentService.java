package org.sid.gestion_etudiant.Metier.Service;

import org.sid.gestion_etudiant.Metier.dto.StudentDTO;

import java.util.List;

public interface StudentService {

    StudentDTO addStudent(StudentDTO studentDTO);

    List<StudentDTO> getAllStudents();

    List<StudentDTO> getArchivedStudents();

    List<StudentDTO> getStudentsByNom(String nom);

    StudentDTO updateStudent(Long id, StudentDTO studentDTO);

    String deleteStudent(Long id);

    StudentDTO restoreStudent(Long id);

    void deleteOldArchivedStudents();
}