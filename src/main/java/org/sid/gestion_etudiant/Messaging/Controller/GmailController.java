package org.sid.gestion_etudiant.Messaging.Controller;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Messaging.Service.GmailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gmail")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class GmailController {

    private final GmailService gmailService;
    private final KafkaProducerService kafkaProducerService;

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/send")
    public String sendMail(@RequestParam String to,
                           @RequestParam String subject,
                           @RequestParam String body) {

        gmailService.sendMail(to, subject, body);

        AppEvent event = new AppEvent();
        event.setEntity(EventEntity.GMAIL);
        event.setAction(EventAction.SENT);
        event.setEntityId(null);
        event.setMessage(
                "Vous avez un message Gmail de Soufiane Bennani. "
                        + "Accédez à Gmail pour lire le contenu de ce message."
        );

        kafkaProducerService.sendEvent(event);

        return "Email sent successfully";
    }
}