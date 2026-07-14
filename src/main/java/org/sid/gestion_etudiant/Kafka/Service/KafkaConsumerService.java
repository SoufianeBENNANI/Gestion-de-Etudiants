package org.sid.gestion_etudiant.Kafka.Service;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Kafka.controller.NotificationSseController;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationSseController
            notificationSseController;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.group-id}"
    )
    public void consume(
            AppEvent event
    ) {
        System.out.println(
                "Kafka event received: "
                        + event.getEntity()
                        + " - "
                        + event.getAction()
                        + " - ID: "
                        + event.getEntityId()
                        + " - Message: "
                        + event.getMessage()
        );

        notificationSseController.sendNotification(
                event
        );
    }
}