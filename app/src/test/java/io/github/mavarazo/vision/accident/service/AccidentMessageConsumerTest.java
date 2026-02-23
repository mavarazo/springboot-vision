package io.github.mavarazo.vision.accident.service;

import io.github.mavarazo.vision.shared.kafka.model.AccidentMessage;
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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@EmbeddedKafka(
        partitions = 1,
        topics = {"accident"}
)
@ActiveProfiles("test")
class AccidentMessageConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockitoSpyBean
    private AccidentService accidentService;

    @Nested
    class ListenJmsTests {

        @Autowired
        private JmsTemplate jmsTemplate;

        @Test
        void listen_for_new_accident() {
            // arrange

            // act
            jmsTemplate.convertAndSend("accident", new AccidentMessage(UUID.randomUUID(), UUID.randomUUID()));

            // assert
            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> verify(accidentService).processAccident(any()));
        }
    }

    @Nested
    class ListenKafkaTests {

        @Autowired
        private KafkaTemplate<String, AccidentMessage> kafkaTemplate;

        @Test
        void listen_for_new_accident() {
            // act
            kafkaTemplate.send("accident", new AccidentMessage(UUID.randomUUID(), UUID.randomUUID()));

            // assert
            await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> verify(accidentService).processAccident(any()));
        }
    }
}