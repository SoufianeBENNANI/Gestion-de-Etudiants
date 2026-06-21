package org.sid.gestion_etudiant.Kafka.Service;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, AppEvent> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    public void sendEvent(AppEvent event) {
        System.out.println("Sending Kafka event: "
                + event.getEntity()
                + " - "
                + event.getAction()
                + " - "
                + event.getMessage());

        kafkaTemplate.send(topic, event.getEntity().name(), event);
    }
}