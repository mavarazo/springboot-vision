package io.github.mavarazo.pocologgo.app;

import io.github.mavarazo.pocologgo.app.order.model.Order;
import io.github.mavarazo.pocologgo.app.user.model.User;
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
    public KafkaTemplate<String, Order> orderKafkaTemplate(final ProducerFactory<String, Order> pf) {
        final KafkaTemplate<String, Order> template = new KafkaTemplate<>(pf);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    public KafkaTemplate<String, User> userKafkaTemplate(final ProducerFactory<String, User> pf) {
        final KafkaTemplate<String, User> template = new KafkaTemplate<>(pf);
        template.setObservationEnabled(true);
        return template;
    }
}
