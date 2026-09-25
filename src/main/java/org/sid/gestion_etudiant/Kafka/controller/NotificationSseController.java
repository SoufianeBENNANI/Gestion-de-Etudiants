package org.sid.gestion_etudiant.Kafka.controller;

import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/notifications")
public class NotificationSseController {

    private static final long SSE_TIMEOUT =
            30L * 60L * 1000L;


    private final CopyOnWriteArrayList<SseEmitter>
            adminEmitters =
            new CopyOnWriteArrayList<>();


    /*
     * ADMIN connecté uniquement.
     */
    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter stream(
            Authentication authentication
    ) {

        if (!isAdmin(authentication)) {

            throw new AccessDeniedException(
                    "Seul ADMIN peut accéder aux notifications SSE."
            );
        }


        SseEmitter emitter =
                new SseEmitter(
                        SSE_TIMEOUT
                );


        adminEmitters.add(
                emitter
        );


        emitter.onCompletion(
                () ->
                        adminEmitters.remove(
                                emitter
                        )
        );


        emitter.onTimeout(
                () -> {

                    adminEmitters.remove(
                            emitter
                    );

                    emitter.complete();
                }
        );


        emitter.onError(
                error ->
                        adminEmitters.remove(
                                emitter
                        )
        );


        try {

            emitter.send(
                    SseEmitter
                            .event()
                            .name(
                                    "connected"
                            )
                            .data(
                                    Map.of(
                                            "connected",
                                            true,

                                            "role",
                                            "ADMIN"
                                    )
                            )
            );


            System.out.println(
                    "ADMIN connecté au SSE."
            );


        } catch (
                IOException exception
        ) {

            adminEmitters.remove(
                    emitter
            );

            emitter.completeWithError(
                    exception
            );
        }


        return emitter;
    }


    /*
     * Appelé par KafkaConsumerService.
     */
    public void sendNotification(
            AppEvent event
    ) {

        if (event == null) {
            return;
        }


        System.out.println(
                "Nombre ADMIN SSE connectés : "
                        + adminEmitters.size()
        );


        for (
                SseEmitter emitter :
                adminEmitters
        ) {

            try {

                emitter.send(
                        SseEmitter
                                .event()
                                .name(
                                        "notification"
                                )
                                .data(
                                        event
                                )
                );


            } catch (
                    IOException exception
            ) {

                adminEmitters.remove(
                        emitter
                );

                emitter.completeWithError(
                        exception
                );
            }
        }
    }


    private boolean isAdmin(
            Authentication authentication
    ) {

        if (authentication == null) {
            return false;
        }


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority
                                        .getAuthority()
                                        .equalsIgnoreCase(
                                                "ROLE_ADMIN"
                                        )
                );
    }
}