package org.sid.gestion_etudiant.Optionnel.Service;

import org.sid.gestion_etudiant.Metier.dto.CreateTeacherRequest;
import org.sid.gestion_etudiant.Metier.dto.CreatedAccountResponse;
import org.sid.gestion_etudiant.Optionnel.DTO.TeacherDTO;

import java.util.List;

public interface TeacherService {

    CreatedAccountResponse createTeacherAccount(
            CreateTeacherRequest request
    );

    TeacherDTO addTeacher(TeacherDTO dto);

    List<TeacherDTO> getAllTeachers();

    List<TeacherDTO> getTeachersByNom(String nom);

    TeacherDTO getTeacherById(Long id);

    TeacherDTO getTeacherByKeycloakId(String keycloakId);

    TeacherDTO updateTeacher(Long id, TeacherDTO dto);

    String deleteTeacher(Long id);

    List<TeacherDTO> getArchivedTeachers();

    TeacherDTO restoreTeacher(Long id);

    void deleteOldArchivedTeachers();

    byte[] generateTeachersPdf();
}