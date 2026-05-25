package org.sid.gestion_etudiant.Metier.Service;

import org.sid.gestion_etudiant.Metier.dto.StudentDTO;

import java.util.List;

public interface StudentService {

    StudentDTO addStudent(StudentDTO dto);

    List<StudentDTO> getAllStudents();

    List<StudentDTO> getStudentsByNom(String nom);

    StudentDTO updateStudent(Long id, StudentDTO dto);

    String deleteStudent(Long id);

    List<StudentDTO> getArchivedStudents();

    StudentDTO restoreStudent(Long id);

    void deleteOldArchivedStudents();

    byte[] generateStudentsPdf();
}