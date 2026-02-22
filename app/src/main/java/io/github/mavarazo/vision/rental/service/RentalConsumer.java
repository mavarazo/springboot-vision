package io.github.mavarazo.vision.rental.service;

import io.github.mavarazo.vision.rental.model.RentalMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RentalConsumer {

    @JmsListener(destination = "${vision.jms.queues.rental}")
    public void listenJms(final RentalMessage message) {
        log.info("Received rental message from JMS: {}", message);
    }

    @KafkaListener(topics = "${vision.kafka.queues.rental}")
    public void listenKafka(final ConsumerRecord<String, RentalMessage> record) {
        log.info("Received rental message from Kafka: {}", record.key());
    }
}
