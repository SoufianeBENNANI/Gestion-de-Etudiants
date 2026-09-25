package org.sid.gestion_etudiant.Kafka.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.controller.NotificationSseController;
import org.sid.gestion_etudiant.Notification.Entity.Notification;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.sid.gestion_etudiant.Notification.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationRepository notificationRepository;
    private final NotificationSseController notificationSseController;

    @Value("${app.admin.email}")
    private String adminEmail;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.group-id}"
    )
    public void consume(AppEvent event) {

        if (event == null) {
            log.warn(
                    "Événement Kafka ignoré : événement null."
            );
            return;
        }

        validateConfiguration();

        log.info(
                "Événement Kafka reçu : entity={}, action={}, entityId={}",
                event.getEntity(),
                event.getAction(),
                event.getEntityId()
        );

        /*
         * La notification est toujours sauvegardée,
         * même si l'administrateur est déconnecté.
         */
        Notification savedNotification =
                saveAdminNotification(event);

        /*
         * L'envoi SSE est seulement utilisé lorsque
         * l'administrateur est connecté.
         */
        sendSseSafely(
                event,
                savedNotification
        );
    }

    private Notification saveAdminNotification(
            AppEvent event
    ) {

        Notification notification =
                Notification.builder()
                        .senderEmail(
                                event.getSenderEmail()
                        )
                        .senderRole(
                                event.getSenderRole()
                        )
                        .recipientEmail(
                                adminEmail
                        )
                        .recipientRole(
                                RecipientRole.ADMIN
                        )
                        .subject(
                                defaultValue(
                                        event.getSubject(),
                                        "Nouvelle notification"
                                )
                        )
                        .message(
                                defaultValue(
                                        event.getMessage(),
                                        "Un nouvel événement a été reçu."
                                )
                        )
                        .entity(
                                event.getEntity()
                        )
                        .action(
                                event.getAction()
                        )
                        .entityId(
                                event.getEntityId()
                        )
                        .readStatus(false)
                        .createdAt(
                                event.getCreatedAt() != null
                                        ? event.getCreatedAt()
                                        : LocalDateTime.now()
                        )
                        .build();

        Notification savedNotification =
                notificationRepository.saveAndFlush(
                        notification
                );

        log.info(
                "Notification ADMIN sauvegardée : id={}, readStatus={}",
                savedNotification.getId(),
                savedNotification.isReadStatus()
        );

        return savedNotification;
    }

    private void sendSseSafely(
            AppEvent event,
            Notification savedNotification
    ) {
        try {
            event.setRecipientEmail(
                    adminEmail
            );

            event.setRecipientRole(
                    RecipientRole.ADMIN
            );

            notificationSseController.sendNotification(
                    event
            );

            log.info(
                    "Notification SSE envoyée : notificationId={}",
                    savedNotification.getId()
            );

        } catch (Exception exception) {

            log.warn(
                    "Notification {} sauvegardée, mais SSE indisponible : {}",
                    savedNotification.getId(),
                    exception.getMessage()
            );
        }
    }

    private void validateConfiguration() {

        if (!StringUtils.hasText(adminEmail)) {
            throw new IllegalStateException(
                    "La propriété app.admin.email est absente ou vide."
            );
        }
    }

    private String defaultValue(
            String value,
            String fallback
    ) {
        return StringUtils.hasText(value)
                ? value
                : fallback;
    }
}