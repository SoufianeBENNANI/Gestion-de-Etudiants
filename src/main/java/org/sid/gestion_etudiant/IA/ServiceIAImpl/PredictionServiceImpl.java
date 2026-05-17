package org.sid.gestion_etudiant.IA.ServiceIAImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.DTO.StudentPerformanceDTO;
import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
import org.sid.gestion_etudiant.IA.Repository.StudentIAPredictionRepo;
import org.sid.gestion_etudiant.IA.Service.PredictionService;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final StudentIAPredictionRepo predictionRepo;
    private final StudentRepo studentRepo;

    @Override
    public List<StudentPerformanceDTO> getAll() {
        List<StudentIAPrediction> predictions = predictionRepo.findAll();

        return predictions.stream()
                .map(prediction -> {
                    Student student = prediction.getStudent();

                    return new StudentPerformanceDTO(
                            prediction.getId(),
                            student != null ? student.getId() : null,
                            student != null ? student.getNom() : null,
                            student != null ? student.getPrenom() : null,
                            student != null ? student.getEmail() : null,
                            prediction.getMoyenne(),
                            prediction.getAbsences(),
                            prediction.getPrediction(),
                            prediction.getScoreRisque(),
                            prediction.getNiveau(),
                            prediction.getRecommandation(),
                            prediction.getDate(),
                            prediction.getStatus(),
                            true
                    );
                })
                .toList();
    }

    @Override
    public List<StudentIAPrediction> getByStudentId(Long studentId) {
        return predictionRepo.findByStudentId(studentId);
    }

    @Override
    public List<StudentPerformanceDTO> getPerformance() {
        List<Student> students = studentRepo.findByArchivedFalse();

        return students.stream()
                .map(student -> {
                    Optional<StudentIAPrediction> optionalPrediction =
                            predictionRepo.findTopByStudentIdOrderByDateDesc(student.getId());

                    if (optionalPrediction.isPresent()) {
                        StudentIAPrediction prediction = optionalPrediction.get();

                        return new StudentPerformanceDTO(
                                prediction.getId(),
                                student.getId(),
                                student.getNom(),
                                student.getPrenom(),
                                student.getEmail(),
                                prediction.getMoyenne(),
                                prediction.getAbsences(),
                                prediction.getPrediction(),
                                prediction.getScoreRisque(),
                                prediction.getNiveau(),
                                prediction.getRecommandation(),
                                prediction.getDate(),
                                prediction.getStatus(),
                                true
                        );
                    }

                    return new StudentPerformanceDTO(
                            null,
                            student.getId(),
                            student.getNom(),
                            student.getPrenom(),
                            student.getEmail(),
                            0.0,
                            0,
                            "No prediction yet",
                            0.0,
                            "N/A",
                            "Generate prediction for this student",
                            null,
                            "NO_PREDICTION",
                            false
                    );
                })
                .toList();
    }
}