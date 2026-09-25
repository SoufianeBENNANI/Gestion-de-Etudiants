package org.sid.gestion_etudiant.Messaging.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendMail(
            String to,
            String subject,
            String body
    ) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println(
                    "Gmail envoyé avec succès : "
                            + senderEmail
                            + " -> "
                            + to
            );

        } catch (MailAuthenticationException exception) {

            throw new RuntimeException(
                    "Authentification Gmail refusée. "
                            + "Vérifie MAIL_USERNAME et surtout "
                            + "le mot de passe d'application MAIL_PASSWORD.",
                    exception
            );

        } catch (MailException exception) {

            throw new RuntimeException(
                    "Erreur pendant l'envoi de l'email vers "
                            + to
                            + " : "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}