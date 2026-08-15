package org.sid.gestion_etudiant.IA.Service;

import org.sid.gestion_etudiant.IA.DTO.StudentPerformanceDTO;
import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;

import java.util.List;

public interface PredictionService {

    List<StudentPerformanceDTO> getAll();

    List<StudentIAPrediction> getByStudentId(Long studentId);

    List<StudentPerformanceDTO> getPerformance();

    StudentPerformanceDTO getMyPerformance(String keycloakId, String email);
}