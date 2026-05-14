package org.sid.gestion_etudiant.IA.Service;

import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
import org.sid.gestion_etudiant.IA.DTO.StudentPerformanceDTO;

import java.util.List;

public interface PredictionService {

    List<StudentIAPrediction> getAll();

    List<StudentIAPrediction> getByStudentId(Long studentId);

    List<StudentPerformanceDTO> getPerformance();
}