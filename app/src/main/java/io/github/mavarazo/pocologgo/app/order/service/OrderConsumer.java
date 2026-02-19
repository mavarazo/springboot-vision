package io.github.mavarazo.pocologgo.app.order.service;

import io.github.mavarazo.pocologgo.app.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final DeliveryService deliveryService;

    @JmsListener(destination = "${pocologgo.jms.queues.order}")
    public void listenJms(final Order order) {
        log.info("Received order from JMS: {}", order);
        deliveryService.processOrder(order);
    }

    @KafkaListener(topics = "${pocologgo.kafka.queues.order}")
    public void listenKafka(final Order order) {
        log.info("Received order from Kafka: {}", order);
        deliveryService.processOrder(order);
    }
}
