package org.sid.gestion_etudiant.IA.ServiceIAImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.DTO.StudentPerformanceDTO;
import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
import org.sid.gestion_etudiant.IA.Repository.StudentIAPredictionRepo;
import org.sid.gestion_etudiant.IA.Service.PredictionService;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.AttendanceRepo;
import org.sid.gestion_etudiant.Metier.Repository.GradeRepo;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final StudentIAPredictionRepo predictionRepo;
    private final StudentRepo studentRepo;
    private final GradeRepo gradeRepo;
    private final AttendanceRepo attendanceRepo;

    @Override
    public List<StudentPerformanceDTO> getAll() {
        List<StudentIAPrediction> predictions = predictionRepo.findAll();

        return predictions.stream()
                .map(prediction -> {
                    Student student = prediction.getStudent();

                    if (student == null) {
                        return new StudentPerformanceDTO(
                                prediction.getId(),
                                null,
                                null,
                                null,
                                null,
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

                    return buildStudentPerformanceDTO(student, prediction);
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

                    return buildStudentPerformanceDTO(
                            student,
                            optionalPrediction.orElse(null)
                    );
                })
                .toList();
    }

    private StudentPerformanceDTO buildStudentPerformanceDTO(
            Student student,
            StudentIAPrediction prediction
    ) {
        double moyenne = calculateMoyenne(student.getId());
        long absences = calculateAbsences(student.getId());

        double scoreRisque = calculateRiskScore(moyenne, absences);
        String predictionText = getPredictionText(scoreRisque);
        String niveau = getNiveau(scoreRisque);
        String status = getStatus(scoreRisque);
        String recommandation = getRecommandation(scoreRisque, moyenne, absences);

        return new StudentPerformanceDTO(
                prediction != null ? prediction.getId() : null,
                student.getId(),
                student.getNom(),
                student.getPrenom(),
                student.getEmail(),
                moyenne,
                absences,
                predictionText,
                scoreRisque,
                niveau,
                recommandation,
                prediction != null ? prediction.getDate() : null,
                status,
                prediction != null
        );
    }

    private double calculateMoyenne(Long studentId) {
        Double moyenne = gradeRepo.calculateAverageByStudentId(studentId);
        return moyenne != null ? moyenne : 0.0;
    }

    private long calculateAbsences(Long studentId) {
        Long absences = attendanceRepo.countAbsencesByStudentId(studentId);
        return absences != null ? absences : 0L;
    }

    private double calculateRiskScore(double moyenne, long absences) {
        double noteRisk = (20 - moyenne) * 3;
        double absenceRisk = absences * 5;

        double score = noteRisk + absenceRisk;

        return Math.max(0, Math.min(score, 100));
    }

    private String getPredictionText(double scoreRisque) {
        if (scoreRisque >= 70) return "Risque élevé";
        if (scoreRisque >= 40) return "Risque modéré";
        return "Risque faible";
    }

    private String getNiveau(double scoreRisque) {
        if (scoreRisque >= 70) return "Élevé";
        if (scoreRisque >= 40) return "Moyen";
        return "Faible";
    }

    private String getStatus(double scoreRisque) {
        if (scoreRisque >= 70) return "HIGH";
        if (scoreRisque >= 40) return "MODERATE";
        return "LOW";
    }

    private String getRecommandation(double scoreRisque, double moyenne, long absences) {
        if (scoreRisque >= 70) {
            return "Risque élevé : suivi urgent recommandé.";
        }

        if (scoreRisque >= 40) {
            return "Risque modéré : améliorer présence et travail.";
        }

        if (moyenne >= 15 && absences <= 2) {
            return "Bonne performance : continuer les efforts.";
        }

        return "Risque faible : maintenir un bon rythme.";
    }
}