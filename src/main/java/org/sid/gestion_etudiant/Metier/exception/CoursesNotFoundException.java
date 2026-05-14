package org.sid.gestion_etudiant.Metier.exception;

public class CoursesNotFoundException extends RuntimeException {
    public CoursesNotFoundException(String message) {
        super(message);
    }
}
