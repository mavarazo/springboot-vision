package io.github.mavarazo.pocologgo.app.order.service;

import io.github.mavarazo.pocologgo.app.order.model.Order;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
        partitions = 1,
        topics = {"order"}
)
@ActiveProfiles("test")
class OrderConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockitoSpyBean
    private DeliveryService deliveryService;

    @Nested
    class ListenJmsTests {

        @Autowired
        private JmsTemplate jmsTemplate;

        @Test
        void listen_for_new_order() {
            // arrange

            // act
            jmsTemplate.convertAndSend("order", new Order("Bingo"));

            // assert
            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> verify(deliveryService).processOrder(any()));
        }
    }

    @Nested
    class ListenKafkaTests {

        @Autowired
        private KafkaTemplate<String, Order> kafkaTemplate;

        @Test
        void listen_for_new_order() {
            // act
            kafkaTemplate.send("order", new Order("Bingo"));

            // assert
            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> verify(deliveryService).processOrder(any()));
        }
    }
}