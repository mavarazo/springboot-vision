package io.github.mavarazo.vision.intranet.common.kafka;

import io.github.mavarazo.vision.shared.persistence.entity.KafkaMessage;
import io.github.mavarazo.vision.shared.testing.TestDataManager;
import io.github.mavarazo.vision.shared.testing.TestDataManagerTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDataManagerTestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "vision.message.polling-interval:-1")
@EmbeddedKafka(
        partitions = 1,
        topics = "rental"
)
class KafkaMessagePublisherTest {

    @Autowired
    private TestDataManager testDataManager;

    @MockitoSpyBean
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private KafkaMessagePublisher sut;

    @BeforeEach
    void setUp() {
        testDataManager.truncateAll();
    }

    @Test
    void name() {
        // arrange
        testDataManager.persistAndGetId(KafkaMessage.builder()
                .destination("rental")
                .key("e0bef468-ef81-4808-8e52-c981d59edcd6")
                .payload("bingo".getBytes(StandardCharsets.UTF_8))
                .build());

        // act
        sut.process();

        // assert
        await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(kafkaTemplate).send(anyString(), anyString(), any()));
    }
}