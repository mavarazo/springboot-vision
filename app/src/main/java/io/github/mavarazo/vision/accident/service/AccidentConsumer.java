package io.github.mavarazo.vision.accident.service;

import io.github.mavarazo.vision.accident.model.AccidentMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccidentConsumer {

    private final AccidentService accidentService;

    @JmsListener(destination = "${vision.jms.queues.accident}")
    public void listenJms(final AccidentMessage accidentMessage) {
        log.info("Received new accident from JMS: {}", accidentMessage);
        accidentService.processAccident(accidentMessage);
    }

    @KafkaListener(topics = "${vision.kafka.queues.accident}")
    public void listenKafka(final AccidentMessage accidentMessage) {
        log.info("Received new accident from Kafka: {}", accidentMessage);
        accidentService.processAccident(accidentMessage);
    }
}
