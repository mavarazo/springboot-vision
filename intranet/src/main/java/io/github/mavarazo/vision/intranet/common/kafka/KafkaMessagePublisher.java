package io.github.mavarazo.vision.intranet.common.kafka;

import io.github.mavarazo.vision.shared.persistence.entity.KafkaMessage;
import io.github.mavarazo.vision.shared.persistence.repository.KafkaMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessagePublisher {

    private final KafkaMessageRepository kafkaMessageRepository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Scheduled(fixedDelayString = "${vision.message.polling-interval:1000}")
    public void process() {
        final List<KafkaMessage> messages = kafkaMessageRepository.findTop50ByCreatedAtBeforeOrderByCreatedAtAsc(LocalDateTime.now());
        for (final KafkaMessage kafkaMessage : messages) {
            kafkaTemplate.send(kafkaMessage.getDestination(), kafkaMessage.getKey(), kafkaMessage.getPayload())
                    .whenComplete((_, throwable) -> {
                        if (throwable == null) {
                            kafkaMessageRepository.delete(kafkaMessage);
                        } else {
                            log.error("Unable to publish kafka message with ID '{}'", kafkaMessage.getId());
                        }
                    });
        }
    }
}
