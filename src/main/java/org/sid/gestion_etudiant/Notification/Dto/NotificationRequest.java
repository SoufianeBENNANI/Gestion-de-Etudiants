package org.sid.gestion_etudiant.Notification.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;

@Data
public class NotificationRequest {

    @NotBlank
    @Email
    private String recipientEmail;

    @NotNull
    private RecipientRole recipientRole;

    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    private EventEntity entity = EventEntity.GMAIL;
    private EventAction action = EventAction.SENT;
    private Long entityId;
}