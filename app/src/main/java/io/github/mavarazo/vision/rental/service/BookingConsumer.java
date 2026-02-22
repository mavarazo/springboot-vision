package io.github.mavarazo.vision.rental.service;

import io.github.mavarazo.vision.rental.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookingConsumer {

    @JmsListener(destination = "${vision.jms.queues.booking}")
    public void listenJms(final Booking booking) {
        log.info("Received booking from JMS: {}", booking);
    }

    @KafkaListener(topics = "${vision.kafka.queues.booking}")
    public void listenKafka(final ConsumerRecord<String, Booking> record) {
        log.info("Received booking from Kafka: {}", record.key());
    }
}
