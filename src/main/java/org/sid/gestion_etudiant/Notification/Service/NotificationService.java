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

    /*
     * Expéditeur -> destinataires autorisés
     */
    private static final Map<RecipientRole, Set<RecipientRole>>
            ALLOWED_RECIPIENTS = Map.of(

            /*
             * ADMIN peut envoyer à :
             * TEACHER, STUDENT et MANAGER.
             */
            RecipientRole.ADMIN,
            Set.of(
                    RecipientRole.TEACHER,
                    RecipientRole.STUDENT,
                    RecipientRole.MANAGER
            ),

            /*
             * STUDENT peut envoyer à :
             * TEACHER et ADMIN.
             */
            RecipientRole.STUDENT,
            Set.of(
                    RecipientRole.TEACHER,
                    RecipientRole.ADMIN
            ),

            /*
             * TEACHER peut envoyer à :
             * STUDENT, ADMIN et MANAGER.
             */
            RecipientRole.TEACHER,
            Set.of(
                    RecipientRole.STUDENT,
                    RecipientRole.ADMIN,
                    RecipientRole.MANAGER
            ),

            /*
             * MANAGER peut envoyer à :
             * ADMIN et TEACHER.
             *
             * TEACHER a été ajouté ici.
             */
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
         * Envoi Gmail.
         */
        gmailService.sendMail(
                recipientEmail,
                subject,
                body
        );

        /*
         * Envoi Kafka pour la cloche.
         */
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

        kafkaProducerService.sendEvent(event);
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