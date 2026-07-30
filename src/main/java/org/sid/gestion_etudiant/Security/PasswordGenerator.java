package org.sid.gestion_etudiant.Security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateTemporaryPassword() {

        StringBuilder password = new StringBuilder();

        // Exemple : 745215
        for (int i = 0; i < 6; i++) {
            password.append(randomCharacter(NUMBERS));
        }

        // Exemple : fg
        for (int i = 0; i < 2; i++) {
            password.append(randomCharacter(LETTERS));
        }

        return password.toString();
    }

    private char randomCharacter(String characters) {

        int index = secureRandom.nextInt(characters.length());

        return characters.charAt(index);
    }
}