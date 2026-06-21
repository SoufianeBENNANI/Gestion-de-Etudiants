package org.sid.gestion_etudiant.Messaging.Service;

import lombok.AllArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GmailService {

    private final JavaMailSender mailSender;
    private final KafkaProducerService kafkaProducerService;

    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        AppEvent event = new AppEvent();
        event.setEntity(EventEntity.GMAIL);
        event.setAction(EventAction.SENT);
        event.setEntityId(null);
        event.setMessage("Email sent successfully to " + to);

        kafkaProducerService.sendEvent(event);
    }
}