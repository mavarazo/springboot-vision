package io.github.mavarazo.pocologgo.app.user.service;

import io.github.mavarazo.pocologgo.app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserConsumer {

    @JmsListener(destination = "${pocologgo.jms.queues.user}")
    public void listenJms(final User user) {
        log.info("Received user from JMS: {}", user);
    }

    @KafkaListener(topics = "${pocologgo.kafka.queues.user}")
    public void listenKafka(final User user) {
        log.info("Received user from Kafka: {}", user);
    }
}
