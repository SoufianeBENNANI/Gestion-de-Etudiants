package org.sid.gestion_etudiant.Metier.exception;

public class PayementNotFoundException extends RuntimeException {
    public PayementNotFoundException(String message) {
        super(message);
    }
}
