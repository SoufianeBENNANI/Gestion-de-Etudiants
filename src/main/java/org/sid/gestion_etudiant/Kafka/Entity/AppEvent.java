package org.sid.gestion_etudiant.Kafka.Entity;

import lombok.Data;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;

import java.time.LocalDateTime;

@Data
public class AppEvent {

    private EventEntity entity;
    private EventAction action;
    private Long entityId;
    private String message;


    private LocalDateTime createdAt = LocalDateTime.now();
}