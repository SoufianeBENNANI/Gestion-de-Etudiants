package org.sid.gestion_etudiant.Kafka.controller;

import lombok.RequiredArgsConstructor;
import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.sid.gestion_etudiant.Notification.Enum.RecipientRole;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationSseController {

    private static final long SSE_TIMEOUT =
            30L * 60L * 1000L;

    private final Map<RecipientRole, List<SseEmitter>>
            emittersByRole =
            new ConcurrentHashMap<>();

    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter stream(
            Authentication authentication
    ) {
        RecipientRole connectedRole =
                extractRole(authentication);

        SseEmitter emitter =
                new SseEmitter(SSE_TIMEOUT);

        emittersByRole
                .computeIfAbsent(
                        connectedRole,
                        role -> new CopyOnWriteArrayList<>()
                )
                .add(emitter);

        emitter.onCompletion(
                () -> removeEmitter(
                        connectedRole,
                        emitter
                )
        );

        emitter.onTimeout(() -> {
            removeEmitter(
                    connectedRole,
                    emitter
            );

            emitter.complete();
        });

        emitter.onError(error ->
                removeEmitter(
                        connectedRole,
                        emitter
                )
        );

        try {
            emitter.send(
                    SseEmitter
                            .event()
                            .name("connected")
                            .data(
                                    Map.of(
                                            "connected", true,
                                            "role", connectedRole.name()
                                    )
                            )
            );

            System.out.println(
                    "SSE connecté pour le rôle : "
                            + connectedRole
            );
        } catch (IOException exception) {
            removeEmitter(
                    connectedRole,
                    emitter
            );

            emitter.completeWithError(
                    exception
            );
        }

        return emitter;
    }

    public void sendNotification(
            AppEvent event
    ) {
        if (
                event == null ||
                        event.getRecipientRole() == null
        ) {
            System.out.println(
                    "Événement SSE ignoré : recipientRole absent"
            );

            return;
        }

        RecipientRole recipientRole =
                event.getRecipientRole();

        List<SseEmitter> roleEmitters =
                emittersByRole.getOrDefault(
                        recipientRole,
                        List.of()
                );

        System.out.println(
                "Envoi SSE vers "
                        + recipientRole
                        + " - Connexions actives : "
                        + roleEmitters.size()
        );

        for (SseEmitter emitter : roleEmitters) {
            try {
                emitter.send(
                        SseEmitter
                                .event()
                                .name("notification")
                                .data(event)
                );

                System.out.println(
                        "Notification SSE envoyée vers "
                                + recipientRole
                                + " : "
                                + event.getMessage()
                );
            } catch (IOException exception) {
                removeEmitter(
                        recipientRole,
                        emitter
                );

                emitter.completeWithError(
                        exception
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
        for (
                GrantedAuthority authority :
                authentication.getAuthorities()
        ) {
            String role =
                    authority.getAuthority();

            if (!role.startsWith("ROLE_")) {
                continue;
            }

            String roleName =
                    role.substring(
                            "ROLE_".length()
                    );

            try {
                return RecipientRole.valueOf(
                        roleName.toUpperCase()
                );
            } catch (
                    IllegalArgumentException ignored
            ) {
                // Continuer
            }
        }

        throw new IllegalStateException(
                "Aucun rôle valide trouvé pour la connexion SSE."
        );
    }
}