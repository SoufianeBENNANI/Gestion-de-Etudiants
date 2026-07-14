package org.sid.gestion_etudiant.Notification.Service;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Messaging.Service.GmailService;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final GmailService gmailService;
    private final KafkaProducerService kafkaProducerService;

    private static final Map<RecipientRole, Set<RecipientRole>>
            ALLOWED_RECIPIENTS = Map.of(

            RecipientRole.ADMIN,
            Set.of(
                    RecipientRole.TEACHER,
                    RecipientRole.STUDENT,
                    RecipientRole.MANAGER
            ),

            RecipientRole.STUDENT,
            Set.of(
                    RecipientRole.TEACHER,
                    RecipientRole.ADMIN
            ),

            RecipientRole.TEACHER,
            Set.of(
                    RecipientRole.STUDENT,
                    RecipientRole.ADMIN,
                    RecipientRole.MANAGER
            ),

            RecipientRole.MANAGER,
            Set.of(
                    RecipientRole.ADMIN,
                    RecipientRole.TEACHER
            )
    );

    public void sendNotification(
            String senderEmail,
            RecipientRole senderRole,
            String recipientEmail,
            RecipientRole recipientRole,
            String subject,
            String body,
            EventEntity entity,
            EventAction action,
            Long entityId
    ) {
        validatePermission(
                senderRole,
                recipientRole
        );

        /*
         * Envoi du message Gmail.
         */
        gmailService.sendMail(
                recipientEmail,
                subject,
                body
        );

        /*
         * Événement Kafka destiné au véritable destinataire.
         */
        AppEvent recipientEvent = createEvent(
                senderEmail,
                senderRole,
                recipientEmail,
                recipientRole,
                body,
                entity,
                action,
                entityId
        );

        kafkaProducerService.sendEvent(
                recipientEvent
        );

        /*
         * Lorsque l'ADMIN envoie une notification vers un autre rôle,
         * une copie Kafka est envoyée à l'ADMIN pour qu'elle apparaisse
         * également dans sa cloche.
         */
        if (
                senderRole == RecipientRole.ADMIN &&
                        recipientRole != RecipientRole.ADMIN
        ) {
            AppEvent adminEvent = createEvent(
                    senderEmail,
                    senderRole,
                    senderEmail,
                    RecipientRole.ADMIN,
                    body,
                    entity,
                    action,
                    entityId
            );

            kafkaProducerService.sendEvent(
                    adminEvent
            );
        }
    }

    private AppEvent createEvent(
            String senderEmail,
            RecipientRole senderRole,
            String recipientEmail,
            RecipientRole recipientRole,
            String body,
            EventEntity entity,
            EventAction action,
            Long entityId
    ) {
        AppEvent event = new AppEvent();

        event.setEntity(
                entity != null
                        ? entity
                        : EventEntity.GMAIL
        );

        event.setAction(
                action != null
                        ? action
                        : EventAction.SENT
        );

        event.setEntityId(entityId);
        event.setMessage(body);

        event.setSenderEmail(senderEmail);
        event.setSenderRole(senderRole);

        event.setRecipientEmail(recipientEmail);
        event.setRecipientRole(recipientRole);

        return event;
    }

    private void validatePermission(
            RecipientRole senderRole,
            RecipientRole recipientRole
    ) {
        if (senderRole == null) {
            throw new AccessDeniedException(
                    "Le rôle de l'expéditeur est obligatoire."
            );
        }

        if (recipientRole == null) {
            throw new AccessDeniedException(
                    "Le rôle du destinataire est obligatoire."
            );
        }

        Set<RecipientRole> allowedRoles =
                ALLOWED_RECIPIENTS.getOrDefault(
                        senderRole,
                        Set.of()
                );

        if (!allowedRoles.contains(recipientRole)) {
            throw new AccessDeniedException(
                    "Le rôle "
                            + senderRole
                            + " ne peut pas envoyer un message au rôle "
                            + recipientRole
            );
        }
    }
}