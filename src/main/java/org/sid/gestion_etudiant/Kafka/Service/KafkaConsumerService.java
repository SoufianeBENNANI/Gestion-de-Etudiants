package org.sid.gestion_etudiant.Kafka.Service;

import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "gestion-etudiant-log-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(AppEvent event) {
        System.out.println("Kafka event received: "
                + event.getEntity()
                + " - "
                + event.getAction()
                + " - ID: "
                + event.getEntityId()
                + " - Message: "
                + event.getMessage());
    }
}