package org.sid.gestion_etudiant.Notification.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.sid.gestion_etudiant.Kafka.Enum.EventAction;
import org.sid.gestion_etudiant.Kafka.Enum.EventEntity;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_email")
    private String senderEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role")
    private RecipientRole senderRole;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role", nullable = false)
    private RecipientRole recipientRole;

    @Column(name = "subject")
    private String subject;

    @Column(
            name = "message",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity")
    private EventEntity entity;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private EventAction action;

    @Column(name = "entity_id")
    private Long entityId;

    /*
     * Le champ Java s'appelle readStatus,
     * mais la colonne PostgreSQL s'appelle read.
     */
    @Builder.Default
    @Column(
            name = "read",
            nullable = false
    )
    private boolean readStatus = false;

    @Builder.Default
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}