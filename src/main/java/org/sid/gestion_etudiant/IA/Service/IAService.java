package org.sid.gestion_etudiant.IA.Service;

import org.sid.gestion_etudiant.IA.Entity.StudentIAPrediction;

public interface IAService {
    StudentIAPrediction predict(Long studentId);
}
