package io.github.mavarazo.vision;

import io.github.mavarazo.vision.accident.model.AccidentMessage;
import io.github.mavarazo.vision.rental.model.RentalMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(final ConsumerFactory<String, String> consumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        final ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setObservationEnabled(true);
        return factory;
    }

    @Bean
    public KafkaTemplate<String, AccidentMessage> accidentKafkaTemplate(final ProducerFactory<String, AccidentMessage> pf) {
        final KafkaTemplate<String, AccidentMessage> template = new KafkaTemplate<>(pf);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    public KafkaTemplate<String, RentalMessage> rentalKafkaTemplate(final ProducerFactory<String, RentalMessage> pf) {
        final KafkaTemplate<String, RentalMessage> template = new KafkaTemplate<>(pf);
        template.setObservationEnabled(true);
        return template;
    }
}
