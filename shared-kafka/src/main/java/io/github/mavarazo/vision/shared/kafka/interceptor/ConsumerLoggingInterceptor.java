package io.github.mavarazo.vision.shared.kafka.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.listener.RecordInterceptor;

@Slf4j
public class ConsumerLoggingInterceptor implements RecordInterceptor<Object, Object> {

    @Override
    public @Nullable ConsumerRecord<Object, Object> intercept(final ConsumerRecord<Object, Object> record, final Consumer<Object, Object> consumer) {
        log.atInfo()
                .addKeyValue("topic", record.topic())
                .addKeyValue("partition", record.partition())
                .addKeyValue("offset", record.offset())
                .addKeyValue("key", record.key())
                .addKeyValue("type", record.value() != null ? record.value().getClass().getSimpleName() : "n/a")
                .log("Processing record");

        return record;
    }

    @Override
    public void success(final ConsumerRecord<Object, Object> record, final Consumer<Object, Object> consumer) {
        log.atInfo()
                .addKeyValue("topic", record.topic())
                .addKeyValue("partition", record.partition())
                .addKeyValue("offset", record.offset())
                .log("Successfully processed record");
    }

    @Override
    public void failure(final ConsumerRecord<Object, Object> record, final Exception exception, final Consumer<Object, Object> consumer) {
        log.atError()
                .addKeyValue("topic", record.topic())
                .addKeyValue("partition", record.partition())
                .addKeyValue("offset", record.offset())
                .addKeyValue("key", record.key())
                .setCause(exception)
                .log("Failed to process record: {}", exception.getMessage());
    }
}
