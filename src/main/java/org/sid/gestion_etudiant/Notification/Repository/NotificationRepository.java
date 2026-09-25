package org.sid.gestion_etudiant.Notification.Repository;

import org.sid.gestion_etudiant.Notification.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByRecipientEmailOrderByCreatedAtDesc(
            String recipientEmail
    );

    List<Notification>
    findByRecipientEmailAndReadStatusFalseOrderByCreatedAtDesc(
            String recipientEmail
    );

    long countByRecipientEmailAndReadStatusFalse(
            String recipientEmail
    );

    Optional<Notification>
    findByIdAndRecipientEmail(
            Long id,
            String recipientEmail
    );
}