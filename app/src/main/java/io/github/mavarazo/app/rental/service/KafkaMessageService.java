package io.github.mavarazo.app.rental.service;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.github.mavarazo.vision.shared.persistence.entity.KafkaMessage;
import io.github.mavarazo.vision.shared.persistence.repository.KafkaMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class KafkaMessageService {

    private final KafkaMessageRepository kafkaMessageRepository;
    private final KafkaAvroSerializer serializer;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Transactional
    public <K, V> void saveMessage(final String topic, final K key, final V value) {
        final byte[] payload = serializer.serialize(topic, value);

        final KafkaMessage message = KafkaMessage.builder()
                .destination(topic)
                .key(String.valueOf(key))
                .payload(payload)
                .name(value != null ? value.getClass().getSimpleName() : null)
                .message(value != null ? objectMapper.writeValueAsString(value) : null)
                .build();

        kafkaMessageRepository.save(message);
    }
}
