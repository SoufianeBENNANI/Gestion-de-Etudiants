package org.sid.gestion_etudiant.Kafka.controller;

import org.sid.gestion_etudiant.Kafka.Entity.AppEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationSseController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/stream")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        System.out.println("SSE client connected. Total: " + emitters.size());

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));

        return emitter;
    }

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "gestion-etudiant-notification-group"
    )
    public void listenKafka(AppEvent event) {
        System.out.println("SSE notification received: "
                + event.getEntity() + " - "
                + event.getAction() + " - "
                + event.getMessage());

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(event));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}