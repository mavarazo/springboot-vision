package io.github.mavarazo.vision.shared.kafka.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

@Slf4j
public class ProducerLoggingInterceptor implements ProducerInterceptor<Object, Object> {

    @Override
    public ProducerRecord<Object, Object> onSend(final ProducerRecord<Object, Object> record) {
        log.atInfo()
                .addKeyValue("topic", record.topic())
                .addKeyValue("partition", record.partition() != null ? record.partition() : "dynamic")
                .addKeyValue("key", record.key())
                .addKeyValue("type", record.value() != null ? record.value().getClass().getSimpleName() : "n/a")
                .log("Sending record");

        return record;
    }

    @Override
    public void onAcknowledgement(final RecordMetadata metadata, final Exception exception) {
        if (exception == null) {
            log.atInfo()
                    .addKeyValue("topic", metadata.topic())
                    .addKeyValue("partition", metadata.partition())
                    .addKeyValue("offset", metadata.offset())
                    .addKeyValue("timestamp", metadata.timestamp())
                    .log("Acknowledged");
        } else {
            log.atError()
                    .addKeyValue("topic", (metadata != null) ? metadata.topic() : "n/a")
                    .setCause(exception)
                    .log("Delivery failed: {}", exception.getMessage());
        }
    }

    @Override
    public void close() {
        // unused
    }

    @Override
    public void configure(final Map<String, ?> configs) {
        // unused
    }
}
