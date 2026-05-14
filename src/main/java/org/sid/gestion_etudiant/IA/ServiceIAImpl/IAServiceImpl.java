package org.sid.gestion_etudiant.IA.ServiceIAImpl;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;
import org.sid.gestion_etudiant.IA.Repository.StudentIAPredictionRepo;
import org.sid.gestion_etudiant.IA.Service.IAService;
import org.sid.gestion_etudiant.Metier.Entity.Student;
import org.sid.gestion_etudiant.Metier.Repository.StudentRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class IAServiceImpl implements IAService {

    private final StudentRepo studentRepo;
    private final StudentIAPredictionRepo predictionRepo;

    @Override
    public StudentIAPrediction predict(Long studentId) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id " + studentId));

        double moyenne = 0.0;
        long absences = 0;

        double scoreRisque = calculateRiskScore(moyenne, absences);

        StudentIAPrediction prediction = new StudentIAPrediction();
        prediction.setStudent(student);
        prediction.setMoyenne(moyenne);
        prediction.setAbsences(absences);
        prediction.setScoreRisque(scoreRisque);
        prediction.setPrediction(calculatePrediction(scoreRisque));
        prediction.setNiveau(calculateNiveau(moyenne));
        prediction.setStatus(calculateStatus(scoreRisque));
        prediction.setRecommandation(calculateRecommendation(scoreRisque));
        prediction.setDate(LocalDateTime.now());
        prediction.setModelVersion("v1");

        return predictionRepo.save(prediction);
    }

    private double calculateRiskScore(double moyenne, long absences) {
        double score = 0;

        if (moyenne < 10) {
            score += 40;
        } else if (moyenne < 12) {
            score += 25;
        } else {
            score += 10;
        }

        if (absences >= 10) {
            score += 40;
        } else if (absences >= 5) {
            score += 25;
        } else {
            score += 10;
        }

        return Math.min(score, 100);
    }

    private String calculatePrediction(double scoreRisque) {
        if (scoreRisque >= 70) return "Risque élevé";
        if (scoreRisque >= 40) return "Risque modéré";
        return "Risque faible";
    }

    private String calculateNiveau(double moyenne) {
        if (moyenne >= 16) return "Excellent";
        if (moyenne >= 12) return "Moyen";
        return "Faible";
    }

    private String calculateStatus(double scoreRisque) {
        if (scoreRisque >= 70) return "AT_RISK";
        if (scoreRisque >= 40) return "MODERATE";
        return "GOOD";
    }

    private String calculateRecommendation(double scoreRisque) {
        if (scoreRisque >= 70) {
            return "Contacter l'étudiant et renforcer le suivi.";
        }

        if (scoreRisque >= 40) {
            return "Améliorer présence et travail.";
        }

        return "Continuer le bon suivi.";
    }
}