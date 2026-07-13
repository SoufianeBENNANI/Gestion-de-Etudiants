package org.sid.gestion_etudiant.Kafka.controller;

import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationSseController {

    private final Map<RecipientRole, List<SseEmitter>>
            emittersByRole = new ConcurrentHashMap<>();

    @GetMapping(
            value = "/stream",
            produces = "text/event-stream"
    )
    public SseEmitter stream(
            Authentication authentication
    ) {
        RecipientRole connectedRole =
                extractRole(authentication);

        SseEmitter emitter =
                new SseEmitter(Long.MAX_VALUE);

        emittersByRole
                .computeIfAbsent(
                        connectedRole,
                        role -> new CopyOnWriteArrayList<>()
                )
                .add(emitter);

        System.out.println(
                "SSE connecté : "
                        + connectedRole
                        + " - Total : "
                        + emittersByRole
                        .get(connectedRole)
                        .size()
        );

        Runnable removeEmitter =
                () -> removeEmitter(
                        connectedRole,
                        emitter
                );

        emitter.onCompletion(removeEmitter);
        emitter.onTimeout(removeEmitter);
        emitter.onError(error -> removeEmitter.run());

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data(
                                    "SSE connected for "
                                            + connectedRole
                            )
            );
        } catch (IOException exception) {
            removeEmitter.run();
        }

        return emitter;
    }

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "gestion-etudiant-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenKafka(AppEvent event) {
        RecipientRole recipientRole =
                event.getRecipientRole();

        if (recipientRole == null) {
            return;
        }

        List<SseEmitter> recipientEmitters =
                emittersByRole.getOrDefault(
                        recipientRole,
                        List.of()
                );

        System.out.println(
                "Notification reçue : "
                        + event.getSenderRole()
                        + " -> "
                        + recipientRole
                        + " - "
                        + event.getMessage()
        );

        for (SseEmitter emitter : recipientEmitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("notification")
                                .data(event)
                );
            } catch (IOException exception) {
                removeEmitter(
                        recipientRole,
                        emitter
                );
            }
        }
    }

    private void removeEmitter(
            RecipientRole role,
            SseEmitter emitter
    ) {
        List<SseEmitter> emitters =
                emittersByRole.get(role);

        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);

        if (emitters.isEmpty()) {
            emittersByRole.remove(role);
        }
    }

    private RecipientRole extractRole(
            Authentication authentication
    ) {
        return authentication.getAuthorities()
                .stream()
                .map(Object::toString)
                .filter(
                        authority ->
                                authority.startsWith("ROLE_")
                )
                .map(
                        authority ->
                                authority.substring(5)
                )
                .map(role -> {
                    try {
                        return RecipientRole.valueOf(role);
                    } catch (
                            IllegalArgumentException exception
                    ) {
                        return null;
                    }
                })
                .filter(role -> role != null)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Rôle utilisateur introuvable"
                        )
                );
    }
}