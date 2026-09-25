package org.sid.gestion_etudiant.Notification.Service;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Kafka.Service.KafkaProducerService;
import org.sid.gestion_etudiant.Messaging.Service.GmailService;
import org.sid.gestion_etudiant.Notification.Entity.Notification;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.sid.gestion_etudiant.Notification.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final GmailService gmailService;
    private final KafkaProducerService kafkaProducerService;
    private final NotificationRepository notificationRepository;

    /*
     * Email de l'ADMIN qui recevra les notifications Kafka.
     */
    @Value("${app.admin.email}")
    private String adminEmail;


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


    /*
     * =========================================================
     * ENVOI DE NOTIFICATION
     * =========================================================
     */
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
         * =====================================================
         * 1. EMAIL GMAIL
         * =====================================================
         *
         * Le vrai destinataire reçoit le mail.
         * Il n'a pas besoin d'être connecté à ton application.
         */
        gmailService.sendMail(
                recipientEmail,
                subject,
                body
        );


        /*
         * =====================================================
         * 2. ÉVÉNEMENT KAFKA POUR ADMIN
         * =====================================================
         *
         * Seul ADMIN reçoit l'événement Kafka dans sa cloche.
         */
        AppEvent adminEvent =
                createEvent(
                        senderEmail,
                        senderRole,

                        adminEmail,
                        RecipientRole.ADMIN,

                        subject,
                        body,

                        entity,
                        action,
                        entityId
                );


        kafkaProducerService.sendEvent(
                adminEvent
        );
    }


    /*
     * =========================================================
     * CRÉATION APP EVENT
     * =========================================================
     */
    private AppEvent createEvent(
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

        AppEvent event =
                new AppEvent();


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


        event.setEntityId(
                entityId
        );


        event.setSubject(
                subject
        );


        event.setMessage(
                body
        );


        event.setSenderEmail(
                senderEmail
        );


        event.setSenderRole(
                senderRole
        );


        event.setRecipientEmail(
                recipientEmail
        );


        event.setRecipientRole(
                recipientRole
        );


        event.setCreatedAt(
                LocalDateTime.now()
        );


        return event;
    }


    /*
     * =========================================================
     * TOUTES LES NOTIFICATIONS
     * =========================================================
     */
    public List<Notification> getMyNotifications(
            String email
    ) {

        return notificationRepository
                .findByRecipientEmailOrderByCreatedAtDesc(
                        email
                );
    }


    /*
     * =========================================================
     * NOTIFICATIONS NON LUES
     * =========================================================
     */
    public List<Notification> getUnreadNotifications(
            String email
    ) {

        return notificationRepository
                .findByRecipientEmailAndReadStatusFalseOrderByCreatedAtDesc(
                        email
                );
    }


    /*
     * =========================================================
     * COMPTEUR DE NOTIFICATIONS NON LUES
     * =========================================================
     */
    public long countUnread(
            String email
    ) {

        return notificationRepository
                .countByRecipientEmailAndReadStatusFalse(
                        email
                );
    }


    /*
     * =========================================================
     * MARQUER COMME LUE
     * =========================================================
     */
    public Notification markAsRead(
            Long id,
            String connectedUserEmail
    ) {

        Notification notification =
                notificationRepository
                        .findByIdAndRecipientEmail(
                                id,
                                connectedUserEmail
                        )
                        .orElseThrow(
                                () ->
                                        new AccessDeniedException(
                                                "Notification inexistante ou accès interdit."
                                        )
                        );


        notification.setReadStatus(
                true
        );


        return notificationRepository.save(
                notification
        );
    }


    /*
     * =========================================================
     * VALIDATION DES PERMISSIONS
     * =========================================================
     */
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